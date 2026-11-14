# Privacy Policy for Cipher

**Effective Date:** September 19, 2026

Cipher is developed by Sk Masum Ali ("I", "the developer"). This Privacy Policy explains how Cipher handles, processes, and safeguards your financial information.

**By design, Cipher is a local-first, privacy-first personal finance application. All your financial transactions, accounts, and budgets are processed and stored 100% offline on your device. Cipher does not operate data collection servers, does not track your financial activities, and does not sell or share your information.**

## 1. Information Accessed

Cipher requests specific Android permissions solely to function as an automated financial ledger on your device:

*   **SMS Permissions (`RECEIVE_SMS`, `READ_SMS`)**: Requested solely to read incoming transaction alerts from your bank or financial institutions on-device. Messages are processed as they arrive and are never uploaded.
*   **Notification Listener Service (`BIND_NOTIFICATION_LISTENER_SERVICE`)**: This optional permission allows Cipher to read notifications from specific financial and UPI apps that you explicitly select to capture transactions sent via app notifications instead of SMS.

## 2. How Your Information is Handled

The SMS messages, notifications, and transactions accessed by Cipher are processed **strictly locally on your device**:
*   **Local Parsing**: Incoming SMS messages and selected app notifications are parsed on-device by Cipher's internal rule engine to extract transaction details (e.g., amount, merchant, and timestamp).
*   **Zero Financial Network Transmission**: Cipher does not transmit, upload, sync, or back up your SMS data, notification content, transaction records, or any financial balances to the cloud or any third-party services.
*   **Encrypted Storage**: Once parsed, your transaction records and accounts are stored locally on your device in a securely encrypted database (AES-256 via SQLCipher).
*   **Home-Screen Widgets**: If you add Cipher widgets, the balances, budget figures and recent transactions they display are cached in the app's private storage so the widgets can render. This cache is private to the app but is not part of the encrypted database.
*   **Encrypted Backups & Exports**: If you configure the auto-backup feature or manually export your data, Cipher saves password-encrypted backup files or statements strictly to a local directory or folder you choose via Android's native Storage Access Framework. Cipher has no remote access to these files.

## 3. Network Access

Cipher requires internet access solely for:
*   **Google Play Billing & Updates**: Cipher Pro purchases and subscriptions are handled by Google Play. Cipher receives only the purchase status from Google Play (such as the product and purchase token) to unlock Pro features, and never sees your payment details. Google's own privacy policy applies to these services, including in-app update checks on Google Play installs.
*   **App Update Checks (GitHub builds only)**: Builds installed outside Google Play check the GitHub Releases API (`api.github.com`) about once a day for a newer version. As with any web request, GitHub can see your IP address.
*   **Zero Financial Transmission**: No financial data, bank names, account numbers, transactions, notes, or balances are ever transmitted.
*   **Zero Trackers & Ads**: Cipher contains zero advertising SDKs, zero third-party telemetry, and zero user profiling trackers.

## 4. Data Retention and Deletion

*   **Financial Data**: Stored 100% locally on your device. Uninstalling the app or clearing app storage permanently deletes all database records and encryption keys immediately.
*   **Purchases**: Handled by Google Play. Cipher does not operate a license server and does not store your email address or billing details.
*   **Earlier Purchases**: Before the move to Google Play, Pro was sold through a separate payment provider with a license server. Records held for those earlier purchases (license key, plan, order ID, billing email and activated device IDs) can be deleted on request by contacting me directly or opening an issue on GitHub.

## 5. Security & Official Distribution Guarantees

*   **On-Device Protection**: Your financial data is protected using Android's native `BiometricPrompt` (requiring fingerprint, face, or device PIN) and encrypted on-disk using AES-256 encryption via SQLCipher.
*   **Official Build Guarantees**: The zero-tracking, local parsing, and encryption guarantees described in this Privacy Policy apply strictly to authentic builds installed via the **Google Play Store** or **[Official GitHub Releases](https://github.com/insaneodyssey26/cipher/releases)**. The developer cannot guarantee privacy or security protections on unofficial, modified, or third-party repackaged APKs.

## 6. Changes to This Privacy Policy

This Privacy Policy may be updated from time to time to reflect new app features. When updated, the "Effective Date" at the top will be revised accordingly.

## 7. Contact the Developer

If you have any questions, feedback, or suggestions regarding this Privacy Policy or Cipher, feel free to open an issue on the [GitHub repository](https://github.com/insaneodyssey26/cipher) or contact me directly at [skmasumali.dev@gmail.com](mailto:skmasumali.dev@gmail.com).
