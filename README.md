# InOffice

Android app for tracking how many days you are **in office**, on **leave**, or on **holiday** against a configurable monthly mandate. Data is stored locally (Room + DataStore) and synced to Google Drive App Data for signed-in users.

## Features

- **Dashboard** — Progress toward the monthly in-office target, with breakdown by base mandate, leave, and holidays.
- **Day marks** — Mark today as in office, leave, holiday, or clear the mark.
- **Month report** — Browse prior/next months and see a list of marked days.
- **Settings** — Set the base in-office mandate per month, view sync status, run sync now, and sign out.
- **Google account gate** — Sign-in is required before entering the app.
- **Cloud sync** — Debounced background sync to Google Drive App Data with retry and conflict handling.

## Requirements

- **JDK 17** (matches Gradle `jvmTarget` and CI).
- **Android Studio** Koala or newer recommended (AGP 8.7.x, Kotlin 2.0, Compose).

## Tech stack

- Kotlin, Jetpack Compose (Material 3), Navigation Compose  
- Hilt (DI), Room (persistence), DataStore Preferences (settings)  
- `minSdk` 26, `compileSdk` / `targetSdk` 35  

Version catalogs and dependency versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Google OAuth and Drive setup

InOffice uses Google Sign-In plus Google Drive App Data scope:

- OAuth scope: `https://www.googleapis.com/auth/drive.appdata`
- Android package name: `com.inoffice.app`

For local debug builds, create an Android OAuth client using your debug SHA-1 fingerprint. For release, add another Android OAuth client for your release signing SHA-1 (or Play App Signing SHA-1). Keep OAuth consent in **Testing** mode while validating with test users, then publish when ready.

See [`RELEASE_CHECKLIST.md`](RELEASE_CHECKLIST.md) for the full release-hardening and publishing flow.

## Build and test

From the repository root:

```bash
./gradlew assembleDebug
```

```bash
./gradlew test assembleDebug
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

Release APK:

```bash
./gradlew assembleRelease
```

Release signing expects `app/release.keystore` when present; otherwise the release build type falls back to the debug keystore for local builds. Passwords and alias are configured in [`app/build.gradle.kts`](app/build.gradle.kts) and must match the keystore you use.

## CI and releases

[`.github/workflows/android-build.yml`](.github/workflows/android-build.yml) runs on pushes and pull requests to `main`:

- **Pull requests** — `test` and `assembleDebug`.
- **Pushes to `main`** — Decodes `RELEASE_KEYSTORE_BASE64` into `app/release.keystore`, bumps `versionName` / `versionCode` from tags and commit message, runs `assembleRelease`, uploads the APK, and creates a GitHub Release with the APK attached.

Configure the **`RELEASE_KEYSTORE_BASE64`** repository secret (base64-encoded keystore file) for signed release builds on CI. Use [Conventional Commit](https://www.conventionalcommits.org/)-style prefixes on the first line of the merge commit (for example `feat:`, `fix:`, `chore:`) so the workflow can infer semver bumps consistently with the release job.

## QA smoke checklist

Before release candidate promotion, verify:

- Sign in succeeds for an allowed account; app opens to dashboard.
- Mark/unmark and mandate edits show queued/syncing/success state in Settings.
- Offline change + reconnect triggers eventual successful sync.
- Sign out returns to auth gate and blocks app access until sign in.

## License

See [`LICENSE`](LICENSE).
