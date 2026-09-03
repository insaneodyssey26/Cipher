package com.masum.cipher.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import com.masum.cipher.R
import com.masum.cipher.ui.theme.DMSans
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
                        text = stringResource(R.string.privacy_policy_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(compose.icons.LucideIcons.ArrowLeft, contentDescription = stringResource(R.string.action_back))
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
                title = stringResource(R.string.privacy_short_version_title),
                body = stringResource(R.string.privacy_short_version_body)
            )

            PolicySection(
                title = stringResource(R.string.privacy_data_storage_title),
                body = stringResource(R.string.privacy_data_storage_body)
            )

            PolicySection(
                title = stringResource(R.string.privacy_permissions_title),
                body = stringResource(R.string.privacy_permissions_body)
            )

            PolicySection(
                title = stringResource(R.string.privacy_biometrics_title),
                body = stringResource(R.string.privacy_biometrics_body)
            )

            PolicySection(
                title = stringResource(R.string.privacy_network_title),
                body = stringResource(R.string.privacy_network_body)
            )

            PolicySection(
                title = stringResource(R.string.privacy_exports_title),
                body = stringResource(R.string.privacy_exports_body)
            )

            PolicySection(
                title = stringResource(R.string.privacy_open_source_title),
                body = stringResource(R.string.privacy_open_source_body)
            )

            PolicySection(
                title = stringResource(R.string.privacy_changes_title),
                body = stringResource(R.string.privacy_changes_body)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            Text(
                text = stringResource(R.string.privacy_last_updated),
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
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.5).sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.privacy_policy_tagline),
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
