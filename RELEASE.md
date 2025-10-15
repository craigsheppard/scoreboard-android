# Android Release Setup Guide

This document describes the GitHub Actions workflow for automatically building, signing, and releasing the Scoreboard Android app to Google Play Store.

## Overview

When you create a GitHub release, the workflow will:
1. Automatically increment the version code
2. Update the app version from the git tag
3. Build and sign the Android App Bundle (AAB)
4. Upload the AAB to the GitHub release
5. Deploy the AAB to Google Play Store (production track)

## Prerequisites

### 1. Create a Release Keystore

If you don't already have a release keystore, create one:

```bash
keytool -genkey -v -keystore scoreboard-release.jks \
  -alias scoreboard \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**Important:** Save the keystore file and passwords securely! You'll need:
- Keystore password (set during creation)
- Key alias (e.g., "scoreboard")
- Key password (set during creation)

### 2. Set Up Google Play Console

#### Create a Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project (or create one)
3. Enable the **Google Play Android Developer API**
4. Go to **IAM & Admin** > **Service Accounts**
5. Click **Create Service Account**
   - Name: `scoreboard-release-deployer`
   - Click **Create and Continue**
   - Skip granting roles (we'll do this in Play Console)
   - Click **Done**
6. Click on the service account you just created
7. Go to **Keys** tab
8. Click **Add Key** > **Create new key** > **JSON**
9. Save the JSON file securely

#### Grant Permissions in Google Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app (or create a new app if needed)
3. Go to **Setup** > **API access**
4. Link your Google Cloud project if not already linked
5. Under **Service accounts**, find your service account
6. Click **Grant access**
7. Grant the following permissions:
   - **Releases**: Create and edit releases
   - **App access**: View app information
8. Click **Invite user** then **Apply**

### 3. Configure GitHub Secrets

Go to your GitHub repository > **Settings** > **Secrets and variables** > **Actions**

Add the following **Repository secrets**:

#### `ANDROID_KEYSTORE_BASE64`
Convert your keystore to base64:
```bash
base64 -i scoreboard-release.jks | pbcopy  # macOS
# or
base64 -w 0 scoreboard-release.jks  # Linux
```
Paste the output as the secret value.

#### `ANDROID_KEYSTORE_PASSWORD`
The password you used when creating the keystore.

#### `ANDROID_KEY_ALIAS`
The alias you used when creating the keystore (e.g., "scoreboard").

#### `ANDROID_KEY_PASSWORD`
The key password (may be the same as keystore password).

#### `SCOREBOARD_GOOGLE_PLAY_SERVICE_ACCOUNT`
Paste the **entire contents** of the service account JSON file you downloaded from Google Cloud Console.

Example:
```json
{
  "type": "service_account",
  "project_id": "your-project-id",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "scoreboard-release-deployer@your-project.iam.gserviceaccount.com",
  ...
}
```

## Creating a Release

### 1. Ensure Your Code is Ready

Make sure:
- All changes are committed and pushed
- Tests pass
- Code builds successfully locally

### 2. Create and Push a Git Tag

```bash
# Create a new tag (use semantic versioning)
git tag v1.0.0

# Push the tag to GitHub
git push origin v1.0.0
```

### 3. Create a GitHub Release

#### Via GitHub UI:
1. Go to your repository on GitHub
2. Click **Releases** (right sidebar)
3. Click **Create a new release**
4. Select your tag (e.g., `v1.0.0`)
5. Add release title (e.g., "Version 1.0.0")
6. Add release notes describing changes
7. Click **Publish release**

#### Via GitHub CLI:
```bash
gh release create v1.0.0 \
  --title "Version 1.0.0" \
  --notes "Initial release with core features"
```

### 4. Monitor the Workflow

1. Go to **Actions** tab in your GitHub repository
2. Click on the running workflow
3. Monitor the build progress
4. If successful, the AAB will be:
   - Attached to the GitHub release
   - Deployed to Google Play Store (production track)

## Version Management

The workflow automatically manages version codes:
- **Version Name**: Extracted from the git tag (e.g., `v1.0.0` → `1.0.0`)
- **Version Code**: Auto-incremented from the previous release
  - Starts at 1 for the first release
  - Increments by 1 for each subsequent release
  - Stored in `androidVersionCode.txt` attached to each release

You don't need to manually update version codes in the `build.gradle.kts` file for releases.

## Troubleshooting

### Build Fails: "Keystore not found"
- Verify `ANDROID_KEYSTORE_BASE64` secret is set correctly
- Ensure base64 encoding doesn't have line breaks

### Build Fails: "Incorrect keystore password"
- Verify `ANDROID_KEYSTORE_PASSWORD` and `ANDROID_KEY_PASSWORD` are correct

### Deploy Fails: "Service account permissions"
- Ensure service account has proper permissions in Play Console
- Verify `SCOREBOARD_GOOGLE_PLAY_SERVICE_ACCOUNT` contains valid JSON

### First Release to Play Console
If this is your first release to Play Console:
1. You may need to upload an initial APK/AAB manually via the console
2. Complete the store listing setup
3. Create a release manually once
4. Then the automated workflow will work for subsequent releases

### Package Name Mismatch
Ensure the `packageName` in `.github/workflows/android-release.yaml` matches the `applicationId` in `app/build.gradle.kts`:
- Current: `com.scoreboard`

## Workflow File Location

`.github/workflows/android-release.yaml`

## Local Testing (Optional)

To test signing locally, create a `keystore.properties` file in the project root:

```properties
storeFile=./path/to/your/scoreboard-release.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=scoreboard
keyPassword=YOUR_KEY_PASSWORD
```

Then build:
```bash
./gradlew bundleRelease
```

**Important:** Never commit `keystore.properties` or your keystore file to git! They are already in `.gitignore`.

## Security Notes

- ✅ Keystore and passwords are stored as encrypted GitHub secrets
- ✅ Keystore file is never committed to the repository
- ✅ Service account has minimal required permissions
- ✅ Secrets are only accessible to repository administrators
- ⚠️ Rotate service account keys periodically (every 90 days recommended)
- ⚠️ Store keystore backup in a secure location (losing it means you can't update your app!)

## Additional Resources

- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [Google Play Developer API](https://developers.google.com/android-publisher)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
