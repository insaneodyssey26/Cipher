# cipher

A local-first, privacy-focused personal finance app for Android. cipher reads your bank SMS alerts and turns them into a clean, searchable transaction ledger — entirely on-device, with zero cloud dependency.

---

## Screenshots

| Onboarding | Permission | Dashboard |
|:-----------:|:---------:|:---------:|
| ![Onboarding](docs/screenshots/onboarding.png) | ![Permission](docs/screenshots/onboarding-permission.png) | ![Dashboard](docs/screenshots/dashboard.png) |

| Insights | Insights | Settings | Settings |
|:--------:|:--------:|:--------:|:--------:|
| ![Insights](docs/screenshots/insights.png) | ![Insights 2](docs/screenshots/insights-2.png) | ![Settings](docs/screenshots/settings-1.png) | ![Settings 2](docs/screenshots/settings-2.png) |

---

## How it works

```
Bank sends SMS alert
        │
        ▼
  SmsReceiver (BroadcastReceiver)
        │  raw message body
        ▼
    SmsParser
   ┌────────────────────────────────┐
   │  1. Regex: amount + direction  │
   │  2. Brand dict: merchant name  │
   │  3. Currency extraction (INR)  │
   └────────────────────────────────┘
        │  ParsedTransaction
        ▼
  CategorizerEngine
   assigns category (Food, Travel, UPI…)
        │
        ▼
  TransactionRepository
        │  TransactionEntity
        ▼
  Room + SQLCipher (AES-256 encrypted DB)
        │
        ▼
  DashboardViewModel ──► UI (Jetpack Compose)
```

No network call is made at any point. The SMS is read, parsed, and written to the encrypted database — all within the `SmsReceiver.onReceive` scope running on `Dispatchers.IO`.

---

## Architecture

cipher uses **MVI (Model-View-Intent)** across all screens, backed by Hilt DI.

Each screen follows the same contract pattern:

```
Screen.kt  ──intent──►  ViewModel  ──state──►  Screen.kt
                │                        ▲
                └──► Repository ──────────┘
```

### SMS pipeline

```mermaid
flowchart TD
    A([Bank SMS]) --> B[SmsReceiver]
    B --> C[SmsParser]
    C -->|not a transaction| D([dropped])
    C -->|ParsedTransaction| E[CategorizerEngine]
    E --> F[TransactionRepository]
    F --> G[(Room · SQLCipher)]

    classDef sys   fill:#0D0D1A,stroke:#4E6CF7,color:#EEEEF5
    classDef logic fill:#0D0D1A,stroke:#8585A0,color:#EEEEF5
    classDef store fill:#141420,stroke:#1AC47D,color:#EEEEF5
    classDef dead  fill:#0D0D1A,stroke:#E8453C,color:#8585A0

    class A,B sys
    class C,E,F logic
    class G store
    class D dead
```

### App layers

```mermaid
flowchart LR
    MA[MainActivity] --> OS[OnboardingScreen]
    MA --> LS[LockScreen]
    MA --> SCR[DashboardScreen]
    MA --> IS[InsightsScreen]
    MA --> SS[SettingsScreen]

    SCR --> DVM[DashboardViewModel]
    IS  --> IVM[InsightsViewModel]
    SS  --> SVM[SettingsViewModel]

    IVM --> SD[SubscriptionDetector]
    DVM --> TR[TransactionRepository]
    IVM --> TR
    SVM --> UP[UserPreferences]

    TR  --> DB[(Room · SQLCipher)]
    UP  --> PDS[(DataStore)]

    BW[BudgetWidget] --> TR
    SW[StatsWidget]  --> TR

    classDef entry  fill:#0D0D1A,stroke:#4E6CF7,color:#EEEEF5
    classDef screen fill:#0D0D1A,stroke:#4E6CF7,color:#EEEEF5
    classDef vm     fill:#0D0D1A,stroke:#8585A0,color:#EEEEF5
    classDef logic  fill:#0D0D1A,stroke:#8585A0,color:#EEEEF5
    classDef store  fill:#141420,stroke:#1AC47D,color:#EEEEF5
    classDef widget fill:#0D0D1A,stroke:#4E6CF7,color:#8585A0

    class MA entry
    class OS,LS,SCR,IS,SS screen
    class DVM,IVM,SVM vm
    class TR,UP,SD logic
    class DB,PDS store
    class BW,SW widget
```

---

## Features

### Automatic SMS parsing
- Listens for `SMS_RECEIVED` broadcasts from bank sender IDs
- Regex pipeline extracts: transaction amount, direction (debit/credit), merchant name, currency
- India-focused brand dictionary covers major UPI, credit card, and bank alert formats
- False-positive filtering rejects OTPs, promotional, and non-transactional messages

### Dashboard
- Running balance with income/expense split
- Transaction timeline with date grouping
- Live search by merchant or category
- Add, edit, delete with snackbar undo
- Monthly budget ring with health color coding
- Privacy mode — blurs all amounts with one tap

### Insights
- Spending velocity chart
- Category breakdown doughnut
- Calendar heatmap
- Day-detail drill-down
- Subscription detector — finds recurring payments and predicted next billing dates

### Security
- **Database**: SQLCipher AES-256 full-disk encryption
- **Biometric lock**: Fingerprint / face auth via `BiometricPrompt`; configurable auto-lock timeout
- **First-run onboarding**: Welcome + SMS permission gate before dashboard is accessible
- **Privacy mode**: All monetary values blurred on-screen

