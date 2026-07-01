---
name: NTF Phase 1 Resources Module
description: Architectural decisions and gotchas from the Phase 1 Resources Module implementation
---

## Key decisions

### Folder hierarchy
- `lecture_folders` Firestore collection drives real folders; `parentId` empty = root
- `FolderRepository.getFoldersForSubject()` tries uppercase subject first, falls back gracefully
- If no Firestore folders exist for a subject → fall back to ResourceType grouping (backwards compat)
- `SubjectResourcesViewModel.scopedItems()` is the single source of truth for current scope — never pass `allItems` directly to filters

### Scope-based filtering
- **Real folder**:      `allItems.filter { folderId == currentRealFolderId }`
- **Unfiled group**:    `allItems.filter { folderId.isBlank() && type matches }`
- **Type folder**:      `allItems.filter { type matches }`
- `applyFilters()` always calls `scopedItems()` first, then applies sort/premium/type secondary filters

### PDF viewer URL fallback
- If `localPath` is empty but `fileUrl` is set → download to `context.cacheDir/pdf_cache/` (temp)
- Temp file is deleted when ViewModel is cleared
- FileProvider `file_paths.xml` must include `<cache-path name="pdf_cache" path="pdf_cache/"/>` for sharing

### Lecture resume playback
- DataStore key: `lecture_positions_v1` (stringSet), entry format: `$url||$positionMs`
- Position saved every 5s while playing; also saved in `onCleared()`
- Resume only if saved position > 3000ms (avoids re-seeking to very start)

### PiP
- Manifest: `android:supportsPictureInPicture="true"` + `android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"` on MainActivity
- Code guard: `Build.VERSION.SDK_INT >= Build.VERSION_CODES.O` before calling `enterPictureInPictureMode()`

### Compose / library constraints
- Project uses Material3 only — NO `androidx.compose.material` (M2) dependency
- Do not use M2 `PullRefreshIndicator` / `pullRefresh` — they won't compile
- `androidx-material-icons-extended` is in BOM so all Icons.Rounded.* variants are available
