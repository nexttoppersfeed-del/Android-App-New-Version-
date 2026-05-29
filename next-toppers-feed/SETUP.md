# Next Toppers Feed — Setup Guide (Prompts 1–4)

## Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android device or emulator (API 26+)

## Firebase Configuration (already wired in code)

| Config | Value |
|---|---|
| Package name | `com.nexttoppers.feed` |
| Firebase Project ID | `aarambh26-27` |
| Project Number | `42465208642` |
| Android App ID | `1:42465208642:android:3eec9300483e1eaa1c40db` |
| API Key | `AIzaSyC1NI-YW0VEkSF_ZL4MefWoLKQVjSuY6Sw` |
| Web Client ID | `42465208642-8pr419mkv4b1rd2s2fs45v8t0g36gm5t.apps.googleusercontent.com` |

---

## Two Manual Steps (you do these)

### Step 1 — google-services.json
1. https://console.firebase.google.com/ → project **aarambh26-27**
2. Project Settings → General → Your apps → Android app
3. Download `google-services.json`
4. Replace `app/google-services.json` with the downloaded file

### Step 2 — SHA-1 Fingerprint
```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android -keypass android
```
Copy SHA-1 → Firebase Console → Project Settings → Android app → Add fingerprint → Save → re-download `google-services.json`.

---

## Firebase Console Setup

### Authentication
Firebase Console → Authentication → Sign-in method → Google → Enable

### Firestore
Firebase Console → Firestore Database → Create database → Production mode

**Firestore Security Rules (covers all prompts so far):**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
    match /announcements/{id} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    match /resources/{id} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

### Resources Collection (Firestore)
Collection: **resources** — add test documents with these fields:

```
title        (string)
subject      (string) — MATHS | SCIENCE | SST | ENGLISH | HINDI | PREMIUM
type         (string) — NOTES | PDF | MODULE | DPP | LECTURE | PRACTICE
description  (string)
thumbnailUrl (string) — leave empty or use an image URL
fileUrl      (string) — HTTPS URL to the actual PDF/video file
premium      (boolean)
views        (number) — default 0
downloads    (number) — default 0
uploadedBy   (string) — "Admin"
duration     (string) — "45 min" for lectures, empty otherwise
pageCount    (number) — for PDFs; 0 otherwise
tags         (array of strings)
createdAt    (timestamp)
```

**Example — free notes:**
```json
{
  "title": "Algebra Basics — Chapter 1",
  "subject": "MATHS",
  "type": "NOTES",
  "description": "Complete notes on algebraic expressions and equations.",
  "fileUrl": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
  "premium": false,
  "views": 0, "downloads": 0,
  "uploadedBy": "Admin", "duration": "", "pageCount": 24,
  "tags": ["algebra", "class10"]
}
```

**Example — premium lecture:**
```json
{
  "title": "Newton's Laws of Motion — Full Lecture",
  "subject": "SCIENCE",
  "type": "LECTURE",
  "description": "Complete video lecture with solved examples.",
  "fileUrl": "",
  "premium": true,
  "views": 0, "downloads": 0,
  "uploadedBy": "Admin", "duration": "52 min", "pageCount": 0,
  "tags": ["physics", "newton"]
}
```

---

## Architecture — What Each Prompt Added

### Prompt 1 — Foundation
- Kotlin + Jetpack Compose + Material 3 dark theme
- Firebase Auth (Google Sign-In via Credential Manager)
- Hilt DI, MVVM, Firestore user profiles
- Splash / Home / Profile / Settings / bottom-nav shell

### Prompt 2 — Polish
- Real Firebase credentials wired in (`google-services.json`, Web Client ID)
- Announcements system (realtime Firestore Flow)
- Shimmer skeletons, glassmorphism cards, SettingsScreen toggles

### Prompt 3 — Resources System

```
data/model/Resource.kt              — ResourceSubject (6) + ResourceType (6) enums + data class
data/repository/ResourcesRepository.kt  — Firestore realtime Flow, filter, view-count
ui/resources/
├── ResourcesScreen.kt              — Categories dashboard + debounced search overlay
├── ResourcesViewModel.kt
├── SubjectResourcesScreen.kt       — Per-subject list/grid + type + premium filters
├── SubjectResourcesViewModel.kt
├── ResourceDetailScreen.kt         — Hero, stats, action buttons
├── ResourceDetailViewModel.kt
└── components/
    ├── ResourceCard.kt
    ├── CategoryCard.kt
    └── SearchFilterBar.kt
```

