# DLP Mobile

A minimal Android app (Kotlin + Jetpack Compose) that wraps **yt-dlp** to
download videos directly on your phone — no PC required.

## Features (v1)
- Paste any video URL (or share from YouTube / a browser)
- Fetch available formats via `yt-dlp --list-formats`
- Pick your preferred format from a grouped list
- Download to `Downloads/` with a live progress log

## Project structure

```
app/src/main/
├── assets/bin/<abi>/yt-dlp   ← you must add the binary (see below)
├── kotlin/com/dlpmobile/
│   ├── MainActivity.kt        ← single activity, NavHost wiring
│   ├── core/
│   │   ├── BinaryManager.kt  ← extracts + chmod the bundled binary
│   │   ├── YtDlpRunner.kt    ← ProcessBuilder wrapper, Flow<String> output
│   │   └── Format.kt         ← data class for a single format entry
│   ├── vm/
│   │   └── MainViewModel.kt  ← all state, format fetch + download logic
│   └── ui/
│       ├── HomeScreen.kt
│       ├── FormatPickerScreen.kt
│       ├── DownloadScreen.kt
│       └── theme/Theme.kt
└── res/
    └── values/
        ├── strings.xml
        └── themes.xml
```

## ⚠️ Adding the yt-dlp binary

The app bundles a pre-compiled yt-dlp binary for ARM/x86 devices.
You must supply the binary yourself:

1. **Download** the static ARM64 build from the [yt-dlp releases page](https://github.com/yt-dlp/yt-dlp/releases):
   - `yt-dlp_linux_aarch64` → rename to `yt-dlp`

2. **Place** it at:
   ```
   app/src/main/assets/bin/arm64-v8a/yt-dlp
   ```
   Optionally also add:
   - `app/src/main/assets/bin/armeabi-v7a/yt-dlp`  (32-bit ARM)
   - `app/src/main/assets/bin/x86_64/yt-dlp`       (emulator / Intel)

3. The `BinaryManager` extracts it to `filesDir` on first launch and makes it executable. No root required.

> **Note**: yt-dlp itself requires Python to run in most distributions.
> The static Linux ARM64 build (`yt-dlp_linux_aarch64`) bundles everything
> and runs without Python on the device. Always use the `_linux_aarch64`
> variant, not the `.py` script.

## Building

```bash
./gradlew assembleDebug
```

Requires Android Studio Hedgehog or newer (AGP 8.5, Kotlin 2.0).

## Permissions

| Permission | Why |
|---|---|
| `INTERNET` | yt-dlp fetches video info and streams |
| `WRITE_EXTERNAL_STORAGE` (≤ API 28) | Save to Downloads on older Android |

On Android 10+ files are saved via `Environment.DIRECTORY_DOWNLOADS`
which doesn't need the storage permission.
