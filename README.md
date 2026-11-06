# O‘zbekcha Caller ID (Android)

A simple Android starter app that announces incoming callers in Uzbek when the device has an installed Uzbek Text-to-Speech voice.

## Build APK on GitHub (free for public repositories within GitHub Actions limits)
1. Create a GitHub account and a **new public repository** named `UzbekCallerID`.
2. Upload all files and folders from this project to the repository (keep `.github/workflows/build-apk.yml`).
3. Open the repository's **Actions** tab.
4. Select **Build Android APK** and press **Run workflow** (or wait for the first push build).
5. Open the completed workflow run, find **Artifacts**, and download `UzbekCallerID-debug-apk`.
6. Extract the ZIP and install `app-debug.apk` on your Android phone. Allow installation from that browser/file manager if Android asks.

## Important limitations
- The app requires Android runtime permissions for Phone and Contacts.
- It uses Android's built-in Text-to-Speech engine and requests `uz-UZ`. It does not download a voice. If the phone has no Uzbek TTS voice, it deliberately does not fall back to Russian/English.
- Android/device versions may restrict delivery of incoming phone numbers to third-party apps. On some devices the caller number may be unavailable.
- This is an initial starter build, not a Play Store-certified release. Test it with a non-critical call before relying on it.
