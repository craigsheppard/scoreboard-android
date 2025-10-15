# Manager Review - Release Workflow Setup

This document outlines the remaining tasks to complete the automated release workflow for the Scoreboard Android app.

## ✅ Completed

- [x] GitHub Actions workflow created (`.github/workflows/android-release.yaml`)
- [x] Gradle build configuration updated with signing support
- [x] `.gitignore` updated to prevent committing sensitive files
- [x] Comprehensive release documentation created (`RELEASE.md`)

## 🔲 Outstanding Tasks

### 1. Generate Release Keystore

**Priority:** High
**Owner:** Craig
**Reference:** RELEASE.md, Section "1. Create a Release Keystore"

You need to generate a new Android release keystore for the Scoreboard app:

```bash
keytool -genkey -v -keystore scoreboard-release.jks \
  -alias scoreboard \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**During creation, you'll be prompted for:**
- Keystore password (create a strong password)
- Key password (can be the same as keystore password)
- Your name/organization details

**After creation:**
- ✅ Store `scoreboard-release.jks` in a secure backup location (1Password, encrypted drive, etc.)
- ✅ Document the passwords securely
- ⚠️ **CRITICAL:** If you lose this keystore, you cannot update the app on Google Play!

---

### 2. Set Up Google Play Console Service Account

**Priority:** High
**Owner:** Craig
**Reference:** RELEASE.md, Section "2. Set Up Google Play Console"

#### Step 2a: Create Service Account in Google Cloud Console

1. Go to https://console.cloud.google.com/
2. Select your project (or create a new one for Scoreboard)
3. Enable the **Google Play Android Developer API**
4. Navigate to **IAM & Admin** > **Service Accounts**
5. Click **Create Service Account**:
   - Name: `scoreboard-release-deployer`
   - Description: "Automated deployment for Scoreboard Android app"
   - Click **Create and Continue**
   - Skip role assignment (permissions granted in Play Console)
   - Click **Done**
6. Click on the newly created service account
7. Go to **Keys** tab
8. Click **Add Key** > **Create new key** > **JSON**
9. Download and save the JSON file securely

**Deliverable:** Service account JSON file (e.g., `scoreboard-deployer-credentials.json`)

#### Step 2b: Grant Permissions in Google Play Console

1. Go to https://play.google.com/console
2. Select the Scoreboard app (create the app first if needed)
3. Navigate to **Setup** > **API access**
4. Link your Google Cloud project (if not already linked)
5. Find your service account under **Service accounts**
6. Click **Grant access**
7. Grant permissions:
   - ✅ **Releases** > Create and edit releases
   - ✅ **App access** > View app information
8. Click **Invite user** then **Apply**

**Note:** You may need to create an initial release manually in Play Console before the automated workflow can deploy.

---

### 3. Configure GitHub Repository Secrets

**Priority:** High
**Owner:** Craig
**Reference:** RELEASE.md, Section "3. Configure GitHub Secrets"

Go to: https://github.com/YOUR_USERNAME/scoreboard-android/settings/secrets/actions

Add the following **5 repository secrets**:

#### Secret 1: `ANDROID_KEYSTORE_BASE64`

Convert your keystore to base64:
```bash
base64 -i scoreboard-release.jks | pbcopy
```

Then paste the clipboard contents as the secret value in GitHub.

**Verification:** The secret should be a long string of random-looking characters (no spaces or line breaks).

---

#### Secret 2: `ANDROID_KEYSTORE_PASSWORD`

Paste the keystore password you created in Task #1.

---

#### Secret 3: `ANDROID_KEY_ALIAS`

Enter the key alias you used when creating the keystore (e.g., `scoreboard`).

---

#### Secret 4: `ANDROID_KEY_PASSWORD`

Paste the key password you created in Task #1 (may be identical to keystore password).

---

#### Secret 5: `SCOREBOARD_GOOGLE_PLAY_SERVICE_ACCOUNT`

Open the service account JSON file from Task #2a and paste the **entire contents** into this secret.

Example format:
```json
{
  "type": "service_account",
  "project_id": "scoreboard-12345",
  "private_key_id": "abc123...",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIE...\n-----END PRIVATE KEY-----\n",
  "client_email": "scoreboard-release-deployer@scoreboard-12345.iam.gserviceaccount.com",
  "client_id": "123456789",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/..."
}
```

**Verification:** The pasted JSON should be valid (no syntax errors).

---

### 4. Create Google Play Console App Listing (If Not Exists)

**Priority:** High
**Owner:** Craig
**Reference:** Play Console documentation

If the app doesn't exist yet in Google Play Console:

1. Go to https://play.google.com/console
2. Click **Create app**
3. Fill in basic details:
   - App name: "Scoreboard"
   - Default language: English (United States)
   - App or Game: App
   - Free or Paid: Free (or Paid, your choice)
4. Accept declarations and create the app
5. Complete the required sections:
   - ✅ App content (privacy policy, target audience, etc.)
   - ✅ Store listing (screenshots, description, icon)
   - ✅ Content rating questionnaire
6. Create an **internal testing track** or **production track** release manually with the first AAB

**Note:** The first AAB upload may need to be manual. After that, the workflow can automate subsequent releases.

---

### 5. Test the Workflow

**Priority:** Medium
**Owner:** Craig
**Prerequisites:** Tasks 1-4 must be complete

Once all secrets are configured:

1. Ensure all changes are committed and pushed to GitHub
2. Create a test release:
   ```bash
   git tag v0.1.0-test
   git push origin v0.1.0-test
   gh release create v0.1.0-test \
     --title "Test Release v0.1.0" \
     --notes "Testing automated release workflow" \
     --prerelease
   ```
3. Monitor the workflow:
   - Go to https://github.com/YOUR_USERNAME/scoreboard-android/actions
   - Click on the running workflow
   - Watch for any errors

**Expected outcome:**
- ✅ Workflow completes successfully
- ✅ AAB is attached to the GitHub release
- ✅ AAB is uploaded to Google Play Console

**If errors occur:** See "Troubleshooting" section in RELEASE.md

---

### 6. Update CLAUDE.md with Release Notes

**Priority:** Low
**Owner:** Craig

Add a section to `CLAUDE.md` documenting the release process:

```markdown
## 📦 Releases

