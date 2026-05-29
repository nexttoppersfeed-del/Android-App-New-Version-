# Firestore Shared Architecture
**Firebase Project:** `aarambh26-27`  
**Last Updated:** Prompt 12  
**Platforms:** Next Toppers Feed Android App + Next Toppers Feed Website

---

## Philosophy

One Firebase project. One Firestore database. One set of security rules. Both platforms share identical collection names, field names, and queries. There is **no Firebase Storage** — all files are external URLs or lightweight Base64 in Firestore.

---

## What Is NOT Used

| Removed | Reason |
|---|---|
| Firebase Storage SDK | Free-tier: 5 GB / 1 GB download limit — insufficient for PDFs & videos |
| `storage.rules` | Replaced with deny-all stub (see `storage.rules`) |
| Storage upload flows | All files are external CDN/hosted links |

---

## Admin Authority — Two Mechanisms, One Rule

Admin status is checked by the Firestore rules in this order (short-circuit `||`):

1. `users/{uid}.isAdmin == true` — **Android primary**
2. `admins/{uid}.role in ['admin', 'owner']` — **Website primary**

**Important:** When granting admin access via the website admin panel, **write to BOTH**:
- `users/{uid}` → set `isAdmin: true`
- `admins/{uid}` → set `role: "admin"` (or `"owner"`)

This keeps both platforms synchronized without extra Firestore reads in the rules.

---

## Premium Status — Single Source of Truth

Premium is controlled by fields on `users/{uid}`:

| Field | Type | Purpose |
|---|---|---|
| `isPremium` | boolean | Is user a premium member? |
| `premiumActive` | boolean | Is membership currently valid? |
| `premiumEnd` | Timestamp | Membership expiry date |
| `premiumStart` | Timestamp | Membership start date |
| `premiumType` | string | `"weekly"` / `"monthly"` / `"yearly"` / `"lifetime"` |

Rules check **both** `isPremium == true AND premiumActive == true` for access.

---

## Base64 Image Policy

| Use Case | Field Name | Max Decoded Size | Rule Enforced |
|---|---|---|---|
| Payment proof screenshot | `paymentScreenshotBase64` | 500 KB | ✅ 700 000 chars |
| Announcement banner | `bannerBase64` | 200 KB | ❌ App-enforced |
| Subject popup image | `popupImageBase64` | 200 KB | ❌ App-enforced |
| Small thumbnail | `thumbnailBase64` | 100 KB | ❌ App-enforced |

**Android:** Use `Base64ImageUtils` (`util/Base64ImageUtils.kt`) to encode and validate.  
**Website:** Compress images before encoding (target < 500 KB decoded for screenshots).

**Never Base64:** PDFs, videos, audio, profile photos, galleries.

---

## Collection Reference

### Shared Collections (Android + Website)

| Collection | Purpose | Who Writes |
|---|---|---|
| `users/{uid}` | User profile, premium status, admin flag | User (own), Admin |
| `leaderboard/{uid}` | XP rankings | User (own), Admin |
| `premiumRequests/{id}` | Payment requests with Base64 screenshot | User (create), Admin (update) |
| `resources/{id}` | Study material metadata + external `downloadUrl` | Admin only |
| `announcements/{id}` | Push announcements with optional `bannerBase64` | Admin only |
| `notifications/{id}` | Per-user push notifications | Admin (create), User (read/delete own) |
| `quizzes/{id}` | Quiz metadata, premium-gated | Admin only |
| `quizAttempts/{id}` | User quiz results | User (own) |
| `comments/{id}` | Post comments | User |
| `replies/{id}` | Comment replies | User |
| `reports/{id}` | Moderation reports | User (create), Admin |
| `moderationLogs/{id}` | Admin action log | Admin only |
| `appFeedback/{id}` | In-app bug reports and feedback | User (create), Admin |

### Android-Only Collections

| Collection | Purpose |
|---|---|
| `chats/{chatId}/messages/{id}` | 1-on-1 DM threads (participants array) |
| `communityPosts/{id}` | Threaded community feed posts |
| `groups/{groupId}/messages/{id}` | Study group chats |
| `activityFeed/{id}` | XP/achievement activity stream |
| `quizHistory/{id}` | Historical quiz stats per user |
| `questions/{id}` | Quiz question bank (flat collection) |

