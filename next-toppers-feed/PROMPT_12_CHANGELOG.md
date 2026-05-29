# Prompt 12 — Final Production Polish Changelog
**Version:** 2.0.0 (versionCode 12)  
**Date:** May 2026

## Summary
Final production polish pass for Next Toppers Feed. All changes maintain full backward compatibility with Prompts 1–11.

---

## 🏗️ Build & Release

### `app/build.gradle.kts`
- **versionCode** bumped to `12`, **versionName** to `"2.0.0"`
- Added `BuildConfig` fields: `APP_NAME`, `APP_VERSION`, `FIREBASE_PROJECT`, `SUPPORT_EMAIL`, `ENABLE_CRASH_LOGGING`, `IS_DEBUG`
- Added `freeCompilerArgs` opt-in annotations for Material3 & Foundation APIs
- Configured **AAB bundle splits** (language, density, ABI) for Play Store
- Added `ndk.debugSymbolLevel = "SYMBOL_TABLE"` for crash symbol upload
- Added lint config (`abortOnError = false`, `checkReleaseBuilds = false`)
- Documented release signing placeholder with CI/CD env var instructions

### `app/proguard-rules.pro`
- Finalized all ProGuard/R8 rules for production release
- Added FCM service keep rule, Hilt generated components
- Added coroutine volatile fields rule
- Added `assumenosideeffects` to strip `Log.d/v/i` in release builds
- Added `-allowaccessmodification` and `-repackageclasses 'ntf'` for maximum APK size reduction
- Added source file attribute retention for crash reports

---

## 🎨 UI / Component Layer

### `ui/components/LoadingSystem.kt` *(NEW)*
Centralized loading architecture — 18 composables:
- `NtfLoadingDialog` — blocking modal dialog with spinner
- `NtfFullscreenLoadingOverlay` — covers content with fade animation
- `InlineLoadingRow` — inline list footer spinner
- `NtfProgressBar` — neon gradient deterministic progress bar
- `NtfLinearLoadingBar` — indeterminate neon linear indicator
- `PulsingSkeletonBlock` — generic pulsing skeleton
- `ShimmerChatRow` — chat message shimmer variant
- `ShimmerLeaderboardRow` — leaderboard entry shimmer
- `ShimmerAdminStatCard` — admin dashboard stat shimmer
- `ShimmerNotificationRow` — notification center shimmer
- `ShimmerPremiumCard` — premium plan card shimmer
- `ShimmerDownloadCard` — downloads library shimmer
- `NtfShimmerList` — flexible count shimmer list
- `NtfSpinner` — pulsing glow circular indicator
- `OfflineChip` — floating offline status chip
- `NtfContentState<T>` — universal content/loading/error/offline/empty wrapper
- `NtfState<T>` — sealed interface for all UI states
- `NtfTransferProgress` — upload/download progress row
- `PulsingBadge` — animated notification badge

### `ui/components/ErrorComponents.kt` *(UPDATED)*
- Added `FullScreenError` — animated error state with icon pulse
- Added `FullScreenOffline` — animated offline state with wifi icon
- Added `FullScreenLoading` — centered loading screen
- Added `EmptyState` — configurable empty state with action button
- Added `ErrorBanner` — inline error banner with retry
- Added `SuccessToast` — auto-dismiss 2.5s success notification
- Added `OfflineBanner` — top-strip offline indicator
- Added `PremiumGateCard` — premium locked content gate
- Added `NtfErrorDialog` — error AlertDialog
- Added `NetworkErrorStrip` — full-width network error strip with retry
- Fixed `ErrorRed` — now imports from theme, no local re-declaration

---

## 📱 Play Store Readiness

### `ui/legal/ReportIssueScreen.kt` *(NEW)*
- Full report-issue flow with 5 issue categories (Bug, Performance, Content Error, Network, General)
- Pre-filled email template with category, description, and app metadata
- Character counter (500 max)
- Alternative contact links (email + website)
- Accessible category selector with icons and color-coding

### `ui/legal/AppFeedbackScreen.kt` *(NEW)*
- 5-star rating system with emoji labels
- Free-text feedback (400 max) with character counter
- Quick-tag chip selector (6 preset tags)
- Smart Play Store rating prompt (only shown for 4-5 star ratings)
- Pre-filled email template with rating and feedback
- Full neon dark theme with PremiumGold accent

---

## ⚙️ Settings Screen

### `ui/settings/SettingsScreen.kt` *(UPDATED)*
New sections and navigation rows:
- **Support & Feedback** section with:
  - "Report an Issue" → `ReportIssueScreen`
  - "App Feedback" → `AppFeedbackScreen`
  - "Help & FAQ" → `nexttoppers.in/help`
  - "Share App" → system share intent
  - "Rate on Play Store" → `market://details?id=...`
- **Downloads** section with "Offline Library" quick nav
- Improved sign-out flow with confirmation dialog
- Improved account deletion dialog with email-based instruction
- App version card with Firebase project display
- Removed unused `Logout` icon import

---

## 🧭 Navigation

### `navigation/NavGraph.kt` *(UPDATED)*
New routes added:
- `Routes.REPORT_ISSUE = "legal/report_issue"` → `ReportIssueScreen`
- `Routes.APP_FEEDBACK = "legal/feedback"` → `AppFeedbackScreen`

Bug fixes:
- `Routes.NOTES` / `Routes.LECTURES` quick-action routes now correctly redirect to `subjectResources("MATHS")` / `subjectResources("SCIENCE")` using `LaunchedEffect`, fixing the incorrect `subject` parameter that was being passed to `SubjectResourcesScreen` (which only accepts arguments via `SavedStateHandle`)
- `navigateSafe()` extension now logs failures via `AppLogger.error()`

---

## 🛠️ Utilities

### `util/AppLogger.kt` *(UPDATED)*
New methods:
- `error()` — alias for `e()`, used by nav-graph error handler
- `firestoreRead()` — logs Firestore read operations
- `firestoreWrite()` — logs Firestore write operations
- `uiEvent()` — logs UI interaction events

---

## 📦 Resources

### `res/values/strings.xml` *(UPDATED)*
- Added comprehensive string table: 60+ strings
- Support contact strings (`support_email`, `website_url`, `play_store_url`)
- Settings labels for all new sections
- Error message strings for global error UX
- Loading message variants
- Empty state strings for all screens
- Accessibility content descriptions (12 `cd_` prefixed strings)
- Share text template

---

## ✅ Compatibility
- Zero breaking changes to Prompts 1–11 screens
- All new Kotlin files are in appropriate packages
- No new Gradle dependencies required
- ProGuard rules are strictly additive
