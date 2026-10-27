package com.masum.cipher.core.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.masum.cipher.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String? = null,
    val body: String? = null,
    val html_url: String? = null,
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    val name: String,
    val size: Long = 0,
    val browser_download_url: String
)

data class AppUpdateCheckResult(
    val isUpdateAvailable: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val changelog: String,
    val downloadUrl: String?,
    val releasePageUrl: String?
)

sealed interface DownloadProgress {
    data class Progress(val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadProgress
    data class Success(val apkFile: File) : DownloadProgress
    data class Error(val message: String) : DownloadProgress
}

object GitHubUpdateManager {

    private const val GITHUB_REPO = "insaneodyssey26/Cipher"
    private const val LATEST_RELEASE_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun checkForUpdate(): AppUpdateCheckResult = withContext(Dispatchers.IO) {
        val currentVersion = BuildConfig.VERSION_NAME
        val connection = (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("User-Agent", "Cipher-Android-App")
        }

        try {
            if (connection.responseCode !in 200..299) {
                return@withContext AppUpdateCheckResult(
                    isUpdateAvailable = false,
                    currentVersion = currentVersion,
                    latestVersion = currentVersion,
                    changelog = "",
                    downloadUrl = null,
                    releasePageUrl = null
                )
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val release = json.decodeFromString<GitHubRelease>(responseBody)
            val releaseVersion = cleanVersion(release.tag_name)
            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            val isNewer = compareVersions(releaseVersion, currentVersion) > 0

            AppUpdateCheckResult(
                isUpdateAvailable = isNewer,
                currentVersion = currentVersion,
                latestVersion = releaseVersion,
                changelog = release.body.orEmpty(),
                downloadUrl = apkAsset?.browser_download_url,
                releasePageUrl = release.html_url
            )
        } catch (_: Exception) {
            AppUpdateCheckResult(
                isUpdateAvailable = false,
                currentVersion = currentVersion,
                latestVersion = currentVersion,
                changelog = "",
                downloadUrl = null,
                releasePageUrl = null
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (DownloadProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val targetFile = File(updatesDir, "cipher_update.apk")
        if (targetFile.exists()) {
            targetFile.delete()
        }

        var connection: HttpURLConnection? = null
        try {
            var redirectUrl = downloadUrl
            var redirects = 0
            while (redirects < 5) {
                connection = (URL(redirectUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "Cipher-Android-App")
                }
                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307 || code == 308) {
                    val location = connection.getHeaderField("Location") ?: break
                    redirectUrl = location
                    connection.disconnect()
                    redirects++
                } else {
                    break
                }
            }

            val conn = connection ?: throw IllegalStateException("Failed to open connection")
            val totalBytes = conn.contentLength.toLong()
            var bytesDownloaded = 0L

            conn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    var lastPercent = -1
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesDownloaded += read
                        val percent = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0
                        if (percent != lastPercent) {
                            lastPercent = percent
                            onProgress(DownloadProgress.Progress(percent, bytesDownloaded, totalBytes))
                        }
                    }
                }
            }
            onProgress(DownloadProgress.Success(targetFile))
        } catch (e: Exception) {
            onProgress(DownloadProgress.Error(e.message ?: "Download failed"))
        } finally {
            connection?.disconnect()
        }
    }

    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(installIntent)
    }

    private fun cleanVersion(raw: String): String {
        return raw.trim()
            .removePrefix("Cipher-")
            .removePrefix("cipher-")
            .removePrefix("v")
            .removePrefix("V")
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) {
                return p1.compareTo(p2)
            }
        }
        return 0
    }
}
