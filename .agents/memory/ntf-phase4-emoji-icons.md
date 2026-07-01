---
name: NTF Phase 4 Material 3 UI Overhaul
description: Phase 4A visual redesign — all neon/dark gaming UI replaced with proper Material 3 components across the full app.
---

## Summary
Pure visual redesign of Next Toppers Feed Android app (Kotlin/Compose). No feature changes. All screens modernized to 2026 Google-quality Material 3 Expressive standard.

## Key Pattern Replacements Applied (App-Wide)
- `NtfCard` (Box+border) → M3 `Card`/`ElevatedCard` with `BorderStroke`
- `NtfGradientCard` → `ElevatedCard`
- `NtfPrimaryButton` → M3 `Button` (FilledButton)
- `NtfOutlinedButton` → M3 `OutlinedButton`
- `NeonDivider` → `HorizontalDivider`
- Box+clip+background+border clickable patterns → `Card(onClick=...)` or `Surface`
- `NtfSpinner` → `CircularProgressIndicator`
- `XpBadge` → `SuggestionChip`
- Custom gradient text titles → plain `Text` with `color = TextPrimary`
- `background(BackgroundBlack)` on fullscreen Box → removed (theme provides background)
- Gradient card container backgrounds → M3 token `containerColor`
- Custom Tab rows (Row+clip+gradient indicator) → M3 `TabRow`/`Tab`
- Box icon buttons → M3 `IconButton`
- Box+background+border badges → `Surface(shape=RoundedCornerShape(...))`

## Files Fully Rewritten / Major Changes
- `ui/components/CommonComponents.kt` — all shared wrappers modernized
- `ui/resources/components/ResourceCard.kt` — all card variants to M3 Card
- `ui/xp/XpComponents.kt` — GlowingLevelCard→ElevatedCard, StreakCard→Card, RankCard→Card, LevelUpDialog→Dialog+ElevatedCard+Button
- `ui/premium/PremiumComponents.kt` — all premium cards/badges to M3 Card+Surface; UpgradeDialog→Dialog+ElevatedCard+Button
- `ui/announcements/AnnouncementCarousel.kt` — PinnedBanner→OutlinedCard, CarouselCard→Card, Ticker→Surface

## Files Partially Edited
- `ui/chats/ChatsScreen.kt` — ConnectTabs→M3 TabRow, ConnectTopBar buttons→IconButton
- `ui/leaderboard/LeaderboardScreen.kt` — TabRow custom→M3, YourRankStrip→Card, PodiumSection→ElevatedCard, LeaderboardRow→Card
- `ui/downloads/DownloadsScreen.kt` — gradient title removed, Color.Red→error token, cards→M3 Card
- `ui/components/ErrorComponents.kt` — RetryButton→FilledTonalButton, SuccessToast→ElevatedCard, PremiumGateCard→OutlinedCard, fullscreen backgrounds removed
- `ui/achievements/AchievementsSection.kt` — AchievementChip→Card, counter badge→Surface
- `ui/home/HomeScreen.kt` — SubjectsGrid and QuickActionButton icon boxes→Surface, fullscreen background removed
- `ui/settings/SettingsScreen.kt` — gradient title removed, ambient glow removed, version badge→Surface, nav row icon→Surface, fullscreen background removed
- `ui/profile/ProfileScreen.kt` — gradient title removed, fullscreen background removed
- `ui/resources/ResourcesScreen.kt` — SubjectCard→Card, FreeResourcesBanner→Card

## Critical Notes for Future Edits
- **Always add extra closing brace** when wrapping existing `Box {...}` content inside `Card { Box { ... } }` — the Card adds one lambda layer.
- `PremiumComponents.kt` `CurrentMembershipCard` had a brace bug (extra `}`) — fixed by removing one `}` after the Card's Column.
- Accent/brand colors intentionally kept: `PremiumGold`, `PremiumViolet`, `NeonGreen` for progress bars, `ErrorRed`, subject-specific semantic colors (AccentBlue, AccentEmerald, etc.)
- Never touch: Firebase, ViewModel, Navigation, Repository, DataModel layers — Phase 4A is visual only.
- `achievement.icon` is rendered as `Text(achievement.icon)` — do NOT change this (it may be an emoji String).
- Screens NOT yet updated: Admin screens, NotificationCenter, PdfViewerScreen, QuizScreens, ChatScreen, CommunityScreen, LecturePlayerScreen, TestsScreen, ActivityFeedScreen, LoginScreen, ResourceDetailScreen, SubjectResourcesScreen (partially done).
