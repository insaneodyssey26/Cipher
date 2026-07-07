import os
import re

files_to_update = [
    "app/src/main/java/com/masum/cipher/ui/components/Charts.kt",
    "app/src/main/java/com/masum/cipher/ui/components/VaultCard.kt",
    "app/src/main/java/com/masum/cipher/ui/components/TransactionDetailsSheet.kt",
    "app/src/main/java/com/masum/cipher/ui/dashboard/DashboardScreen.kt",
    "app/src/main/java/com/masum/cipher/ui/insights/DayDetailScreen.kt",
    "app/src/main/java/com/masum/cipher/ui/insights/InsightsScreen.kt",
    "app/src/main/java/com/masum/cipher/ui/onboarding/OnboardingScreen.kt",
    "app/src/main/java/com/masum/cipher/ui/settings/SettingsScreen.kt"
]

replacements = {
    r'\bMidnightDeep\b': 'MaterialTheme.colorScheme.background',
    r'\bVaultSurface\b': 'MaterialTheme.colorScheme.surface',
    r'\bVaultElevated\b': 'MaterialTheme.colorScheme.surfaceVariant',
    r'\bSlate50\b': 'MaterialTheme.colorScheme.onSurface',
    r'\bSlate400\b': 'MaterialTheme.colorScheme.onSurfaceVariant',
    r'\bSlate600\b': 'MaterialTheme.colorScheme.outline'
}

for file_path in files_to_update:
    if os.path.exists(file_path):
        with open(file_path, 'r') as f:
            content = f.read()
        
        # Add import if needed
        if "androidx.compose.material3.MaterialTheme" not in content and any(re.search(p, content) for p in replacements.keys()):
            if "import androidx.compose.material3." in content:
                content = re.sub(r'(import androidx\.compose\.material3\..*\n)', r'\1import androidx.compose.material3.MaterialTheme\n', content, count=1)
            else:
                content = content.replace("import androidx.compose.runtime.", "import androidx.compose.material3.MaterialTheme\nimport androidx.compose.runtime.")
        
        original_content = content
        for pattern, replacement in replacements.items():
            content = re.sub(pattern, replacement, content)
        
        if content != original_content:
            with open(file_path, 'w') as f:
                f.write(content)
            print(f"Updated {file_path}")
