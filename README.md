# Cipher

Cipher is a local-first, privacy-first personal finance companion for Android. It securely extracts transaction information from SMS alerts, stores data encrypted on-device, and presents a modern Compose-based dashboard and insights UI.

See the full technical release notes and developer guidance in `docs/RELEASE_NOTES_FULL.md`.

Quick start
-----------

- Open the project in Android Studio (compileSdk 35, minSdk 24).
- Build and run on a device or emulator (API 24+).

Command-line (fish / Linux / WSL):

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Notes
-----
- The package id remains `com.example.cipherspend` (internal project/package names not renamed).
- User-facing strings and theme styling use "Cipher" as the display name.

For full technical details, known issues and developer notes, see `docs/RELEASE_NOTES_FULL.md`.

