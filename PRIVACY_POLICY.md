# Privacy Policy for Cipher

**Effective Date:** September 19, 2026

Cipher ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how our application handles, processes, and safeguards your financial information. 

**By design, Cipher is a local-first, privacy-first personal finance application. All your financial transactions, accounts, and budgets are processed and stored 100% offline on your device. We do not operate data servers, we do not collect your financial data, and we do not sell your information.**

## 1. Information We Access

Cipher requests specific permissions solely to function as an automated financial ledger:

*   **SMS Permission (`RECEIVE_SMS`)**: We request this permission solely to read incoming transaction alerts from your bank or financial institutions.
*   **Notification Listener Service (`BIND_NOTIFICATION_LISTENER_SERVICE`)**: This optional permission allows Cipher to read notifications from specific financial and UPI apps that you explicitly select to capture transactions sent via app notifications instead of SMS.

## 2. How We Use Your Information

The SMS messages, notifications, and transactions accessed by Cipher are processed **strictly locally on your device**:
*   **Local Parsing**: Incoming SMS messages and selected app notifications are parsed on-device by Cipher's internal rule engine to extract transaction details (e.g., amount, merchant, and timestamp).
*   **Zero Financial Network Transmission**: We do not transmit, upload, sync, or back up your SMS data, notification content, transaction records, or any financial balances to the cloud or any third-party services.
*   **Encrypted Storage**: Once parsed, your transaction records and accounts are stored locally on your device in a securely encrypted database (AES-256 via SQLCipher).
*   **Encrypted Backups & Exports**: If you configure the auto-backup feature or manually export your data, Cipher saves password-encrypted backup files or statements strictly to a local directory or folder you choose via Android's native Storage Access Framework. Cipher has no remote access to these files.

## 3. Network Access & Licensing

Cipher uses network communication exclusively for:
*   **License Key Verification**: When you activate or deactivate a Cipher Pro product key, the app contacts our secure license verification endpoint to validate your license and allocate your 3-device quota using an anonymous device identifier. No financial data, account numbers, or personal transaction histories are ever included in this request.

Cipher contains **zero** third-party analytics trackers, **zero** advertising SDKs, and **zero** user profiling telemetry.

## 4. Data Retention and Deletion

All financial data is stored locally on your device. You have complete control over your data:
*   You can edit or delete individual transactions within the app at any time.
*   You can wipe all data by selecting "Clear All Data" in Settings or clearing app storage via Android Settings.
*   Uninstalling the app permanently removes all local database records.

## 5. Security

Your financial data is protected using Android's native `BiometricPrompt` (requiring fingerprint, face, or device PIN) and encrypted on-disk using AES-256 encryption.

## 6. Changes to This Privacy Policy

We may update our Privacy Policy from time to time to reflect new app features. When updated, we will revise the "Effective Date" at the top of this policy.

## 7. Contact Us

If you have any questions or suggestions regarding this Privacy Policy, please open an issue or reach out via our GitHub repository.
