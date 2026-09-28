# CloudBackup Android

CloudBackup is an Android notes/todos app with offline Room storage and Google Drive backup integration.

## Automated APK/AAB builds

GitHub Actions is configured in `.github/workflows/android-release.yml`.

- Every push and pull request runs a debug build and checks the project.
- A tag such as `v1.0.0` builds a signed release APK and AAB.
- The tag workflow publishes the APK and AAB to a GitHub Release.
- The workflow also supports optional Google Play internal-track publishing.

### Required GitHub Actions secrets

Configure these repository secrets before creating a release tag:

- `ANDROID_KEYSTORE_BASE64`: base64 contents of the release `.keystore` file
- `ANDROID_KEYSTORE_PASSWORD`: keystore password
- `ANDROID_KEY_ALIAS`: signing key alias
- `ANDROID_KEY_PASSWORD`: signing key password

For optional Play Store upload, additionally configure:

- `PLAY_STORE_SERVICE_ACCOUNT_JSON`: Google Play Console service-account JSON

The service account must have release permissions in Play Console. The package name is `cloud.rakib.backup`, and the workflow uploads to the `internal` track when the Play secret is present.

## Local build

```bash
./gradlew assembleDebug
./gradlew bundleRelease
```

A release build needs the four signing environment variables above, plus a local keystore path in `RELEASE_STORE_FILE`.

## Release process

```bash
git tag v1.0.0
git push origin v1.0.0
```

The workflow output includes SHA-256 checksums for both generated artifacts. Never commit a keystore or service-account JSON file.
