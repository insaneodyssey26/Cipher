# Release Notes

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
