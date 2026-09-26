# ZevSync 📡⚡
> **Fully Offline Peer-to-Peer Bluetooth Mesh Synchronizer & Conflict-Free Cache Vault** with Direct GitHub Hub & In-App APK Distribution (Rewritten in React & TypeScript with Vite and Tailwind CSS).

[![React](https://img.shields.io/badge/React-19-blue.svg)](https://react.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7+-blue.svg)](https://www.typescriptlang.org)
[![Vite](https://img.shields.io/badge/Vite-6.0-646CFF.svg)](https://vitejs.dev)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-v4-38BDF8.svg)](https://tailwindcss.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

---

## 🚀 Overview

ZevSync is an offline-first peer-to-peer file sharing and cache synchronization application. It creates an autonomous peer-to-peer sharing and cache environment without needing any internet connection, cellular data, or central servers.

### Key Capabilities
- **📶 Bluetooth Mesh Network & Radar**: Detects nearby Bluetooth devices and virtual mesh nodes, monitors RSSI dBm signal strength, and streams chunked files with verification.
- **🗃️ Content-Addressable Storage (CAS)**: SHA-256 integrity checksums with category breakdown and pin protection against cache eviction.
- **⏱️ Vector Clock & Lamport Lineage**: Tracks lineage across every peer device to detect concurrent revisions without centralized clocks.
- **⚔️ Side-by-Side Conflict Resolution**: 4 resolution strategies (Keep Local Mine, Adopt Remote Peer, Fork Duplicate Branch, or Interactive Manual Merge Editor).
- **🐙 GitHub Direct Hub**: Directly imports raw files, release assets, and repository file trees, and exports vault documents to GitHub Gists.
- **📱 Android APK Sideloading**: In-app self-hosting package extraction and step-by-step sideloading installation guide.

---

## 💻 Tech Stack

- **Framework**: React 19 + TypeScript
- **Bundler**: Vite
- **Styling**: Tailwind CSS
- **Icons**: Lucide React
- **Cryptography**: Web Crypto API (SHA-256)
- **Lineage Engine**: In-memory & reactive vector clock state machine

---

## 🛠️ Development & Build

### Running Locally

```bash
# Install dependencies
npm install

# Start Vite dev server on port 3000
npm run dev
```

The application will be served at `http://localhost:3000`.

### Production Build

```bash
# Type check and build bundle
npm run build
```

The compiled assets will be output to the `dist/` directory.

---

## 🔒 Security & Privacy

- **100% Offline-Capable**: Peer-to-peer operations run completely client-side in the browser or on device.
- **Cryptographic Verification**: Every file and block transfer is verified with SHA-256 digest checks.
