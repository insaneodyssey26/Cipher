# Cipher — Release (v1.0)

Cipher v1.0 — first public release (initial APK).

Short summary
---------------
Cipher is a privacy-first, local-only personal finance app that extracts transaction data from SMS alerts and stores everything encrypted on-device. This release delivers the initial, production-ready foundation: automated SMS parsing, encrypted storage, biometric options, and a polished Compose UI.

Highlights
----------
- Automatic on-device SMS parsing (merchant, amount, currency)
- Encrypted local storage (Room + SQLCipher)
- Optional biometric gating for sensitive screens
- Material 3 Compose UI with bundled Google Sans typography
- No network permissions — data stays on your device

Important notes for users
-------------------------
- Minimum Android: 7.0 (API 24). Target/compile SDK: 35.
- Runtime permissions: grant RECEIVE_SMS and READ_SMS for automatic parsing.
- This is the first APK release — if you encounter install or runtime issues, please check the full technical notes in the repo (link below) before filing an issue.

Where to find more
-------------------
For installation steps, full troubleshooting, developer notes, and detailed security/privacy information see: `docs/RELEASE_NOTES_FULL.md` (long-form release notes and developer guidance).

Copy-paste for GitHub Releases
------------------------------
Use this short release body when creating the GitHub release page. Keep the release description focused — link to `docs/RELEASE_NOTES_FULL.md` for details.