### Home screen widgets
- **BudgetWidget** — monthly spend vs. budget at a glance
- **StatsWidget** — today's income and expense summary

### Data portability
- **CSV export** — standard format, opens in any spreadsheet app
- **Encrypted backup / restore** — password-protected binary backup of the full database

---

## Data flow in detail

### SMS → Transaction

```
android.provider.Telephony.Sms.Intents.SMS_RECEIVED
    └─► SmsReceiver.onReceive()
            └─► SmsParser.parse(body: String): ParsedTransaction?
                    ├── amount regex        (e.g. "Rs. 450.00", "INR 1,200")
                    ├── direction keywords  (debited/credited/spent/received)
                    ├── merchant extraction (brand dict → fallback heuristics)
                    └── returns null for non-transactional messages
            └─► CategorizerEngine.classify(merchant): TransactionCategory
            └─► TransactionRepository.insertTransaction(TransactionEntity)
                    └─► TransactionDao.insert() → SQLCipher Room DB
```

### App launch → visible UI

```
MainActivity.onCreate()
    └─► UserPreferences.settingsFlow (DataStore)
            ├── hasCompletedOnboarding?
            │       NO  → show OnboardingScreen (blocks all input below it)
            │       YES → continue
            ├── isBiometricEnabled + BiometricAuthenticator.available?
            │       YES → show LockScreen → BiometricPrompt
            │       NO  → isAuthenticated = true immediately
            └─► NavHost renders: dashboard / insights / day_detail / settings
```

### UserPreferences (DataStore)

| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| `app_theme` | String | `SYSTEM` | Light / Dark / System |
| `biometric_enabled` | Boolean | `true` | Biometric lock on/off |
| `privacy_mode` | Boolean | `false` | Blur amounts |
| `haptics_enabled` | Boolean | `true` | Haptic feedback |
| `preferred_currency` | String | `INR` | Display currency |
| `auto_lock_timeout` | Long | `0` | ms before re-locking on resume |
| `last_stop_time` | Long | `0` | Used to compute lock grace period |
| `monthly_budget` | Double | `0.0` | Budget cap |
| `onboarding_completed` | Boolean | `false` | First-run gate |

---

## Module map

```
app/
└── src/main/java/com/masum/cipher/
    ├── MainActivity.kt               # Nav host, biometric gate, lifecycle lock
    ├── CipherSpendApp.kt             # Hilt application class
    │
    ├── core/
    │   ├── data/
    │   │   ├── local/
    │   │   │   ├── AppDatabase.kt    # Room + SQLCipher setup
    │   │   │   ├── dao/              # TransactionDao, MerchantAliasDao
    │   │   │   ├── entity/           # TransactionEntity, MerchantAliasEntity
    │   │   │   └── pref/             # UserPreferences, WidgetDataStore
    │   │   └── repository/           # TransactionRepository, BackupRepository
    │   ├── di/                       # Hilt modules (DatabaseModule)
    │   ├── domain/
    │   │   ├── CategorizerEngine.kt  # Merchant → category heuristics
    │   │   ├── SubscriptionDetector.kt
    │   │   └── model/                # ParsedTransaction, TransactionCategory
    │   ├── mvi/                      # MviBase (shared ViewModel base)
    │   ├── security/                 # BiometricAuthenticator, SecurityManager
    │   ├── sms/                      # SmsReceiver, SmsParser
    │   └── util/                     # Formatters
    │
    └── ui/
        ├── components/               # Shared composables, Charts, LockScreen
        ├── dashboard/                # DashboardScreen + ViewModel + Contract
        ├── insights/                 # InsightsScreen + DayDetailScreen + ViewModel
        ├── onboarding/               # OnboardingScreen (first-run)
        ├── privacy/                  # PrivacyPolicyScreen
        ├── settings/                 # SettingsScreen + ViewModel + Contract
        ├── theme/                    # Color, Typography, Theme
        └── widget/                   # BudgetWidget, StatsWidget + Receivers
```

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.1.10 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVI via `MviBase` |
| DI | Hilt |
| Database | Room 2.x + SQLCipher (AES-256) |
| Preferences | DataStore Preferences |
| Security | BiometricPrompt, androidx.security.crypto |
| Navigation | Navigation Compose |
| Widgets | Glance (AppWidget) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |
| Version | 2.2.0 (build 4) |

---

## Build

```bash
# Debug APK
./gradlew :app:assembleDebug

# Release APK (requires signing config)
./gradlew :app:assembleRelease
```

Open in Android Studio (Ladybug or newer). Compile SDK 35 required.

---

## Installing & granting SMS access

1. **Disable Play Protect** (temporary): Play Store → Profile → Play Protect → Settings → turn off *Scan apps with Play Protect*.
2. **Allow unknown sources** for your installer app: Settings → Apps → Special app access → Install unknown apps → select installer → Allow from this source.
3. **Install** the APK via file manager or `adb install path/to/app.apk`.
4. **Grant SMS permission** when prompted on first launch.
   - If the permission appears restricted: Settings → Apps → Special app access → look for *Allow access to restricted settings* → select cipher → enable → return and grant.
5. Re-enable Play Protect.

---

## Privacy

cipher requests exactly one permission: `RECEIVE_SMS`. It has **no INTERNET permission**. There is no telemetry, no analytics SDK, no crash reporter, and no account system. All data — transactions, preferences, backups — stays on your device.

---

## Release history

See [RELEASE_NOTES.md](RELEASE_NOTES.md).
