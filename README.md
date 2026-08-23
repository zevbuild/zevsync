# SyncBeam 📡⚡
> **Fully Offline Peer-to-Peer Bluetooth Mesh Synchronizer & Conflict-Free Cache Vault** with Direct GitHub Hub Integration.

[![Android CI](https://github.com/actions/workflows/android.yml/badge.svg)](https://github.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose%20M3-brightgreen.svg)](https://developer.android.com/jetpack/compose)

---

## 🚀 Direct Download via GitHub

You can download and install SyncBeam directly onto your Android device from GitHub:

### Option 1: Download Pre-Built APK from GitHub Releases (Recommended)
1. Go to the **[Releases](../../releases)** tab of this repository.
2. Under **Assets**, click on `SyncBeam-v1.0.0.apk` (or `app-debug.apk` / `app-release.apk`).
3. Open the downloaded APK on your Android device and tap **Install** (allow *Install Unknown Apps* if prompted).

### Option 2: Download Artifacts from GitHub Actions
1. Navigate to the **[Actions](../../actions)** tab.
2. Select the latest successful workflow run.
3. Scroll down to the **Artifacts** section and download `SyncBeam-debug-apk`.

### Option 3: In-App GitHub Direct Hub
SyncBeam comes equipped with an internal **GitHub Direct Hub** allowing you to:
- Directly fetch files, raw scripts, and repositories straight into your offline vault.
- Browse repository directory trees with one tap.
- Download latest release assets and APKs directly inside the app.
- Export your local offline notes and documents as GitHub Gists.

---

## ✨ Core Features

### 1. 📶 100% Offline Bluetooth Auto-Environment
- Automatically scans and pairs with nearby peer devices over Bluetooth RFCOMM & BLE.
- Peer discovery with real-time distance proximity estimations, RSSI telemetry, and channel handshake.
- Live chunked streaming with high throughput socket connections and instant resume.

### 2. 🗃️ Conflict-Free Cache Storage Vault
- **SHA-256 Content-Addressable Storage (CAS):** Deduplicates identical files automatically.
- **Lamport Timestamps & Vector Clocks:** Tracks lineage across every peer device to detect concurrent revisions.
- **Visual 3-Way Merge & Conflict Resolver:** Side-by-side visual diff comparison with diff highlighting and manual/automatic resolution modes.
- **Intelligent LRU Cache Eviction & Pinning:** Set storage quotas (500 MB – 10 GB) with auto-purge protection for pinned documents.

### 3. 📝 Live Markdown, Code & Document Editor
- In-app text and code viewer/editor with syntax awareness (.md, .kt, .json, .py, .txt).
- Live vector clock generation upon saving edits.

---

## 🛠️ Building From Source

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17
- Android SDK 35 (minSdk 26)

### Clone & Build Command
```bash
# Clone the repository
git clone https://github.com/your-username/SyncBeam.git
cd SyncBeam

# Build the Debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Security & Privacy
- **Zero Cloud Reliance:** All peer-to-peer transmissions occur directly over device-to-device Bluetooth sockets.
- **Local Persistence:** Room Database encrypted at rest on device.
