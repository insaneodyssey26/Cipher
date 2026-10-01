# Contributing to Cipher

Thanks for helping make Cipher better. Cipher is a private, fully offline finance tracker, so a few rules matter more here than in a typical app. Please read this before opening a pull request.

## The privacy promise

Cipher keeps all financial data on the device. Pull requests that break this will not be merged.

- No analytics, telemetry, crash reporters, ads or tracking SDKs.
- No new network calls. The only network use is Pro license validation and Google Play update checks.
- Never log transaction content, SMS bodies, license keys or anything personal.
- Never commit secrets: keystores, passwords, API keys or tokens.

## Getting started

1. Install the latest stable Android Studio.
2. Clone the repository and open it. Gradle downloads the pinned JDK 21 on its own.
3. Use the `debug` build type. It installs as `com.masum.cipher.debug` next to the Play Store app and never touches its data.

```
./gradlew assembleDebug            # build the debug app
./gradlew testDebugUnitTest        # run unit tests
./gradlew assembleRelease          # build with R8, as the CI does
./gradlew lintDebug                # run Android lint
./gradlew connectedDebugAndroidTest  # device tests (needs a device or emulator)
```

The CI runs the unit tests, lint and the release build on every pull request. Please run them locally first.

## How the code is organised

- `core/` holds the engine: `data` (Room, DataStore, repositories), `domain` (use cases and rules), `sms` and `notifications` (capture and parsing), `security`, `worker`.
- `ui/` holds the screens, built with Jetpack Compose and Material 3.
- Each screen follows the same pattern: a `Contract` (state, intent, effect), a `ViewModel` built on `BaseViewModel`, and a composable. Look at `ui/dashboard` for an example.
- Dependencies are injected with Hilt.

## Code style

- Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- **We do not use code comments.** Write code that explains itself through clear names and small functions, and put the reasoning in your commit message.
- **Never hard-code user-facing text.** Add it to `app/src/main/res/values/strings.xml` and use `stringResource`. Use `plurals` for anything with a count.
- Add translations for the languages in `res/values-*` when you add or change text. If you cannot translate a language, say so in the pull request and we will help.
- Prefer small, pure functions that can be unit tested over logic inside composables.

## Database changes

Cipher stores user data in an encrypted Room database, so schema mistakes can lose data.

1. Bump the version in `AppDatabase`.
2. Write a `Migration` and register it in `DatabaseModule`. Never use destructive migration.
3. Commit the new file Room generates in `app/schemas/`.
4. Add a migration test next to `AppDatabaseSchemaTest`.

## Bank message parsers

Region rules live in `core/sms/region`. When you add or change a pattern:

- Add test cases to `TransactionParserTest` or the region test for that country.
- Use made-up numbers, names and account digits. Never paste a real message.
- Check that OTP, promotional and balance-only messages are still ignored.

## Tests

- New logic needs unit tests. Bug fixes should include a test that fails without the fix.
- Prefer fakes over mocks where possible.
- Anything that depends on real SQL or on the screen belongs in `androidTest`.

## Commits and pull requests

- Use [Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`, `perf:`, `refactor:`, `test:`, `docs:`, `build:`, `ci:`.
- Keep commits focused and explain why in the body.
- Keep pull requests small. Describe what changed, why, and how you tested it. Add screenshots for UI changes, in dark and light themes if relevant.
- Make sure CI passes before asking for review.

## Reporting security issues

Please do not open a public issue for a vulnerability. Use GitHub's private vulnerability reporting under the repository's **Security** tab instead.

## License

By contributing you agree that your work is released under the [GNU General Public License v3.0](LICENSE).
