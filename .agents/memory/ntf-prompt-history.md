---
name: Next Toppers Feed — Prompt history
description: Android Kotlin/Compose MVVM app; each prompt adds a feature scope. Key decisions and gotchas per prompt.
---

# Next Toppers Feed

Package: com.nexttoppers.feed  
Firebase project: aarambh26-27  
Stack: Kotlin, Jetpack Compose, Firebase Firestore, Hilt DI, MVVM + Repository

## Prompt 6 — XP + Leaderboard + Streak (completed)

### New files created
- `util/LevelUtils.kt` — level thresholds & helpers
- `data/model/LeaderboardEntry.kt`, `Achievement.kt`
- `data/repository/LeaderboardRepository.kt`
- `ui/leaderboard/LeaderboardViewModel.kt`
- `ui/xp/XpComponents.kt`, `ui/achievements/AchievementsSection.kt`

### Key decisions
- Leaderboard derived from `users` collection (no separate collection).
- Weekly leaderboard: filter lastSeen >= 7 days, sort by XP client-side.
- Streak: date-based using Calendar comparison on `lastActive` Timestamp.
- Daily login XP (25 XP): gated by `lastActive` field, once per calendar day.
- **Firestore composite index needed** on first run for weekly leaderboard query.

---

## Prompt 7 — Premium Membership + Access Control (completed)

### New files created
- `data/model/PremiumMembership.kt` — MembershipType/Badge enums, PremiumPlan data, premiumPlans list, `User.toPremiumMembership()` extension
- `data/repository/PremiumRepository.kt` — observe/activate/expire/restore premium via Firestore
- `ui/premium/PremiumViewModel.kt` — membership state, plan selection, purchase flow (placeholder)
- `ui/premium/PremiumComponents.kt` — PremiumBadge, PremiumBannerCard, CurrentMembershipCard, MembershipPlanCard, PremiumBenefitItem, UpgradeDialog, LockedContentCard
- `ui/premium/PremiumScreen.kt` — full premium screen with hero, plan cards, FAQs, purchase button
- `ui/premium/UpgradeSuccessScreen.kt` — animated confetti + crown success screen

### Modified files
- `data/model/User.kt` — added isPremium, premiumType, premiumStart, premiumEnd, premiumActive, membershipBadge
- `ui/theme/Color.kt` — added PremiumGoldGlow, PremiumViolet, PremiumVioletDim, PremiumRose
- `navigation/NavGraph.kt` — replaced PREMIUM placeholder with PremiumScreen, added UPGRADE_SUCCESS route
- `ui/home/HomeScreen.kt` — PremiumBannerCard replaces old banners, user.premium → user.isPremium
- `ui/profile/ProfileScreen.kt` — PremiumBadge next to name, CurrentMembershipCard after level card

### Key architectural decisions
- **User.premium kept** as legacy backward-compat field; `isPremium` is the new source of truth.
- `User.toPremiumMembership()` is an extension in `PremiumMembership.kt` — reusable from any composable without injecting PremiumRepository.
- HomeViewModel and ProfileViewModel NOT modified — premium state derived inline from User object.
- Purchase is a placeholder (1.8s delay then Firestore write) — real payment gateway in a future prompt.
- Expiry detection runs on app start (PremiumViewModel.checkExpiry()) without Cloud Functions.
- `LockedContentCard` uses alpha(0.12f) + frosted overlay instead of BlurMaskFilter (API 31+ only).

### NOT built yet (next prompt)
- Real payment gateway (Razorpay/Stripe)
- Push notification for expiry warning
- Admin analytics
