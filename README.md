# Cipher Spend

Cipher Spend is a local-first, privacy-focused personal finance application for Android. It automates expense tracking by extracting transaction data from SMS alerts, ensuring all sensitive information remains encrypted on-device.

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

For version history, see [RELEASE_NOTES.md](RELEASE_NOTES.md).
