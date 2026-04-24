# InOffice

Android app for tracking how many days you are **in office**, on **leave**, or on **holiday** against a configurable monthly mandate. Data is stored on the device (Room + DataStore); account and cloud sync are planned.

## Features

- **Dashboard** — Progress toward the monthly in-office target, with breakdown by base mandate, leave, and holidays.
- **Day marks** — Mark today as in office, leave, holiday, or clear the mark.
- **Month report** — Browse prior/next months and see a list of marked days.
- **Settings** — Set the base in-office mandate per month.

## Requirements

- **JDK 17** (matches Gradle `jvmTarget` and CI).
- **Android Studio** Koala or newer recommended (AGP 8.7.x, Kotlin 2.0, Compose).

## Tech stack

- Kotlin, Jetpack Compose (Material 3), Navigation Compose  
- Hilt (DI), Room (persistence), DataStore Preferences (settings)  
- `minSdk` 26, `compileSdk` / `targetSdk` 35  

Version catalogs and dependency versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

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

## License

See [`LICENSE`](LICENSE).
