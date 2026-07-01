---
name: Next Toppers Feed — Prompt history and conventions
description: Android Kotlin/Compose app; key conventions for Firestore deserialization safety and incremental feature work.
---

## App overview
Android Kotlin/Jetpack Compose app with Firebase/Firestore backend. Website (React/Next.js) uses same Firestore collections but sometimes with different field names or types — the source of mixed Timestamp/String bugs.

## Firestore deserialization safety conventions

### The pattern
Any model deserialized via `toObject()` that has a Timestamp field must:
1. Add `@get:Exclude @field:Exclude` to the field in the data class (BOTH annotations always together — never just one).
2. Import `com.google.firebase.firestore.Exclude` in the model file.
3. At every `toObject(Foo::class.java)` call site, immediately chain `.copy(field = doc.resolveTimestamp("field"))`.
4. Import `com.nexttoppers.feed.util.resolveTimestamp` in the repository file.

**Why:** Firestore's `toObject()` crashes with `RuntimeException: Failed to convert value of type Timestamp to String` (or vice versa) if a field's stored type doesn't match the Kotlin type. Old app versions and the website may have written fields as Strings that newer schema expects as Timestamps (and vice versa).

### Utility location
`util/UserDocumentExt.kt` — contains:
- `DocumentSnapshot.resolveLastActive()` — handles Timestamp→String or String→String for `lastActive`
- `DocumentSnapshot.resolveTimestamp(fieldName, default)` — handles Timestamp→Timestamp or String→Timestamp for any field; default is `Timestamp.now()`

**Thread safety:** Both helpers create `SimpleDateFormat` per call (not shared). Do not refactor to a shared instance.

### Safe models (manual mapping — no toObject() crash risk)
- Chat, ChatMessage (ChatRepository.mapToChat/mapToMessage)
- CommunityPost (CommunityRepository.mapPost)
- NtfTest (TestRepository.mapTest)
- TestAttempt in TestRepository (TestRepository.mapAttempt — but XpRepository uses toObject, patched)
- Quiz, QuizAttempt (QuizRepository.mapDoc)
- PremiumRequest (PremiumRequestRepository.mapRequest)
- PremiumMembership (PremiumRepository.mapMembership)
- LeaderboardEntry
- Resource in ResourcesRepository (uses manual mapFile/mapLecture — only ResourceManagementRepository uses toObject)

### Patched models (toObject() path, both annotations + resolveTimestamp applied)
- User: lastActive (resolveLastActive), lastSeen, createdAt, updatedAt
- Announcement: createdAt
- Comment: createdAt
- Reply: createdAt
- NtfNotification: timestamp
- ActivityFeedItem: timestamp
- Group: createdAt
- TestAttempt: completedAt (XpRepository only)
- Resource: createdAt (ResourceManagementRepository only)

### Default value note
`Timestamp.now()` is used as the default for missing/unparseable fields — consistent with existing manual mapper defaults. This is semantically lossy (fabricates recency) but avoids crashes. For write operations, always use real Timestamps.

## General conventions
- Feature additions are incremental; never rewrite architecture.
- Writes always use proper Timestamps — only reads need the compat layer.
