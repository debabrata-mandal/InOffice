# InOffice Release Checklist

Use this checklist before publishing a new InOffice build to broad users.

## 1) Google Cloud project and APIs

- Select the correct Google Cloud project for InOffice.
- Ensure **Google Drive API** is enabled.
- Verify OAuth consent screen branding/contact details are complete.
- Keep OAuth app in **Testing** while validating internally.

## 2) OAuth clients and SHA fingerprints

Create Android OAuth clients for:

- **Debug build**
  - Package: `com.inoffice.app`
  - SHA-1: debug keystore fingerprint
- **Release build**
  - Package: `com.inoffice.app`
  - SHA-1: release signing key fingerprint
- **Play Store distribution** (if using Play App Signing)
  - Package: `com.inoffice.app`
  - SHA-1: Play App Signing certificate fingerprint

If any expected signing key SHA-1 is missing, Google sign-in may fail with `DEVELOPER_ERROR (10)`.

## 3) OAuth audience strategy

- In **Testing**, ensure all testers are listed as OAuth test users.
- Before public rollout, switch OAuth app to **In production**.
- Confirm the requested Drive scope is only:
  - `https://www.googleapis.com/auth/drive.appdata`

## 4) App signing and CI

- Confirm `app/release.keystore` settings in `app/build.gradle.kts` match your real release keystore.
- Confirm GitHub secret `RELEASE_KEYSTORE_BASE64` is set.
- Run CI path used for release (`assembleRelease`) successfully.

## 5) Functional QA (must pass)

- First-run sign-in gate works and dashboard opens post-auth.
- Mark day and change mandate values; sync status updates in Settings.
- Offline edit then reconnect eventually syncs.
- Manual `Sync now` works.
- Sign out returns to auth gate and blocks app until sign-in.
- Install same account on two devices and validate update/delete conflict behavior.

## 6) Data integrity checks

- Day entries persist after app restart.
- Deleted entries do not reappear after cross-device sync.
- Tombstone pruning works without resurrecting recently deleted records.

## 7) Final go/no-go

- Version bump and changelog/release notes prepared.
- Release APK or App Bundle built from a clean state.
- Smoke test completed on at least one physical device.
