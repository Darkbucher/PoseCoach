<div align="center">

<img src="metadata/screenshots/banner.png" width="100%" alt="PoseCoach Banner" />

# POSE COACH
### *Perfect Your Form. Elevate Your Presence.*

[![Platform](https://img.shields.io/badge/Platform-Android-00808080?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Engine](https://img.shields.io/badge/Engine-TanStack_Start-C9A84C?style=for-the-badge&logo=react&logoColor=white)](https://tanstack.com/start)
[![Styling](https://img.shields.io/badge/Styling-Tailwind_CSS_v4-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)](https://tailwindcss.com)
[![License](https://img.shields.io/badge/License-MIT-gold?style=for-the-badge)](LICENSE)

---

**PoseCoach** is an ultra-premium hybrid application. It combines a cutting-edge **React 19 / TanStack Start** web engine with a high-performance, immersive **Android shell** to create a seamless "Atelier" experience for creators who demand perfection.

[The Experience](#-the-experience) • [Full Feature List](#-features) • [Tech Stack](#-the-atelier-tech) • [Getting Started](#-getting-started)

</div>

## 📸 The Experience

<table align="center">
  <tr>
    <td width="50%"><img src="metadata/screenshots/camera_view.png" alt="Live Silhouette Guide" /></td>
    <td width="50%"><img src="metadata/screenshots/pose_library.png" alt="Curated Pose Library" /></td>
  </tr>
  <tr>
    <td align="center"><b>Live Silhouette Guidance</b><br/>Real-time positioning with gold-glow overlays.</td>
    <td align="center"><b>Curated Pose Library</b><br/>A boutique selection of forms for every occasion.</td>
  </tr>
</table>

---

## 🌟 Features

*   🪞 **Live Silhouette Overlay** — A translucent mannequin sits on top of the camera feed so you know exactly where to stand.
*   🎯 **Angle Coach** — On-device tilt / turn / chin / shoulder guidance in real time.
*   📚 **Curated Pose Library** — Travel, dating, lifestyle, business, couple, mirror selfie, and more.
*   ✨ **Atelier UI** — Cormorant Garamond serifs, gold accents, glassmorphism, and a shutter button that glows amber on tap.
*   🔒 **100% On-Device** — Frames never leave your phone. No servers, no uploads, no accounts. **Privacy by design.**
*   📸 **One-Tap Capture** — Instant download, saves directly to your native Android gallery.
*   🤏 **Pinch-to-Zoom** — Scale the pose overlay to fit your frame; mirror-aware for front-camera shots.
*   💾 **Persistent Permissions** — The Android shell remembers camera access so you are never interrupted.

---

## 🧱 The Atelier Tech

### **The Android Shell**
The native container provides the high-end foundation:
*   **Immersive Mode:** Uses `WindowInsetsControllerCompat` to flow content into the notch and around system bars.
*   **Haptic Engine:** Native `HapticFeedbackConstants` for physical confirmation of shutter and UI actions.
*   **MediaStore Bridge:** Custom `JavascriptInterface` to decode Base64/Blob data and save it to `Pictures/PoseCoach`.

### **The Web Engine**
The core experience is built with modern, local-first web technologies:

| Layer | Choice |
|---|---|
| **Framework** | **TanStack Start v1** (React 19, file-based routing) |
| **Bundler** | **Vite 7** |
| **Styling** | **Tailwind CSS v4** (Native `@import`, theme tokens) |
| **Typography** | Cormorant Garamond (Display) · Inter (UI) |
| **Camera** | `MediaDevices.getUserMedia` + `<canvas>` overlay |
| **Runtime** | Cloudflare Workers (Edge SSR) |

---

## 🚀 Getting Started

### **Web Development**
```bash
# 1. Install dependencies
bun install

# 2. Run the dev server (http://localhost:8080)
bun run dev

# 3. Production build
bun run build
```
*Requires Bun ≥ 1.1. Node/pnpm/npm also work.*

### **Android Deployment**
1.  Open the project in **Android Studio Koala (2024.1.1)** or newer.
2.  Set `TARGET_URL` in `MainActivity.kt` to your deployment.
3.  Deploy to a physical device to experience the haptics and immersive UI.

---

## 🗂️ Project Structure
```text
src/
├── routes/              # File-based routes (TanStack Start)
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

---

<div align="center">
  <p>Designed with ❤️ by Darkbucher Atelier</p>
  <img src="https://img.shields.io/badge/PRIVACY-100%25_ON_DEVICE-green?style=flat-square" alt="Privacy First" />
  <img src="https://img.shields.io/badge/MADE_WITH-OBSIDIAN_%26_GOLD-C9A84C?style=flat-square" alt="Handcrafted" />
</div>
