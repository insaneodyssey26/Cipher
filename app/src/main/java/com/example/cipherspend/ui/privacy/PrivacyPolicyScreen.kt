package com.masum.cipher.ui.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.ui.theme.CipherBlue

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                title = "SMS Access",
                body = "Cipher requests permission to read SMS messages solely to detect and parse bank transaction alerts. SMS content is processed in memory and immediately discarded — it is never stored in raw form, logged, or transmitted anywhere."
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
                title = "Exports & Backups",
                body = "When you export a backup, the file is encrypted with a password you choose and saved to a location you pick. Cipher does not retain a copy and has no access to where you store it."
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
                text = "Last updated: June 2026",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PolicyMeta() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Cipher",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "A privacy-first finance tracker. No accounts. No cloud. No tracking.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = CipherBlue
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}
