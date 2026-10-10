# Privacy Policy for Cipher

**Effective Date:** September 19, 2026

Cipher is developed by Sk Masum Ali ("I", "the developer"). This Privacy Policy explains how Cipher handles, processes, and safeguards your financial information.

**By design, Cipher is a local-first, privacy-first personal finance application. All your financial transactions, accounts, and budgets are processed and stored 100% offline on your device. Cipher does not operate data collection servers, does not track your financial activities, and does not sell or share your information.**

## 1. Information Accessed

Cipher requests specific Android permissions solely to function as an automated financial ledger on your device:

*   **SMS Permission (`RECEIVE_SMS`)**: Requested solely to read incoming transaction alerts from your bank or financial institutions on-device.
*   **Notification Listener Service (`BIND_NOTIFICATION_LISTENER_SERVICE`)**: This optional permission allows Cipher to read notifications from specific financial and UPI apps that you explicitly select to capture transactions sent via app notifications instead of SMS.

## 2. How Your Information is Handled

The SMS messages, notifications, and transactions accessed by Cipher are processed **strictly locally on your device**:
*   **Local Parsing**: Incoming SMS messages and selected app notifications are parsed on-device by Cipher's internal rule engine to extract transaction details (e.g., amount, merchant, and timestamp).
*   **Zero Financial Network Transmission**: Cipher does not transmit, upload, sync, or back up your SMS data, notification content, transaction records, or any financial balances to the cloud or any third-party services.
*   **Encrypted Storage**: Once parsed, your transaction records and accounts are stored locally on your device in a securely encrypted database (AES-256 via SQLCipher).
*   **Encrypted Backups & Exports**: If you configure the auto-backup feature or manually export your data, Cipher saves password-encrypted backup files or statements strictly to a local directory or folder you choose via Android's native Storage Access Framework. Cipher has no remote access to these files.

## 3. Network Access & Licensing

Cipher requires internet access solely for:
*   **License Key Verification & Device Management**: When you activate or deactivate a Cipher Pro product key or manage your device activations, the app contacts the license verification endpoint (`https://cipher-license-api.skmasumali-main.workers.dev`).
*   **Data Transmitted for Licensing**: The request transmits only your license key, a pseudonymous device identifier (derived from device hardware properties and stored securely), your device model name (to help you identify devices on your 3-device seat limit), and your email address if provided during purchase or activation.
*   **Zero Financial Transmission**: No financial data, bank names, account numbers, transactions, notes, or balances are ever transmitted.
*   **Zero Trackers & Ads**: Cipher contains zero advertising SDKs, zero third-party telemetry, and zero user profiling trackers.

## 4. License Server Data Retention and Deletion

*   **Financial Data**: Stored 100% locally on your device. Uninstalling the app or clearing app storage permanently deletes all database records and encryption keys immediately.
*   **Licensing Records**: License records (license key, tier, order ID, optional billing email, active device IDs, and activation timestamps) are stored securely in Cloudflare KV to enforce multi-device limits and preserve your license entitlements across device migrations.
*   **Data Deletion**: You may request complete removal of your billing email and device association records at any time by contacting me directly or opening an issue on GitHub. Revoking a device directly from the app immediately removes that device ID from active license records.

## 5. Security & Official Distribution Guarantees

*   **On-Device Protection**: Your financial data is protected using Android's native `BiometricPrompt` (requiring fingerprint, face, or device PIN) and encrypted on-disk using AES-256 encryption via SQLCipher.
*   **Official Build Guarantees**: The zero-tracking, local parsing, and encryption guarantees described in this Privacy Policy apply strictly to authentic builds installed via the **Google Play Store** or **[Official GitHub Releases](https://github.com/insaneodyssey26/cipher/releases)**. The developer cannot guarantee privacy or security protections on unofficial, modified, or third-party repackaged APKs.

## 6. Changes to This Privacy Policy

This Privacy Policy may be updated from time to time to reflect new app features. When updated, the "Effective Date" at the top will be revised accordingly.

## 7. Contact the Developer

If you have any questions, feedback, or suggestions regarding this Privacy Policy or Cipher, feel free to open an issue on the [GitHub repository](https://github.com/insaneodyssey26/cipher) or contact me directly at [skmasumali.dev@gmail.com](mailto:skmasumali.dev@gmail.com).
