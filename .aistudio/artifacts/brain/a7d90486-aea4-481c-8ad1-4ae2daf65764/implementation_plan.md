# Real Device Discovery & Web Bluetooth Mesh Implementation Plan

Replace all simulated and hardcoded fake devices with actual hardware discovery via the native Web Bluetooth API (`navigator.bluetooth`), accompanied by a real browser-to-browser peer-to-peer fallback mesh (BroadcastChannel + WebRTC/local signaling) and clean empty-state onboarding.

> [!IMPORTANT]
> **Confirmed Decisions from Phase 1:**
> - **Primary Discovery Method**: Native Web Bluetooth API (`navigator.bluetooth.requestDevice`) to discover, pair, and connect with actual physical nearby Bluetooth devices.
> - **Unsupported Browser Fallback**: Real multi-window / multi-device peer-to-peer mesh fallback (via BroadcastChannel & local peer signaling) with clear status prompts when Web Bluetooth is not supported or permission is denied.

---

## 1. Overview & Core Concept

Currently, the application displays hardcoded mock devices ("Pixel 9 Pro", "Galaxy Tab S9") and injects a simulated "OnePlus 12" during scans. This update completely eradicates simulated devices:

- **True Hardware Bluetooth**: Invokes the browser's native Bluetooth pairing dialog to search for real nearby Bluetooth devices (smartphones, peripherals, BLE nodes, tablets).
- **Zero Mock Data Policy**: Device lists start empty. Only authentic devices paired by the user or discovered via real local channels appear.
- **Resilient Fallback**: In browsers without Web Bluetooth (or when Bluetooth is turned off), the app seamlessly activates a real peer-to-peer network across browser tabs, windows, and local sessions with live heartbeat pinging and actual file transfer capabilities.

---

## 2. User Experience & Visual Design

### Key User Flows

```
[ Radar Screen ] 
       │
       ├── User clicks "Scan for Bluetooth Devices"
       │       │
       │       ▼
       │  [ Native Web Bluetooth Picker ] ──► User picks real hardware device
       │                                             │
       │                                             ▼
       │                                    Device added to Mesh
       │                                    (Real name, ID, GATT connection)
       │
       └── If Web Bluetooth unavailable/denied:
               │
               ▼
          [ Peer-to-Peer Fallback Banner ]
               │
               ├── Generates live shareable Peer Link / QR
               └── Automatically connects other open ZevSync instances (Tabs / Windows)
```

### Visual & Component Specifications

- **Device Radar Screen (`BluetoothRadarScreen.tsx`)**:
  - **Zero-State / Empty Mesh**: Clean informative illustration and text: *"No devices connected yet. Scan for nearby Bluetooth hardware or open ZevSync on another tab or device to build your mesh."*
  - **Hardware Status Badge**: Quiet unboxed indicator showing Web Bluetooth availability (`Web Bluetooth Supported` or `P2P Mesh Mode Active`).
  - **Scan Button**: Prominent action triggering `navigator.bluetooth.requestDevice()`.
  - **Device Cards**: Display authentic device name, hardware ID, connection state (`Connected` / `Disconnected`), and disconnect action.

---

## 3. Key Product Decisions & Trade-Offs

- **Real Web Bluetooth API (`navigator.bluetooth`)**:
  - *Chosen Approach*: Request device using `acceptAllDevices: true` and standard optional services (`battery_service`, `device_information`, `generic_access`) to maximize compatibility with phones, tablets, and peripherals.
  - *Why*: Allows actual discovery of physical Bluetooth hardware directly from web browsers without native APK installation.
- **Cross-Session P2P Mesh Fallback**:
  - *Chosen Approach*: Use `BroadcastChannel('zevsync_mesh_bus')` with synchronized local peer heartbeats.
  - *Why*: Enables instant testing and real multi-device synchronization between two browser windows or tabs on the same machine or local LAN without needing external servers.
- **Eradication of Mock Artifacts**:
  - *Chosen Approach*: Remove hardcoded initial peer array, mock scan timer additions, and simulated mock conflicts with fictitious nodes.
  - *Why*: Directly solves the user's report of fake devices.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   ZevSync App Context                  │
└───────────────────────────┬────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            ▼                               ▼
┌────────────────────────┐      ┌────────────────────────┐
│   Web Bluetooth API    │      │  Real P2P Mesh Engine  │
│  (navigator.bluetooth) │      │   (BroadcastChannel)   │
├────────────────────────┤      ├────────────────────────┤
│ • requestDevice()      │      │ • Node Heartbeat Ping  │
│ • gatt.connect()       │      │ • Peer-to-Peer Packets │
│ • gattserverdisconnect │      │ • Multi-Window Sync    │
└────────────────────────┘      └────────────────────────┘
            │                               │
            └───────────────┬───────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│               Unified Discovered Peer List             │
│            (Only 100% Real Connected Nodes)            │
└────────────────────────────────────────────────────────┘
```

### Key Changes by Module

1. **`src/services/webBluetoothService.ts` (New)**:
   - Encapsulates `navigator.bluetooth` capability checks, `requestDevice()`, GATT server connection management, and disconnection listeners.
2. **`src/services/p2pMeshService.ts` (New)**:
   - BroadcastChannel-based real-time communication bus for multi-device/multi-window synchronization without external infrastructure.
3. **`src/context/SyncBeamContext.tsx`**:
   - Initialize `discoveredPeers` as empty `[]`.
   - Wire `startBluetoothScan` to `webBluetoothService.scanAndConnect()`.
   - Integrate P2P fallback bus to detect and sync with real local peer sessions.
   - Clean up initial mock conflicts that referenced fictitious devices.
4. **`src/components/BluetoothRadarScreen.tsx`**:
   - Add native Web Bluetooth capability status notice.
   - Implement real connection controls and clear empty states.