### Website-Only Collections

| Collection | Purpose |
|---|---|
| `admins/{uid}` | Admin role registry (role: admin/owner) |
| `communityMessages/{id}` | Global community chat (flat, real-time) |
| `privateChats/{chatId}/messages/{id}` | Private chat (chatId = uid1_uid2) |
| `privateChatMeta/{chatId}` | Private chat metadata, last message |
| `premiumUsers/{uid}` | Approved premium member registry |
| `contactMessages/{id}` | Contact form submissions |
| `subjects/{id}` | Course subject catalogue |
| `lecture_folders/{id}` | Lecture folder hierarchy |
| `files/{id}` | File metadata (external `downloadUrl`) |
| `lectures/{id}` | Lecture content + YouTube metadata |
| `yt_channels/{id}` | YouTube channel links |
| `branding/{id}` | Site branding config |
| `siteSettings/{id}` | Global site configuration |
| `subjectPopups/{subjectId}` | Subject popup content + `popupImageBase64` |
| `siteBanners/{id}` | Site banners with optional `bannerBase64` |
| `coupons/{id}` | Discount coupon codes |
| `couponUsage/{id}` | Coupon redemption tracking |
| `tests/{id}` | Online test definitions |
| `testAttempts/{id}` | Test attempt records |
| `ratings/{key}/users/{uid}` | Per-user rating data |
| `ratings/{key}/meta/{doc}` | Aggregate rating metadata |

---

## Resource Document Schema

Resources do NOT use Firebase Storage. All files are external links.

```
resources/{resourceId}
├── title:        string
├── description:  string
├── subject:      string  (e.g. "MATHS", "SCIENCE")
├── type:         string  (e.g. "NOTES", "SOLUTION", "BOOK")
├── premium:      boolean (gates access via Firestore rules)
├── downloadUrl:  string  (external URL — CDN / website-hosted / direct link)
├── thumbnailUrl: string  (external URL or lightweight Base64 thumbnail)
├── views:        number
├── downloads:    number
└── createdAt:    Timestamp
```

---

## premiumRequests Document Schema

```
premiumRequests/{requestId}
├── userId:                   string
├── username:                 string
├── userEmail:                string
├── plan:                     string  ("weekly" | "monthly" | "yearly" | "lifetime")
├── amount:                   string  (display string e.g. "₹299")
├── utrId:                    string  (UPI transaction reference)
├── paymentScreenshotBase64:  string  (Base64 JPEG, max 700 000 chars)
├── status:                   string  ("PENDING" | "APPROVED" | "REJECTED")
├── createdAt:                Timestamp
├── reviewedAt:               Timestamp | null
├── reviewedBy:               string  (admin uid)
└── adminNote:                string
```

---

## Free-Tier Checklist

| Feature | Free Tier Limit | Status |
|---|---|---|
| Firestore reads | 50 000/day | ✅ Cached + paginated |
| Firestore writes | 20 000/day | ✅ Batched where possible |
| Firestore storage | 1 GB | ✅ Metadata only, no binaries |
| Firebase Auth | Unlimited | ✅ |
| FCM (notifications) | Unlimited | ✅ |
| Firebase Storage | N/A | ✅ **Removed** |

---

## Deploying Rules

```bash
# Deploy Firestore rules only (from project root)
firebase deploy --only firestore:rules --project aarambh26-27

# Deploy everything (rules + indexes)
firebase deploy --only firestore --project aarambh26-27
```

Rules file: `next-toppers-feed/firestore.rules`

---

## Migration Notes (Prompt 12)

- `paymentScreenshot` field renamed to `paymentScreenshotBase64` in `PremiumRequest` model and Firestore documents
- Existing documents with `paymentScreenshot` field will still be readable (Kotlin data class has default empty string for the old field)
- New submissions always use `paymentScreenshotBase64`
- Firebase Storage SDK dependency removed from Android `build.gradle.kts`
- `FirebaseStorage` provider removed from Hilt `AppModule`
