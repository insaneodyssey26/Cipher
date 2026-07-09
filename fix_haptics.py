import os

files_to_patch = [
    "app/src/main/java/com/masum/cipher/ui/dashboard/DashboardScreen.kt",
    "app/src/main/java/com/masum/cipher/ui/components/DashboardComponents.kt",
    "app/src/main/java/com/masum/cipher/ui/insights/DayDetailScreen.kt",
    "app/src/main/java/com/masum/cipher/ui/insights/InsightsScreen.kt",
    "app/src/main/java/com/masum/cipher/ui/settings/SettingsScreen.kt"
]

import_statement = "import com.masum.cipher.core.util.performVibrate\n"

for f in files_to_patch:
    with open(f, 'r') as file:
        content = file.read()

    # Add import
    if "com.masum.cipher.core.util.performVibrate" not in content:
        content = content.replace("import compose.icons.LucideIcons", import_statement + "import compose.icons.LucideIcons")

    # Replace LocalHapticFeedback with LocalView
    content = content.replace("val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current",
                              "val view = androidx.compose.ui.platform.LocalView.current")
    content = content.replace("val haptic = LocalHapticFeedback.current",
                              "val view = androidx.compose.ui.platform.LocalView.current")

    # Replace HapticFeedback usages
    content = content.replace("if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)",
                              "view.performVibrate(isHapticsEnabled, isLongPress = true)")
    content = content.replace("if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)",
                              "view.performVibrate(isHapticsEnabled)")
    content = content.replace("if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)",
                              "view.performVibrate(state.isHapticsEnabled, isLongPress = true)")
    content = content.replace("if (state.isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)",
                              "view.performVibrate(state.isHapticsEnabled)")
    content = content.replace("if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)",
                              "view.performVibrate(it, isLongPress = true)")

    with open(f, 'w') as file:
        file.write(content)
