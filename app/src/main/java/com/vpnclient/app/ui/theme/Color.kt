package com.vpnclient.app.ui.theme

import androidx.compose.ui.graphics.Color

// Light Canvas & Modern Surfaces (iOS Health / Fitness style matching reference image)
val BackgroundLight = Color(0xFFF3F4F8)
val SurfaceCardLight = Color(0xFFFFFFFF)
val SurfaceCardBorder = Color(0xFFE2E8F0)
val SurfaceCardPurple = Color(0xFFF3F0FF) // Soft Lavender card background
val BorderPurple = Color(0xFFDDD6FE)

// Dark Hero Card (Obsidian container matching the top hero card in image)
val HeroCardDark = Color(0xFF111319)
val HeroCardSurface = Color(0xFF1C1E28)
val HeroCardBorder = Color(0xFF2A2D3C)

// Brand & Accents
val PrimaryIndigo = Color(0xFF4F46E5)
val PrimaryBlue = Color(0xFF3B82F6)
val PrimaryBlueVariant = Color(0xFF2563EB)

// Gradient & Health Ring Colors (matching image)
val RingCoral = Color(0xFFFF5E3A)   // Coral / Orange ring in health score
val RingAmber = Color(0xFFFFAA00)   // Amber / Gold
val RingCyan = Color(0xFF06B6D4)    // Electric Cyan
val RingPurple = Color(0xFF8B5CF6)  // Vibrant Lavender
val RingPurpleDeep = Color(0xFF6D28D9)

val AccentGreen = Color(0xFF10B981)
val AccentGreenGlow = Color(0x3310B981)

val AccentRed = Color(0xFFEF4444)
val AccentRedGlow = Color(0x33EF4444)

val AccentAmber = Color(0xFFF59E0B)

// Typography on Light Canvas
val TextDarkPrimary = Color(0xFF111827)
val TextDarkSecondary = Color(0xFF4B5563)
val TextDarkMuted = Color(0xFF9CA3AF)

// Typography on Hero / Dark Cards
val TextLightPrimary = Color(0xFFF8FAFC)
val TextLightSecondary = Color(0xFF94A3B8)
val TextLightMuted = Color(0xFF64748B)

// Aliases for unified styling across existing components
val BackgroundDark = BackgroundLight
val SurfaceDark = SurfaceCardLight
val SurfaceLightDark = SurfaceCardPurple
val TextPrimary = TextDarkPrimary
val TextSecondary = TextDarkSecondary
val TextMuted = TextDarkMuted
val CardBorder = SurfaceCardBorder
