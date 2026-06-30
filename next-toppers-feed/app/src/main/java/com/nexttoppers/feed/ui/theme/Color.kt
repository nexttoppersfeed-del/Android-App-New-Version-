package com.nexttoppers.feed.ui.theme

import androidx.compose.ui.graphics.Color

// ── Material 3 Primary Palette — Blue ──────────────────────────────────────────
val Md3Blue10   = Color(0xFF001849)
val Md3Blue20   = Color(0xFF002D96)
val Md3Blue30   = Color(0xFF0043C9)
val Md3Blue40   = Color(0xFF1B59E8)  // Primary (light)
val Md3Blue80   = Color(0xFFAEC6FF)  // Primary (dark)
val Md3Blue90   = Color(0xFFD9E2FF)
val Md3Blue95   = Color(0xFFEEF0FF)

// ── Material 3 Secondary Palette — Green ───────────────────────────────────────
val Md3Green10  = Color(0xFF002110)
val Md3Green20  = Color(0xFF003920)
val Md3Green30  = Color(0xFF005230)
val Md3Green40  = Color(0xFF1A6B46)  // Secondary (light)
val Md3Green80  = Color(0xFF72D8A4)  // Secondary (dark)
val Md3Green90  = Color(0xFFB2F0D0)

// ── Neutral Palette ────────────────────────────────────────────────────────────
val Neutral0    = Color(0xFF000000)
val Neutral10   = Color(0xFF1A1C1E)
val Neutral20   = Color(0xFF2F3033)
val Neutral30   = Color(0xFF46474A)
val Neutral40   = Color(0xFF5E5E62)
val Neutral80   = Color(0xFFC7C6CA)
val Neutral90   = Color(0xFFE4E2E6)
val Neutral95   = Color(0xFFF2F0F4)
val Neutral99   = Color(0xFFFCFBFF)
val Neutral100  = Color(0xFFFFFFFF)

// ── Named Accents (canonical) ──────────────────────────────────────────────────
val AccentBlue      = Md3Blue40
val AccentGreen     = Md3Green40
val AccentCyan      = Color(0xFF0288D1)   // Info/link blue (Material Blue 700)
val AccentViolet    = Color(0xFF7B2FBE)
val AccentIndigo    = Color(0xFF3D5AFE)
val AccentEmerald   = Md3Green40

// ── Legacy aliases — keep old names so un-touched files still compile ──────────
val NeonGreen       = Md3Green40
val NeonGreenDim    = Color(0xFF005230)
val NeonCyan        = AccentCyan
val NeonCyanDim     = Color(0xFF006494)

// ── Surface — Light ────────────────────────────────────────────────────────────
val LightBackground  = Neutral99          // #FCFBFF  off-white
val LightSurface     = Neutral100         // #FFFFFF
val LightCard        = Neutral95          // #F2F0F4
val LightElevated    = Neutral90          // #E4E2E6
val LightBorder      = Neutral80          // #C7C6CA
val LightTextPrimary = Neutral10          // #1A1C1E
val LightTextSecond  = Neutral30          // #46474A
val LightTextMuted   = Neutral40          // #5E5E62

// ── Surface — Dark ─────────────────────────────────────────────────────────────
val BackgroundBlack  = Color(0xFF1A1C1E)  // Neutral 10
val SurfaceDark      = Color(0xFF121417)  // slightly deeper
val SurfaceCard      = Color(0xFF2F3033)  // Neutral 20
val SurfaceElevated  = Color(0xFF46474A)  // Neutral 30
val SurfaceBorder    = Color(0xFF5E5E62)  // Neutral 40

// ── Dark text ──────────────────────────────────────────────────────────────────
val TextPrimary      = Neutral99          // on dark backgrounds
val TextSecondary    = Neutral80          // secondary on dark
val TextMuted        = Neutral40          // muted on dark

// ── Status ─────────────────────────────────────────────────────────────────────
val ErrorRed         = Color(0xFFBA1A1A)
val WarningAmber     = Color(0xFFE65100)
val SuccessGreen     = Md3Green40

// ── Gradient stops ─────────────────────────────────────────────────────────────
val GradientStart    = Md3Blue40
val GradientEnd      = AccentViolet

// ── Premium ────────────────────────────────────────────────────────────────────
val PremiumGold      = Color(0xFFF59E0B)
val PremiumGoldDim   = Color(0xFFB45309)
val PremiumGoldGlow  = Color(0x33F59E0B)
val PremiumViolet    = AccentViolet
val PremiumVioletDim = Color(0xFF6A0F9C)
val PremiumRose      = Color(0xFFE91E63)
