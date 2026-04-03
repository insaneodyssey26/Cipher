# Release Notes

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
