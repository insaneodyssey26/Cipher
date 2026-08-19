package com.masum.cipher.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.lucideicons.ArrowLeft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(compose.icons.LucideIcons.ArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            PolicyMeta()

            PolicySection(
                title = "The Short Version",
                body = "Cipher does not collect, transmit, or share any of your data. Everything stays on your device, encrypted. That's the whole point of the app."
            )

            PolicySection(
                title = "Data Storage",
                body = "All transaction records, settings, and preferences are stored locally on your device using SQLCipher — an encrypted database. No data is written to external storage without your explicit action (export)."
            )

            PolicySection(
                title = "SMS & App Permissions",
                body = "Cipher requests permission to read SMS messages and (optionally) read app notifications solely to detect and parse financial transactions. Message content is processed in memory and immediately discarded — it is never stored in raw form, logged, or transmitted anywhere. Cipher may also request permission to send you notifications (for budget alerts), all processed locally."
            )

            PolicySection(
                title = "Biometrics",
                body = "If you enable biometric lock, authentication is handled entirely by your device's operating system via Android BiometricPrompt. Cipher never accesses, stores, or processes your biometric data."
            )

            PolicySection(
                title = "Network Access",
                body = "Cipher does not request internet permission and makes zero network calls. There are no remote servers, analytics SDKs, crash reporters, or ad libraries in this app."
            )

            PolicySection(
                title = "Exports & Auto Backups",
                body = "When you export a CSV, PDF statement, or configure Auto Backup, the files are saved directly to your selected device storage or cloud-synced folder via Android's native Storage Framework. Database backups are encrypted with a password you choose. Cipher does not transmit this data anywhere and has no remote access to where you store these files beyond the folder permissions you explicitly grant."
            )

            PolicySection(
                title = "Open Source",
                body = "Cipher is open source. You can audit every line of code on GitHub to verify all of the above. If you find anything that contradicts this policy, please open an issue."
            )

            PolicySection(
                title = "Changes",
                body = "If this policy ever changes, the updated version will be shipped with the app release and noted in the changelog."
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            Text(
                text = "Last updated: July 2026",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PolicyMeta() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "cipher.",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "A privacy-first finance tracker. No accounts. No cloud. No tracking.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 22.sp
        )
    }
}
