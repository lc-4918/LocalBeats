# LocalBeats

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android\&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin\&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?logo=jetpackcompose\&logoColor=white)
![Media3](https://img.shields.io/badge/AndroidX-Media3-34A853)
![ExoPlayer](https://img.shields.io/badge/ExoPlayer-1.4-4285F4)
![Room](https://img.shields.io/badge/Room-Database-1976D2)
![DataStore](https://img.shields.io/badge/DataStore-Preferences-1976D2)
![Coil](https://img.shields.io/badge/Coil-Image%20Loading-FF6F00)
![License](https://img.shields.io/badge/License-GPLv3-blue.svg)

A native Android music player (Kotlin + Jetpack Compose + Media3) for playing **locally stored music** on the phone and microSD card. Designed for Android 12
(tested on a Samsung Galaxy S10e), compatible with Android 8.0 and later.

---

## Features

* **Spotify-inspired layout** with a bottom navigation bar and content above.
* **Home** screen showing recently played tracks.
* **Folders** browser for internal storage and microSD, displaying only folders
  containing audio files and only audio files within them.
* **Playlists** stored locally using Room.
* **Search** across all local storage.
* **Context menu** for each track:

  * Add to a playlist
  * Create a new playlist from the track
  * Add to the playback queue
* **Multi-selection** to add multiple tracks to a playlist or queue.
* **Playlist editing**: remove individual or multiple tracks.
* **Now Playing** screen displaying artist, title, repeat mode, and the
  `jpg`/`png` image found in the track's folder (if available).
* **Background playback** with the screen off (Media3 + MediaSession service).
* **Playlist details** with **Play All**, **Shuffle**, **Repeat All**, and a
  custom cover image (`jpg`/`png`).
* Supports **MP3, M4A, WAV, FLAC, OGG**, and other formats supported by
  ExoPlayer/Android codecs.
* Compact multi-line track items with folder artwork on the left and metadata
  on the right; **no empty placeholder** when artwork is unavailable.
* **Round avatar button** in the upper-left corner for quick access to Settings.
* **Settings**:

  * Manual library scan
  * Display customization
  * Light/Dark theme
  * Metadata display options
* Minimal permissions only:

  * `READ_MEDIA_AUDIO` / `READ_MEDIA_IMAGES` on Android 13+
  * `READ_EXTERNAL_STORAGE` on earlier versions
  * **No Internet permission required**

---

## Option A (Recommended) — Build the APK with GitHub Actions

GitHub can compile the application for you, free of charge, in the cloud.

1. Create a free account on GitHub.
2. Create a new **public** repository.
3. Upload the entire project (including the `.github` directory) and commit the files.
4. The build starts automatically. Open the **Actions** tab and wait for the **Build APK** workflow to complete.
   *(If it doesn't start automatically: **Actions → Build APK → Run workflow**.)*
5. Once finished, open the workflow, scroll down to **Artifacts**, and download **`app-debug-apk`**. The ZIP archive contains `app-debug.apk`.

### Installing the APK on a Samsung Galaxy S10e

1. Copy `app-debug.apk` to the phone (USB cable or direct download).
2. Open it with the file manager. Android will ask permission to install apps from unknown sources—allow it.
3. Install **LocalBeats**, grant access to your audio files, then open **Settings → Scan Library** to detect your music.

> The **debug APK** is perfectly suitable for everyday use. The optional
> **release APK** is unsigned and cannot be installed directly; it can safely
> be ignored for personal use.

---

## Option B — Build the Project Yourself with Android Studio

1. Install Android Studio.
2. Select **File → Open** and open the project folder.
3. Wait for Gradle to synchronize and download dependencies.
4. Select **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
5. The generated APK will be located in:

```
app/build/outputs/apk/debug/
```

Android Studio is also the recommended environment for fixing build errors.

---

## Technology Stack

* Kotlin 2.0
* Jetpack Compose
* Media3 / ExoPlayer 1.4 (playback + background playback)
* Room (playlists and history)
* DataStore (settings)
* Coil (image loading)
* Navigation Compose

**Minimum SDK:** 26 (Android 8.0)
**Target SDK:** 34

Project structure:

* `app/src/main/java/com/example/localmusic/` — source code
* `app/src/main/res/` — resources
* `app/src/main/AndroidManifest.xml` — permissions and services

---

## Troubleshooting Build Errors

The source code is complete but has not been compiled or tested on a physical device.

If the build fails:

* **GitHub Actions:** open the failed workflow, expand the **Build Debug APK**
  step, and read the error message (file and line number).
* **Android Studio:** build errors appear in the **Build** window. Clicking an
  error takes you directly to the corresponding line of code, making it the
  easiest environment for debugging.

---

**Application ID:** `com.example.localmusic`

No data is transmitted over the Internet. All music, playlists, and settings
remain entirely on your device.

## License

LocalBeats is free software licensed under the **[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html) (GPL-3.0)**.

You are free to use, study, modify, and redistribute this software under the terms of the GPL-3.0. Any modified or redistributed versions must also be licensed under GPL-3.0.

See the [LICENSE](LICENSE) file for the full license text.
