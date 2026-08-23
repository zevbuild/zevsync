# ZevSync 📡⚡
> **Fully Offline Peer-to-Peer Bluetooth Mesh Synchronizer & Conflict-Free Cache Vault** with Direct GitHub Hub & In-App APK Distribution.

[![Android CI](https://github.com/actions/workflows/android.yml/badge.svg)](https://github.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose%20M3-brightgreen.svg)](https://developer.android.com/jetpack/compose)

---

## 🚀 Download & Install ZevSync APK

You can download and install ZevSync directly onto your Android device through multiple simple methods:

### Option 1: In-App Self-Extraction & Offline Beaming (Zero Internet!)
1. Open ZevSync on any device that already has the app.
2. Tap the **Download APK** icon (top bar, Vault, or Storage menu).
3. Tap **Extract APK to Local Vault** or **Share APK**.
4. You can now beam the APK directly over Bluetooth to nearby phones or share it over Quick Share/WhatsApp without requiring any internet connection!

### Option 2: Download Pre-Built APK from GitHub Releases
1. Go to the **[Releases](../../releases)** tab of this repository.
2. Under **Assets**, download `ZevSync-v1.0.0.apk`.
3. Open the downloaded APK on your Android device and tap **Install** (allow *Install Unknown Apps* if prompted).

### Option 3: Download Artifacts from GitHub Actions CI/CD
1. Navigate to the **[Actions](../../actions)** tab.
2. Select the latest successful workflow run.
3. Scroll down to the **Artifacts** section and download `ZevSync-debug-apk`.

### Option 4: AI Studio Export
1. In the AI Studio top-right toolbar, click the **Settings / Menu (⋮)**.
2. Select **Download APK** or **Export as ZIP**.

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
git clone https://github.com/your-username/ZevSync.git
cd ZevSync

# Build the Debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Security & Privacy
- **Zero Cloud Reliance:** All peer-to-peer transmissions occur directly over device-to-device Bluetooth sockets.
- **Local Persistence:** Room Database encrypted at rest on device.
