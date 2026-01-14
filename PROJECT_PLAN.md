# CipherSpend: Development Roadmap

A local-first, hardware-encrypted financial management system built with modern Android standards.

## Phase 1: The Cryptographic Vault (Security & Data)
- **Keystore Integration:** Implementation of a hardware-backed AES-256 key generation system.
- **SQLCipher Setup:** Integration of Room with SQLCipher for at-rest database encryption.
- **Schema Design:** Defining the `Transaction` and `Account` entities with a focus on extensibility.

## Phase 2: The Neural Parser (SMS & Logic)
- **SmsReceiver:** A lightweight, secure BroadcastReceiver to capture incoming financial alerts.
- **Regex Intelligence Engine:** A local-only parsing system to extract Amount, Merchant, and Currency with high precision.
- **Data Persistence:** Immediate, encrypted storage of parsed data via Room.

## Phase 3: The MVI Backbone (Architecture)
- **Dependency Injection:** Robust Hilt setup for clean scoping and testability.
- **MVI Foundation:** Setting up the `BaseViewModel`, `ViewState`, `Intent`, and `Reducer` patterns.
- **Repository Pattern:** Reactive data streams using Kotlin Coroutines and Flow.

## Phase 4: The Expressive Interface (UI/UX)
- **Design Tokens:** Implementing a Material 3 Design System (Typography, Shapes, and "Expressive" color palettes).
- **Core Components:** Building reusable, premium-feel Compose components (Glassmorphism, subtle gradients, fluid haptics).
- **Dashboard Feature:** A high-performance, jank-free main view observing the MVI state.

## Phase 5: Polish & Motion
- **Shared Element Transitions:** Seamless navigation between transaction lists and details.
- **Micro-interactions:** Fine-tuned animations for state changes and data entries.
- **Privacy Controls:** Biometric integration for app entry and sensitive data masking.

---

### Strict Development Principles
- **No Comments:** Code must be self-documenting through clear naming and structure.
- **Quality First:** Incremental, well-tested commits.
- **Zero Latency:** All operations must feel instantaneous to the user.
- **Absolute Privacy:** No network permissions; data never leaves the device.