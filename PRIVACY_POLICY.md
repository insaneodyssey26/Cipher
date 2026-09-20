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

Cipher requires internet access solely for:
*   **License Key Verification & Device Management**: When you activate or deactivate a Cipher Pro product key or manage your device activations, the app contacts our secure license verification backend (`https://cipher-license-api.skmasumali-main.workers.dev`).
*   **Data Transmitted for Licensing**: The request transmits only your license key, a pseudonymous device identifier (derived from device hardware properties and stored securely), your device model name (to help you identify devices on your 3-device seat limit), and your email address if provided during purchase or activation.
*   **Zero Financial Transmission**: No financial data, bank names, account numbers, transactions, notes, or balances are ever transmitted.
*   **Zero Trackers & Ads**: Cipher contains zero advertising SDKs, zero third-party telemetry, and zero user profiling trackers.

## 4. License Server Data Retention and Deletion

*   **Financial Data**: Stored 100% locally on your device. Uninstalling the app or clearing app storage permanently deletes all database records and encryption keys immediately.
*   **Licensing Records**: License records (license key, tier, order ID, optional billing email, active device IDs, and activation timestamps) are stored in Cloudflare KV to enforce multi-device limits and preserve your license entitlements across device migrations.
*   **Data Deletion**: Users may request complete removal of their billing email and device association records at any time by contacting support or opening an issue on GitHub. Revoking a device directly from the app immediately removes that device ID from active license records.

## 5. Security

Your financial data is protected using Android's native `BiometricPrompt` (requiring fingerprint, face, or device PIN) and encrypted on-disk using AES-256 encryption.

## 6. Changes to This Privacy Policy

We may update our Privacy Policy from time to time to reflect new app features. When updated, we will revise the "Effective Date" at the top of this policy.

## 7. Contact Us

If you have any questions or suggestions regarding this Privacy Policy, please open an issue or reach out via our GitHub repository.
