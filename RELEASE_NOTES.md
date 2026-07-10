# Release Notes

## [4.1.0]

### Added
- **App Notification Parsing**: Cipher now uses `NotificationListenerService` to intelligently capture and parse transaction alerts directly from explicitly tracked finance and UPI apps (PhonePe, GPay, Paytm, etc.). This acts as a powerful alternative/fallback for banks that send app alerts instead of SMS!
- **Permissions Health Dashboard**: New diagnostic tool in Settings to check SMS, Notification Listener, and App Notification permissions with 1-click fixes to open Android system settings.
- **Interactive App Tour**: A polished, swipeable 3-page carousel added to the end of Onboarding to guide new users through core features (Dashboard, Manual Adding, and Insights).
- **Play Store Rating**: Added a "Rate on Google Play" button that natively launches the Play Store.
- **Contact Developer**: Added a one-tap email intent button for direct support requests.

### Changed
- **Unified Parsing Engine**: Upgraded `SmsParser` to handle both raw SMS messages and Android push notification payloads seamlessly.
- **Settings Reorganization**: Split the massive "About" block into structured "Support & Feedback" and "About Cipher" groups for easier scanning.
- **Dark Mode Legibility**: Upgraded typography weights and boosted contrast (using `onSurfaceVariant`) for subtitles across the entire Settings screen to ensure they look crisp in Dark Mode.
- **Updated Privacy Policy**: Explicitly updated in-app and README documentation to detail the new `POST_NOTIFICATIONS` permission requirement for Android 13+ budget alerts.

---

## [4.0.0]

### Added
- **Major Redesign**: Complete visual overhaul with premium colors, typography, and component styling.
- **Floating Navigation Bar**: A sleek, pill-shaped centered navigation bar for main app routing.
- **Advanced Insights Charts**: Integrated the high-performance Vico library for fluid spending velocity charts and enhanced data visualization.
- **Scrollable Activity Calendar**: Upgraded the calendar heatmap to support horizontal scrolling with clear month headers and today-highlighting.
- **Promotional SMS Detection**: Intelligent filtering to automatically identify and drop promotional texts and spam.
- **Transaction Sorting**: Added capability to sort transactions by spend amount directly on the dashboard.
- **Informal SMS Support**: Updated parsing rules to detect manual test messages (e.g. "sent 1000 rs") for easier manual testing.
- **Sponsors Page**: Added a section to recognize project sponsors.

### Changed
- **Architecture Upgrade**: Decoupled `MainActivity` into `MainViewModel` & `MainContract`, and moved business logic into a dedicated `UseCase` layer.
- **Pattern Externalization**: Extracted hardcoded regex, keywords, and brand mappings from `SmsParser` and `CategorizerEngine` into centralized configuration objects.
- **Robust SMS Processing**: Upgraded `SmsReceiver` to utilize `goAsync()` for highly reliable background database writes.
- **Empty State UX**: Implemented a smooth loading indicator on app launch to prevent jarring flashes of the empty state screen.

### Fixed
- **Status Bar Overlap**: Corrected system window insets padding on the onboarding screens so content no longer clips under the device status bar.
- **Categorizer Edge Cases**: Resolved edge cases in the categorization engine and expanded test coverage.

---

## [3.0.0]

### Added
- **First-run onboarding**: Two-screen welcome flow with SMS permission gate — new users no longer land cold on the dashboard.
- **Home screen widgets**: BudgetWidget shows monthly spend vs. budget at a glance; StatsWidget shows monthly income and expense summary.
- **Privacy Policy screen**: Dedicated in-app screen explaining data handling and permissions.
- **Feedback form**: In-app feedback submission.
- **Investment category**: New transaction category with dedicated icon for tracking investments and SIPs.
- **More metrics in Insights**: Additional spending patterns and trend data on the intelligence screen.
- **49 unit tests**: 42 core SMS parsing tests + 7 additional tests covering rupee symbol variants — all passing.
- **Install guide**: Dedicated [INSTALL.md](INSTALL.md) with step-by-step sideload instructions including the Android 13+ restricted SMS permission setup.

### Changed
- **SMS parsing accuracy**: Edited exclusion keyword list to reduce false positives; fixed edge cases in debit/credit classification; added 8 new brands to the merchant dictionary.
- **Transaction icons**: Category icons replace merchant initials in the transaction list for faster visual scanning.
- **Dashboard UI**: Removed blue accent patterns from stat pills, the intelligence row, and the add button — color only appears where it carries meaning (income green, expense red).
- **New app icon**: Replaced previous icon with updated design.
- **Smoother animations**: Refined animation specs across the app to reduce jank.
- **Brand casing**: App name rendered as lowercase "cipher" consistently across all screens.

### Fixed
- **Widget race condition**: Fixed a race condition in widget state updates that could cause stale data to appear.
- **Merchant alias bug**: Fixed incorrect alias resolution for certain merchants.
- **Font fallback**: Fixed a crash/rendering issue caused by missing font fallback configuration.
- **Package name**: Corrected package name inconsistency introduced in an earlier build.
- **BudgetCard rendering**: Removed double-background bug causing a visible overlay artifact on the card border.
- **Biometric gate**: Lock screen no longer triggers during first-run onboarding.
- **Onboarding click-through**: Dashboard buttons no longer respond to taps while onboarding is visible.

---

## [2.1.0]

### Fixed
- **Encrypted Backups**: Resolved a critical issue where the app's auto-lock would prevent the password dialog from appearing during import/export.
- **Biometric Persistence**: Improved UI architecture to preserve app state and pending actions across biometric unlock cycles.

---

## [2.0.1]

### Fixed
- **App Launcher**: Fixed an issue where the icon appeared as a white mascot on some devices; updated to a custom blue vault logo.

---

## [2.0.0]

### Added
- **Subscription Intelligence**: Offline engine for detecting recurring payments and predicting billing cycles.
- **Monthly Budgeting**: Spending limit configuration with real-time tracking on the dashboard.
- **Manual Ledger Management**: Full CRUD support for adding, editing, and deleting transactions.
- **Undo System**: Recovery mechanism for accidental transaction deletions.
- **Data Portability**: CSV export functionality for external financial analysis.
- **Tactile Feedback**: Haptic feedback with a dedicated toggle in settings.

### Changed
- **Header Redesign**: Shifted to a bolder, left-aligned title for improved readability.
- **Enhanced Search**: Reactive filtering system for merchants and categories.
- **Parsing Logic**: Heuristic improvements to reduce false positives from non-transactional messages.
- **Performance**: Optimized animation specs and list rendering to reduce frame drops.

### Fixed
- **Biometric Handling**: Resolved blank screen issues during app resume; implemented dedicated lock screen UI.
- **Lifecycle Logic**: Fixed auto-lock timer inconsistencies during rapid app switching.
---

## [1.0.0]
- Initial release.
- Automated SMS parsing.
- Encrypted local storage (SQLCipher).
- Biometric authentication.
- Core Dashboard and Insights.
