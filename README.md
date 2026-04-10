# Cipher

Cipher is a local-first, privacy-focused personal finance application for Android. It automates expense tracking by extracting transaction data from SMS alerts, ensuring all sensitive information remains encrypted on-device.

## Core Principles
- **Data Privacy**: No external servers or cloud synchronization.
- **Security**: Full database encryption using SQLCipher and biometric authentication.
- **Offline Operation**: No network permissions required.

## Key Features
- **Automated Parsing**: Heuristic engine for merchant and amount extraction from SMS.
- **Insights**: Subscription detection and spending trend analysis.
- **Budgeting**: Monthly spending limit configuration with visual health tracking.
- **Management**: Full manual control over the transaction ledger with undo support.
- **Portability**: Encrypted backups and standard CSV export.

## Technical Specifications
- **Platform**: Android 7.0+ (API 24)
- **UI**: Jetpack Compose / Material 3
- **Architecture**: MVI (Model-View-Intent)
- **Persistence**: Room with SQLCipher
- **Dependency Injection**: Hilt

## Build Instructions
Open the project in Android Studio (Compile SDK 35). Run `./gradlew :app:assembleDebug` to build the debug APK.

## Installing the APK and SMS permission

1. Turn off Play Protect: Play Store → Profile → Play Protect → Settings → turn off "Scan apps with Play Protect".
2. Allow installs from unknown sources for the app you'll use to install the APK (Chrome, Files, etc.): Settings → Apps → Special app access → Install unknown apps → select installer → Allow from this source.
3. Install the APK (tap it in a file manager or run `adb install path/to/app.apk`).
4. Open Cipher — the app will request SMS permission. Grant it.
	- If the SMS permission is shown as restricted and can't be granted, enable restricted access: Settings → Apps → Special app access → (look for "Allow access to restricted settings") → select Cipher → enable → return to Cipher and grant SMS permission.
5. Turn Play Protect back on if you want.

That's it — Cipher works once SMS permission is granted.

For version history, see [RELEASE_NOTES.md](RELEASE_NOTES.md).
