# E-ink OS / Nothing OS Launcher

A minimalist, monochrome Android launcher & web prototype inspired by Nothing OS and E-ink aesthetics. Built with Jetpack Compose & pure HTML/CSS/JS.

![Monochrome Theme](assets/wallpaper.jpg)

---

## 📱 Features

- **Monochrome / E-ink Aesthetic**: Pure grayscale design system (#141414, #E9E7E2, true blacks and off-whites).
- **Dot-Matrix Engine**: Custom 5x7 dot-matrix typography clock and widgets.
- **Interactive Widgets**:
  - Dot-matrix, Grotesk, and Analog clock faces
  - Quick glance weather & moon phases
  - Quick action toggles (DND, Flashlight, Wi-Fi, Flight mode)
  - Earbuds (Nothing Ear style) battery & connection status
  - Minimalist pedometer & battery meter
  - App drawer with category tabs and fast search
- **Multi-Screen Horizontal Pager**:
  - Screen 1: Quick Tiles & Device Info
  - Screen 2: Main Clock, Weather & Quick Apps
  - Screen 3: Productivity & Tasks

---

## 🛠️ Project Structure

```
E-inkOs/
├── index.html                   # Interactive Web Prototype
├── tokens.css                   # Shared CSS design tokens
├── tokens.json                  # Design system tokens in JSON
├── assets/                      # Wallpaper and imagery assets
├── android/                     # Android Jetpack Compose Project
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/example/e_inkoslauncher/
│   │       ├── MainActivity.kt
│   │       ├── theme/           # Color, Theme, Typography
│   │       ├── data/            # Data models and repository
│   │       └── ui/main/         # MainScreen and ViewModel
│   ├── build.gradle.kts
│   └── settings.gradle.kts
└── README.md
```

---

## 🚀 Getting Started

### 1. Web Prototype
Simply open `index.html` in any modern web browser or serve locally:
```bash
npx serve .
```

### 2. Android App (Jetpack Compose)
Open the `android/` directory in Android Studio or compile using Gradle:
```bash
cd android
./gradlew assembleDebug
```
The output APK will be generated at:
`android/app/build/outputs/apk/debug/app-debug.apk`

---

## 🔄 Syncing with GitHub from VS Code

1. Make any edits to your code in VS Code.
2. In the VS Code **Source Control** tab (or press `Ctrl+Shift+G`):
   - Stage your changes (click `+` next to files or "Stage All Changes").
   - Type a commit message into the box.
   - Click **Commit** (or `Ctrl+Enter`).
   - Click **Sync Changes** (or **Push**) to update GitHub.
3. Or using terminal:
   ```bash
   git add .
   git commit -m "Your update message"
   git push
   ```