### Prompt 4 — PDF Viewer + Downloads + Offline Library

```
data/model/DownloadedResource.kt    — Local download record + DownloadStatus sealed class
data/model/RecentlyOpened.kt        — Recently-opened record
data/repository/DownloadRepository.kt  — DownloadManager + Firestore downloads counter
data/repository/OfflineRepository.kt   — DataStore persistence (downloads + recents)
di/AppModule.kt                     — Added DataStore<Preferences> singleton
ui/pdf/
├── PdfViewerScreen.kt              — Native PdfRenderer + HorizontalPager + pinch-zoom
└── PdfViewerViewModel.kt           — Opens PDF file, renders pages to Bitmap, caches ±3 pages
ui/downloads/
├── DownloadsScreen.kt              — Offline library: stat cards, recents, downloaded files list
└── DownloadsViewModel.kt
ui/resources/ResourceDetailScreen.kt  — Upgraded: download progress bar, open PDF button, completed banner
ui/resources/ResourceDetailViewModel.kt  — Integrated DownloadRepository + OfflineRepository
navigation/NavGraph.kt              — + DOWNLOADS route + PDF_VIEWER (query-param route)
AndroidManifest.xml                 — Storage permissions + FileProvider
```

**Download flow:**
1. User taps "⬇ Download for Offline" on `ResourceDetailScreen`
2. `ResourceDetailViewModel.startDownload()` calls `DownloadRepository.startDownload(resource)`
3. Android `DownloadManager` downloads the file to `getExternalFilesDir()/Downloads/{Subject}/{title}.pdf`
4. `observeProgress()` Flow emits `Queued → Progress(%) → Completed` every 500ms
5. On completion → saved to DataStore via `OfflineRepository` + Firestore `downloads` field incremented
6. "⬇ Download" button becomes "✓ Downloaded" + "📖 Read PDF" appears
7. Tap "📖 Read PDF" → navigates to `PdfViewerScreen` with the local file path

**PDF rendering:**
- Uses Android's built-in `android.graphics.pdf.PdfRenderer` (no WebView, no extra library)
- Each page rendered to `Bitmap` in a `Dispatchers.IO` coroutine
- `HorizontalPager` for swipe navigation between pages
- `detectTransformGestures` for pinch-to-zoom (1× → 5×)
- Bitmap cache: keeps at most 6 pages in memory, recycles the rest
- Tap anywhere to toggle top/bottom chrome (title + page indicator)

**Offline Library (`/downloads`):**
- Lists all downloaded files with subject chip, size label, download date
- Recently Opened section: last 20 items (PDF opens or detail views)
- Storage stats: file count, total disk used, opens count
- Per-item delete (removes file from disk + DataStore entry)
- Clear All dialog

**Permissions:**
- `WRITE_EXTERNAL_STORAGE` (only API ≤ 28; not needed on API 29+)
- `READ_EXTERNAL_STORAGE` (only API ≤ 32)
- App-specific external storage (`getExternalFilesDir()`) — no runtime permission on API 26+
- `FileProvider` configured for future share intent support

---

## Navigation Map (after Prompt 4)

```
splash
├── login
└── home (shell)
    ├── [tab] home
    ├── [tab] resources
    │   ├── resources/subject/{subject}
    │   │   └── resources/detail/{resourceId}
    │   │       └── pdf_viewer?resourceId=&title=&localPath=
    │   └── resources/detail/{resourceId}
    │       └── pdf_viewer?...
    ├── [tab] leaderboard  (placeholder)
    ├── [tab] chats        (placeholder)
    ├── [tab] profile
    ├── settings
    └── downloads          (Offline Library)
        └── pdf_viewer?...
```

---

## Remaining Prompts

| Prompt | What it adds |
|---|---|
| 5 | Live Leaderboard + XP system + streaks |
| 6 | Real-time group chats + DMs + FCM notifications |
| 7 | ExoPlayer video player + lecture system |
| 8 | Premium subscriptions (Google Play Billing) |
| 9 | Admin upload tools + content management |
