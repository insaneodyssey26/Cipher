# Release Notes

## [4.6.0]

### Added
- **Modern Home Screen Widgets**: Complete visual overhaul of `Budget Progress` and `Monthly Overview` home screen Glance widgets with dynamic dark/light theme support, user accent color synchronization, and high-precision touch handling for instant manual refresh.
- **Launcher Widget Previews**: Added dedicated launcher picker preview cards (`widget_preview_budget_layout` & `widget_preview_stats_layout`) so widgets render accurate visual previews when added to the home screen.
- **Precision Numpad Cursor Control**: Enhanced manual transaction entry and editing sheets to allow tap-to-place cursor positioning anywhere in numbers with soft keyboard suppression and selection-aware insertion/deletion.

### Changed
- **Target SDK Upgrade**: Updated `targetSdk` and `compileSdk` to **37 (Android 17)** to meet Google Play's latest target API level requirements.
- **Insights Render Performance**: Optimized `CategoryAllocationDonut` with `@Immutable` data stability and cached calculations, eliminating unnecessary recompositions and frame drops during scrolling.
- **Haptic Feedback**: Added responsive tactile haptic vibration to the Activity Calendar month navigation buttons on the Insights screen.

## [4.5.0]

### Added
- **Dashboard Scrolling**: The main dashboard header now scales and pins to the top as you scroll.
- **Big Number Formatting**: Balances over 10k dynamically scale their font size to prevent overflow clipping.
- **Custom Native Charts**: Built native Compose Canvas implementations for the Spending Trend, Peak Hours, and Category Allocation charts.
- **Calendar Paging**: The Activity Heatmap is now split and paginated by month.
- **Weekly Filters**: Added a 'This Week' filter option to the transaction dropdown.

### Changed
- **Dropped Vico**: Removed the `vico` charting dependency entirely to reduce APK size and improve render performance.
- **State Management**: Switched all UI state collection to `collectAsStateWithLifecycle` to prevent background memory leaks.
- **Library Updates**:
  - Kotlin: 2.1.10 → 2.4.10
  - Compose BOM: 2025.01.01 → 2026.06.01
  - AGP: 8.8.0 → 8.12.0
  - Room: 2.6.1 → 2.8.4
  - Hilt: 2.55 → 2.57.1
  - KSP: 2.1.10-1.0.29 → 2.3.10
  - Kotlinx Serialization: 1.7.3 → 1.8.0

## [4.2.0]

### Added
- **Automated Backups**: You can now configure Cipher to automatically backup your encrypted database to a local folder or cloud-synced directory on a regular schedule.
- **Inline Calculator**: Need to split a bill? You can now perform math directly inside the amount field when adding or editing transactions.
- **Onboarding Personalization**: New users can now select their preferred Accent Color directly during the first-run onboarding flow.

### Changed
- **Fluid Keyboard Padding**: Bottom sheets now dynamically slide up when the system keyboard appears, preventing input fields from ever being hidden.
- **Massive Balance Handling**: Enhanced the dashboard UI to gracefully handle and truncate astronomically large balances without breaking the layout.

---

## [4.1.0]

### Added
- **App Notification Parsing**: Utilizes `NotificationListenerService` to capture and parse transaction alerts from finance and UPI applications.
- **Target App Management**: Users can explicitly select which applications Cipher should monitor for transaction notifications.
- **Custom Accent Colors**: Introduced dynamic theming options allowing users to personalize the app's primary accent color.
- **Permissions Health Dashboard**: Diagnostic interface in Settings to verify and resolve missing permissions (SMS, Notification Access).
- **Interactive App Tour**: A swipeable guide integrated into the onboarding flow to educate new users on core functionality.
- **Play Store Link**: Added direct intent to review the application on Google Play.
- **Developer Contact**: Added email intent for direct support requests.

### Changed
- **Unified Parsing Engine**: Expanded `SmsParser` to process both SMS and push notification payloads seamlessly.
- **Settings Reorganization**: Restructured the About section into distinct Support and Application information groups.
- **Dark Mode Legibility**: Adjusted typography weights and updated secondary text colors to `onSurfaceVariant` to improve contrast in dark themes.
- **Privacy Policy**: Updated in-app and documentation text to reflect the usage of `POST_NOTIFICATIONS` for local budget alerts.

---

## [4.0.0]

### Added
- **Major Redesign**: Complete visual overhaul with premium colors, typography, and component styling.
- **Floating Navigation Bar**: A sleek, pill-shaped centered navigation bar for main app routing.
- **Advanced Insights Charts**: Integrated high-performance custom Canvas-based charts for fluid spending velocity charts and enhanced data visualization.
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
