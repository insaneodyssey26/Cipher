# Cipher — Release Notes (v1.0) - Full Technical Notes

Release: 1.0
Date: 2026-03-26

Short summary
---------------
Cipher is a local-first, privacy-first personal finance companion for Android. It securely extracts transaction information from SMS alerts, stores data encrypted on-device, and presents a modern Compose-based dashboard and insights UI. This release focuses on a secure, minimal, extensible foundation for private financial tracking.

Highlights (what's new in v1.0)
--------------------------------
- Automated SMS parsing: local-only `SmsReceiver` and `SmsParser` extract merchant, amount and currency from incoming SMS alerts.
- Encrypted storage: Room + SQLCipher stores all transaction data encrypted at rest.
- Hardware-backed keying: designed to integrate with Android Keystore for stronger encryption (see `PROJECT_PLAN.md`).
- Biometric protection: optional biometric gating for sensitive screens and actions.
- Modern architecture: Hilt for DI, MVI-style view models, Kotlin Coroutines + Flow for reactive data streams.
- Compose UI: Material 3 design tokens and a performant dashboard, charts and detail screens.
- Offline-first: no network permissions; data never leaves the device by default.
- Bundled fonts: Google Sans font files included for a consistent, premium look across devices.

Why you might choose Cipher
---------------------------------

Quick facts / metadata
-----------------------
- Package: `com.example.cipherspend`
- Minimum supported Android: API 24 (Android 7.0)
- Target / compile SDK: 35
- Version: 1.0 (versionCode=1)
- Main permissions (requested at runtime): `RECEIVE_SMS`, `READ_SMS`

Security & Privacy
-------------------
- Data is encrypted with SQLCipher (Room + SQLCipher integration).
- The codebase is structured to use hardware-backed keystore when available (see `core/security` and `PROJECT_PLAN.md`).
- No network permissions are declared — by design this keeps user data local-only.
- Biometric integration is available for optional in-app protection (`core/security/BiometricAuthenticator.kt`).

Installation (developer & user guidance)
---------------------------------------
Developer build (recommended via Android Studio):

1. Open the project in Android Studio.
2. Build and Run on a device or emulator with API >= 24.

Command-line (Linux / WSL):

```bash
# build debug APK
./gradlew :app:assembleDebug

# install to a connected device (replace with the actual file path shown by Gradle)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Notes on runtime permissions
- After installation the app will request SMS permissions at runtime when needed. Grant `RECEIVE_SMS` and `READ_SMS` for automatic parsing.
- On Android 10+ some OEMs restrict background SMS broadcast delivery; please test on target devices.

Fonts and typography
---------------------
- This release bundles Google Sans font files under `app/src/main/res/font/` (`google_sans_regular.ttf`, `google_sans_medium.ttf`, `google_sans_semibold.ttf`, `google_sans_bold.ttf`).
-- Compose typography is wired up in `ui/theme/Type.kt` using a `GoogleSansFontFamily` and the `Typography` object. The app's Compose UI should use these fonts by default via `CipherTheme` (`ui/theme/Theme.kt`).

If text still appears as another system font (e.g., Inter):
- Ensure your composables use `MaterialTheme.typography` (or the text styles defined in `Type.kt`) instead of hard-coded font families.
- For Android view-based components (TextView), set the `fontFamily` via theme `textAppearance` or use `android:fontFamily="@font/google_sans_regular"`.
- If you changed font file names, make sure the resource names match the references in `Type.kt` (e.g. `R.font.google_sans_regular`).

Known issues & troubleshooting
--------------------------------
- INSTALL_PARSE_FAILED_NOT_APK on run/install
  - Symptom: Android Studio or adb fails to install with: `INSTALL_PARSE_FAILED_NOT_APK: Failed to parse .../base.apk: Failed to load asset path .../base.apk`.
  - Common causes & fixes:
    1. Build cache or incremental build produced a corrupted APK: run a clean build — `./gradlew clean :app:assembleDebug` — and try installing again.
    2. Conflicting prior installs: uninstall any previous package with the same applicationId: `adb uninstall com.example.cipherspend` before reinstalling.
    3. Android Studio / ADB confusion when the project path contains spaces or unusual characters. Try building from a path without spaces or use the direct `adb install` on the APK produced by Gradle.
    4. Corrupted build intermediates: delete `app/build/` and rebuild if the clean task alone doesn't help.
    5. If using an emulator/device with limited storage or permission restrictions, free up space and ensure the device accepts APKs from adb/Android Studio.
  - If you continue to see the issue, collect the full adb log (`adb logcat`) and Gradle build output and open an issue with the logs.

- SMS delivery differences across OEMs
  - On some devices (especially with aggressive battery or background restrictions) SMS broadcast delivery to `SmsReceiver` may be delayed or blocked. Test on target devices and guide users to allow background behavior where necessary.

Development notes for contributors
----------------------------------
- Architecture: Hilt + MVI patterns. See `core/mvi`, `ui/*` and `core/di` for entry points.
- Database: Room entities and DAOs live under `core/data/local`.
- SMS parsing rules: `core/sms/SmsParser.kt` contains the parsing engine; `res/xml/data_extraction_rules.xml` documents extraction rules.

How to help / Contributing
--------------------------
- Open issues and PRs: provide a short description, steps to reproduce, device/API level, and logs when relevant.
- Tests: add unit tests for parsing rules and viewmodel logic. The project already includes an example test under `app/src/test`.
- Security reviews: changes touching crypto or storage should include a security rationale and brief testing notes.

Acknowledgements & third-party libraries
----------------------------------------
- Jetpack Compose and Material 3
- Room + SQLCipher
- Hilt (Dagger)
- AndroidX Security & Biometric

License
-------
This repository does not currently contain a license file. Add a `LICENSE` in the repo root to make the project terms explicit.

Contact & support
------------------
For bugs or questions, open an issue in this repository and include device Android version, build output and relevant logs.

Files of interest (quick map)
-----------------------------
- App entry: `MainActivity.kt`, `CipherSpendApp.kt`
- Theme & typography: `ui/theme/Theme.kt`, `ui/theme/Type.kt`
- SMS processing: `core/sms/SmsReceiver.kt`, `core/sms/SmsParser.kt`
- Database & encryption: `core/data/local` and `core/di/DatabaseModule.kt`
- Security & biometrics: `core/security`

---
Thank you for using and contributing to Cipher. This v1.0 release is intentionally minimal and secure — your feedback on parsing accuracy and device compatibility is most welcome.
