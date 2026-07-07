# Privacy Policy for Cipher

**Effective Date:** July 7, 2026

Cipher ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how our application collects, uses, and safeguards your information. 

**By design, Cipher is a 100% offline, privacy-first application. We do not have servers, we do not collect your data, and we do not sell your information.**

## 1. Information We Access

Cipher requests specific permissions to function as an automated financial vault. The primary permission requested is:

*   **SMS Permission (`RECEIVE_SMS`)**: We request this permission solely to read incoming text messages from your bank or financial institutions. 

## 2. How We Use Your Information

The SMS messages accessed by Cipher are processed **strictly locally on your device**:
*   **Local Parsing**: Incoming SMS messages are read by the app's internal engine to extract transaction details (e.g., amount, merchant, and date).
*   **Zero Network Transmission**: We do not transmit, upload, sync, or back up your SMS data, transaction data, or any personal information to the cloud, our servers, or any third-party services. Cipher does not even request the Android `INTERNET` permission.
*   **Encrypted Storage**: Once parsed, your transaction data is stored locally on your device in a securely encrypted database (AES-256 via SQLCipher). 

## 3. Third-Party Access

Because Cipher is entirely offline and has no internet access, it is technically impossible for us to share your data with third parties. There are no analytics trackers, no crash reporters, and no advertising SDKs bundled with the application.

## 4. Data Retention and Deletion

All data is stored locally on your device. You have complete control over your data:
*   You can delete individual transactions within the app.
*   You can wipe all data by clearing the app's storage via Android Settings.
*   Uninstalling the app will permanently delete all your transaction history (unless you have manually created a local, encrypted backup file).

## 5. Security

We take security seriously. Your financial data is secured using Android's native `BiometricPrompt` (requiring your fingerprint, face, or device PIN to open the app) and stored in an encrypted database. However, please remember that the security of your data also depends on maintaining the physical security of your device and keeping your device PIN private.

## 6. Changes to This Privacy Policy

We may update our Privacy Policy from time to time. If we make material changes, we will update the "Effective Date" at the top of this policy and provide notice within the app.

## 7. Contact Us

If you have any questions or suggestions about this Privacy Policy, please open an issue on our GitHub repository.
