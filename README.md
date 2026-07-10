<div align="center">

<img src="metadata/screenshots/banner.png" width="100%" alt="PoseCoach Banner" />

# PoseCoach

**A hybrid Android app that overlays real-time pose guidance on the camera feed — entirely on-device, no server, no accounts.**

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Framework](https://img.shields.io/badge/Framework-TanStack_Start-C9A84C?style=flat-square&logo=react&logoColor=white)](https://tanstack.com/start)
[![Styling](https://img.shields.io/badge/Styling-Tailwind_CSS_v4-38B2AC?style=flat-square&logo=tailwind-css&logoColor=white)](https://tailwindcss.com)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

[Features](#features) • [Architecture](#architecture) • [Setup](#setup) • [Limitations](#known-limitations--roadmap)

</div>

---

## What it does

PoseCoach shows a translucent pose silhouette over your live camera feed and gives real-time positional feedback (tilt / turn / chin / shoulder angle) so you can match a target pose before taking a photo. All camera frames and processing stay on-device — nothing is uploaded or stored remotely.

<table align="center">
  <tr>
    <td width="50%"><img src="metadata/screenshots/camera_view.png" alt="Live silhouette overlay" /></td>
    <td width="50%"><img src="metadata/screenshots/pose_library.png" alt="Pose library" /></td>
  </tr>
  <tr>
    <td align="center"><b>Live silhouette + angle overlay</b></td>
    <td align="center"><b>Pose library</b></td>
  </tr>
</table>

*(Demo GIF coming soon — see [Known Limitations](#known-limitations--roadmap))*

## Features

- **Live silhouette overlay** — a target pose is rendered on `<canvas>` on top of the camera feed for direct visual alignment.
- **Angle coach** — computes tilt/turn/chin/shoulder offsets from the live camera stream and surfaces corrective guidance in real time (`useAngleCoach.ts`).
- **Pose library** — curated set of poses across travel, lifestyle, business, and couple categories.
- **On-device only** — camera frames never leave the device; no backend, no accounts, no analytics.
- **One-tap capture** — saves directly to the native Android gallery via a custom JS↔native bridge.
- **Pinch-to-zoom overlay** — scale/position the silhouette to fit the frame; mirror-aware for front camera.
- **Native Android integration** — immersive display mode, haptic feedback on shutter/UI actions, persistent camera permission handling.

## Architecture

PoseCoach is a **hybrid app**: a native Android shell (Kotlin) hosting a web-based UI (React), connected by a custom JavaScript bridge.

```
┌─────────────────────────────────────────────┐
│  Android Shell (Kotlin)                     │
│  - WebView host, permissions, lifecycle     │
│  - WindowInsetsControllerCompat (immersive) │
│  - HapticFeedbackConstants                  │
│  - JavascriptInterface  ───────────────┐    │
└─────────────────────────────────────────│───┘
                                          │ Base64/Blob bridge
┌─────────────────────────────────────────▼───┐
│  Web Engine (React 19 / TanStack Start)     │
│  - MediaDevices.getUserMedia (camera)       │
│  - <canvas> silhouette + angle overlay      │
│  - useAngleCoach.ts (pose angle detection)  │
│  - Pose catalog (lib/silhouettes.ts)        │
└──────────────────────────────────────────────┘
```

**Android shell**
| Component | Role |
|---|---|
| `WindowInsetsControllerCompat` | Immersive display, content flows around system bars/notch |
| `HapticFeedbackConstants` | Native haptic confirmation on shutter/UI actions |
| Custom `JavascriptInterface` | Decodes Base64/Blob image data from the web layer and writes it to `MediaStore` (`Pictures/PoseCoach`) |

**Web engine**
| Layer | Choice |
|---|---|
| Framework | TanStack Start v1 (React 19, file-based routing) |
| Bundler | Vite 7 |
| Styling | Tailwind CSS v4 |
| Camera | `MediaDevices.getUserMedia` + `<canvas>` overlay |
| Runtime | Cloudflare Workers (Edge SSR) |

## Project structure

```text
src/
├── routes/
│   ├── __root.tsx       # App shell, fonts, SEO defaults
│   ├── index.tsx        # /        → CameraScreen
│   └── library.tsx      # /library → PoseLibrary
├── components/
│   ├── CameraScreen.tsx     # Camera UI, shutter, controls
│   ├── CameraOverlay.tsx    # Silhouette rendered on <canvas>
│   └── AngleCoachOverlay.tsx
├── hooks/
│   ├── useCamera.ts         # Shared getUserMedia stream
│   └── useAngleCoach.ts     # Live angle detection
└── lib/
    └── silhouettes.ts       # Pose catalog
```

## Setup

### Web development
```bash
bun install        # Requires Bun ≥ 1.1 (npm/pnpm also work)
bun run dev         # http://localhost:8080
bun run build       # Production build
```

### Android deployment
1. Open the project in Android Studio Koala (2024.1.1) or newer.
2. Set `TARGET_URL` in `MainActivity.kt` to your deployment URL (or local dev server).
3. Run on a physical device — the camera, haptics, and immersive UI require real hardware and will not behave correctly in the emulator.

### Try it without building
A pre-built **debug** APK is available at [`release/PoseCoach-v1.0-debug.apk`](release/PoseCoach-v1.0-debug.apk). It is unsigned and intended for local testing only — expect an "install blocked" warning on stock Android unless you allow installs from unknown sources.

## Known limitations & roadmap

Being upfront about the current state:

- **Debug build only.** No signed release APK or Play Store listing yet.
- **No demo video yet.** A short screen recording of the live overlay + angle coaching is the next priority — planned for this README.
- **Angle detection scope.** `useAngleCoach` currently tracks tilt/turn/chin/shoulder alignment against the selected silhouette; it does not yet do full-body pose estimation (e.g. joint-level keypoints via a ML model). That's a planned direction, not a current claim.
- **WebView-based, not a fully native UI.** The Android layer hosts a web engine rather than rendering native views; this was a deliberate trade-off for iteration speed, at some cost to raw performance versus a fully native camera pipeline.
- **Single platform.** Android only; no iOS build.

## License

MIT — see [LICENSE](LICENSE).

<div align="center">
<sub>Built by Adarsh Singh</sub>
</div>
