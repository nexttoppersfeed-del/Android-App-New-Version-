package com.nexttoppers.feed.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary accents (refined from neon to professional) ────────────────────────
val AccentCyan    = Color(0xFF06B6D4)   // Tailwind cyan-500
val AccentEmerald = Color(0xFF10B981)   // Tailwind emerald-500
val AccentViolet  = Color(0xFF8B5CF6)   // Tailwind violet-500

// Legacy name aliases — unchanged names keep all existing code working
val NeonGreen     = AccentEmerald
val NeonGreenDim  = Color(0xFF0D9066)
val NeonCyan      = AccentCyan
val NeonCyanDim   = Color(0xFF0891B2)

// ── Surface layers (deep navy theme) ───────────────────────────────────────────
val BackgroundBlack = Color(0xFF070B14)
val SurfaceDark     = Color(0xFF0D1321)
val SurfaceCard     = Color(0xFF121A2E)
val SurfaceElevated = Color(0xFF1A2540)

// ── Text ───────────────────────────────────────────────────────────────────────
val TextPrimary     = Color(0xFFF1F5F9)
val TextSecondary   = Color(0xFF94A3B8)
val TextMuted       = Color(0xFF475569)

// ── Status ─────────────────────────────────────────────────────────────────────
val ErrorRed        = Color(0xFFEF4444)
val WarningAmber    = Color(0xFFF59E0B)
val SuccessGreen    = Color(0xFF22C55E)

// ── Gradient stops ─────────────────────────────────────────────────────────────
val GradientStart   = AccentCyan
val GradientEnd     = AccentViolet

// ── Premium ────────────────────────────────────────────────────────────────────
val PremiumGold     = Color(0xFFF59E0B)
val PremiumGoldDim  = Color(0xFFB45309)
val PremiumGoldGlow = Color(0x33F59E0B)
val PremiumViolet   = AccentViolet
val PremiumVioletDim= Color(0xFF7C3AED)
val PremiumRose     = Color(0xFFEC4899)

// ── Light mode surfaces ────────────────────────────────────────────────────────
val LightBackground  = Color(0xFFF8FAFC)
val LightSurface     = Color(0xFFFFFFFF)
val LightCard        = Color(0xFFF1F5F9)
val LightElevated    = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecond  = Color(0xFF475569)
val LightTextMuted   = Color(0xFF94A3B8)
val LightBorder      = Color(0xFFCBD5E1)
