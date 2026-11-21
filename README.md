# SnapCatalog

**SnapCatalog** is a native Android app built with Kotlin and Jetpack Compose for capturing, naming, organizing, and exporting photo inventories into portable ZIP archives with structured JSON metadata.

## Download the Latest APK

The latest compiled Android `.apk` is built automatically on every push to `main`/`master` and published under the **Releases** tab on the right side of this GitHub repository.

1. Open the **Releases** section on the right sidebar of GitHub.
2. Select the latest `v1.0.x` release.
3. Download the attached `.apk` file under **Assets** and install it on your Android device (Android 7.0 / API 24 or newer).

## Features

- **Category Lists:** Create custom category lists (e.g., *Kitchen Items*, *Tools*, *Documents*) with color-coded compact cards and optional descriptions displayed inside each list.
- **Continuous Capture & Auto-Naming Workflow:** Take a photo of an item, immediately get the keyboard focused to name the item, save it directly to the list's folder, and automatically reopen the camera for the next item.
- **Gallery Import & Item Management:** Add existing photos via the Android Photo Picker, search items, view full-screen details, rename items (automatically syncing stored filenames), or delete items.
- **One-Tap ZIP + JSON Export:** Share any list as a `.zip` file containing all item photos saved with their assigned names inside the category folder, alongside a `data.json` manifest documenting each item's name, filename, and folder location.

## Tech Stack

- **Language & UI:** Kotlin, Jetpack Compose, Material Design 3
- **Local Storage:** Room Database + App-scoped folder organization
- **Image Loading:** Coil Compose
- **Build System:** Gradle Kotlin DSL (Gradle 9.3.1, AGP 9.1.1, `compileSdk 36.1`, `minSdk 24`)

## Building Locally

1. Clone the repository.
2. Generate or restore the debug keystore (if not present):
   ```bash
   tr -d '[:space:]' < debug.keystore.base64 | base64 -d > debug.keystore
   ```
3. Build the debug APK using the Gradle wrapper:
   ```bash
   ./gradlew assembleDebug
   ```
   The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Automated CI/CD (GitHub Actions)

The workflow in `.github/workflows/build-and-release.yml` automatically:
- Triggers on every push to `main` or `master`, or manually via **Actions → Run workflow**.
- Builds a **Debug APK** out of the box with zero manual secret configuration.
- Optionally builds a **Signed Release APK** if you add the following repository secrets under **Settings → Secrets and variables → Actions**:
  - `KEYSTORE_BASE64`: Base64-encoded `.jks` keystore containing key alias `upload`
  - `STORE_PASSWORD`: Keystore password
  - `KEY_PASSWORD`: Key password
- Publishes a new GitHub Release tagged `v1.0.<run_number>` with the `.apk` attached.