The app uses automated GitHub Actions for releases. See `RELEASE.md` for details.

To create a new release:
1. Tag the commit: `git tag v1.0.0`
2. Push the tag: `git push origin v1.0.0`
3. Create a GitHub release from the tag
4. Workflow automatically builds, signs, and deploys to Play Store
```

---

## 📋 Checklist Summary

Before your first automated release, verify:

- [ ] Keystore generated and backed up securely
- [ ] Service account created in Google Cloud Console
- [ ] Service account permissions granted in Play Console
- [ ] All 5 GitHub secrets configured
- [ ] App created in Play Console (if first release)
- [ ] Initial manual release completed (if required by Play Console)
- [ ] Test release created and workflow verified

---

## 🔐 Security Reminders

1. **Keystore Backup:** Store `scoreboard-release.jks` in at least 2 secure locations (e.g., 1Password + encrypted cloud backup)
2. **Password Management:** Use a password manager (1Password, LastPass, etc.) to store:
   - Keystore password
   - Key alias
   - Key password
   - Location of keystore file
3. **Service Account JSON:** Store the JSON file securely; it grants deployment access
4. **GitHub Secrets:** Only repository admins can view/edit secrets
5. **Rotation:** Plan to rotate the service account key every 90 days

---

## 📞 Support

- **GitHub Actions Issues:** Check `.github/workflows/android-release.yaml` and workflow logs
- **Play Console Issues:** https://support.google.com/googleplay/android-developer
- **Signing Issues:** See RELEASE.md "Troubleshooting" section

---

## 🎯 Next Steps After Setup

Once the workflow is tested and working:

1. Delete the test release/tag if desired
2. Create your first production release (e.g., `v1.0.0`)
3. Monitor the Play Store for the app to appear
4. Set up crash reporting (Firebase Crashlytics - optional)
5. Enable Play App Signing for additional security (optional but recommended)

---

## Notes

- The workflow is configured to deploy to the **production** track by default
- To deploy to internal/alpha/beta instead, edit `.github/workflows/android-release.yaml` line 146: change `track: production` to `track: internal` (or `alpha`, `beta`)
- Version codes are automatically incremented; you only need to create semantic version tags (v1.0.0, v1.1.0, etc.)
