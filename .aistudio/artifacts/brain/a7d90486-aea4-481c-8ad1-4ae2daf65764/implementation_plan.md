# Replace CI Workflow with Web-Only Build & PWA Offline Engine

Transform the repository into a 100% reliable, Web-only offline application by replacing the failing Android CI workflow with a streamlined GitHub Actions Web CI pipeline and enabling full Progressive Web App (PWA) offline capabilities.

> [!IMPORTANT]
> **Confirmed User Decisions:**
> - **CI Target**: Replace `.github/workflows/android.yml` with a Web-only build, lint, and test workflow.
> - **Execution Mode**: 100% web-native with complete offline standalone capabilities (no Android Gradle or keystore requirements).

---

## 1. Overview & Core Concept

The repository originally included Android native Gradle configs (`.github/workflows/android.yml`, `app/`, `gradlew`) that fail in GitHub Actions due to missing keystores, Android SDK version mismatches, and build dependencies.

This implementation delivers:
1. **Streamlined Web GitHub Actions Workflow (`.github/workflows/web.yml`)**:
   - Automated testing on Node.js 22.
   - Executes dependency installation, TypeScript type checking (`tsc --noEmit`), and production compilation (`vite build`).
   - Uploads static build artifacts (`dist/`) ready for deployment (GitHub Pages, hosting, or release distribution).
2. **100% Offline Progressive Web App (PWA)**:
   - Offline Service Worker caching all static assets (HTML, CSS, JS, fonts, icons) so the app works seamlessly with zero internet connection.
   - Compliant Web App Manifest (`manifest.json`) for installing ZevSync directly to mobile home screens and desktops as a standalone app.
   - In-app install banner/button with iOS Safari and Android/Desktop support.
3. **Repository Cleanliness**:
   - Delete obsolete Android Gradle artifacts (`app/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradlew`, `android.yml`) that cause CI failures and clutter.

---

## 2. User Experience & Visual Design

```
┌─────────────────────────────────────────────────────────────┐
│ [ZevSync Brand]        [Install App] [Offline Mesh] [Hub]   │ ◄ Header with PWA Install
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────────────────────────────────────────────┐   │
│   │  Bluetooth & P2P Mesh                               │   │
│   │  Local node: Mac Workstation · Real hardware P2P    │   │
│   │  [ MESH ONLINE ] [ 100% OFFLINE CACHED ]            │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

- **In-App PWA Install Trigger**:
  - Automatically detects whether the app is installable on Chromium / Android / Desktop or iOS Safari.
  - Renders a clean action in the header (`Install App`) that triggers the native OS installation dialog.
  - Automatically hides once installed in standalone mode.
- **Offline Reliability Badge**:
  - Visual reassurance that cache storage and mesh networking function completely offline without internet connectivity.

---

## 3. Key Product Decisions & Trade-Offs

- **Web-Only CI Workflow**:
  - *Chosen Approach*: Replace `android.yml` with a clean `web.yml` running Node.js 22, `npm ci`, `npm run lint`, and `npm run build`.
  - *Why*: Directly eliminates Gradle compilation errors, missing keystore secret failures, and 15+ minute Android emulator runs in GitHub Actions. Web build takes under 20 seconds.
- **Service Worker & Manifest**:
  - *Chosen Approach*: Configure Web App Manifest (`/manifest.json`), service worker registration (`/sw.js`), and install hook (`usePWAInstall.ts`).
  - *Why*: Allows users on Android, iOS, Windows, Mac, and Linux to install ZevSync as a native-feeling standalone app directly from their browser with full offline caching.
- **Purge Obsolete Android Files**:
  - *Chosen Approach*: Remove `app/`, `gradle/`, `gradlew`, and `settings.gradle.kts`.
  - *Why*: Eliminates false-positive CI triggers and avoids confusion between Android native and Web architectures.

---

## 4. Technical Architecture & CI Pipeline

```
┌────────────────────────────────────────────────────────┐
│            GitHub Actions: .github/workflows/web.yml   │
├────────────────────────────────────────────────────────┤
│ 1. Checkout (actions/checkout@v4)                      │
│ 2. Setup Node.js 22 (actions/setup-node@v4)           │
│ 3. Install Dependencies (npm ci)                       │
│ 4. Type Check & Lint (npm run lint)                   │
│ 5. Production Build (npm run build)                   │
│ 6. Upload Artifacts (actions/upload-artifact@v4)       │
└────────────────────────────────────────────────────────┘
```

### Key Changes by File:

1. **`/.github/workflows/web.yml`**:
   - Modern, reliable CI pipeline for pushes and PRs on `main`/`master` and tags.
2. **`/.github/workflows/android.yml`**:
   - Deleted to resolve the CI failure.
3. **`/public/manifest.json` & `/public/sw.js`**:
   - Web App Manifest and offline caching service worker for complete offline functionality.
4. **`/src/hooks/usePWAInstall.ts` & `/src/components/Header.tsx`**:
   - In-app install affordance and offline status indication.
5. **Clean up Android files**:
   - Remove `app/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradlew`, `gradlew.bat`.
