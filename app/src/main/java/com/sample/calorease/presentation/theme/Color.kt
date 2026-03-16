package com.sample.calorease.presentation.theme

import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────────────────────────────────
// Brand Primary — Teal #006874 (lighter, clearly visible — used for buttons, accents, FAB)
// ──────────────────────────────────────────────────────────────────────────
val DeepTeal        = Color(0xFF006874)   // Primary — all buttons & accents (lightened from #00303D)
val DeepTealLight   = Color(0xFF009BAA)   // Gradient top (hover / lighter shimmer)
val DeepTealDark    = Color(0xFF004E5B)   // Gradient bottom (pressed / deeper)
val DeepTealGlass   = Color(0x22006874)   // 15% alpha glass tint

// Alias so all existing DarkTurquoise references continue to compile
val DarkTurquoise   = DeepTeal

// ──────────────────────────────────────────────────────────────────────────
// Accent shades for hero card gradient
// ──────────────────────────────────────────────────────────────────────────
val TealAccent      = Color(0xFF00505F)
val TealHighlight   = Color(0xFF006070)
val CoralRed        = Color(0xFFE57373)   // Over-calorie hero card
val MintGreen       = Color(0xFF43A047)

// ──────────────────────────────────────────────────────────────────────────
// Aesthetic White / Gray palette (backgrounds + surfaces)
// ──────────────────────────────────────────────────────────────────────────
val AestheticWhite  = Color(0xFFF8F9FA)   // App background — warm white
val PaperWhite      = Color(0xFFFCFCFD)   // Card surface — slightly cooler
val GlassWhite      = Color(0xE6FFFFFF)   // 90% white glass card fill
val GlassWhiteLight = Color(0xB3FFFFFF)   // 70% white lighter glass layer
val OffWhite        = Color(0xFFF0F2F5)   // Subtle section divider
val SilverGray      = Color(0xFFCDD5DE)   // Borders, outlines
val MediumGray      = Color(0xFF8A95A0)   // Secondary / hint text
val SubtleGray      = Color(0xFFDDE3EA)   // Very light card background

// ──────────────────────────────────────────────────────────────────────────
// Text colors — aesthetic dark, not pure black
// ──────────────────────────────────────────────────────────────────────────
val TextPrimary    = Color(0xFF1A2332)   // Deep navy-charcoal for headings
val TextSecondary  = Color(0xFF4A5568)   // Slate gray for body text
val TextHint       = Color(0xFF8A95A0)   // Placeholder / hint
val TextOnDark     = Color(0xFFF0F4F8)   // Text rendered on dark backgrounds

// Aliases for backward-compatible legacy code
val CharcoalGray   = TextPrimary
val White          = Color(0xFFFFFFFF)
val Black          = Color(0xFF000000)
val SoftGray       = OffWhite

// ──────────────────────────────────────────────────────────────────────────
// Semantic Status Colors
// ──────────────────────────────────────────────────────────────────────────
val SuccessGreen   = Color(0xFF2E7D32)
val ErrorRed       = Color(0xFFD32F2F)
val WarningAmber   = Color(0xFFEF6C00)
val InfoBlue       = Color(0xFF1565C0)

// ──────────────────────────────────────────────────────────────────────────
// Dark mode surface colors
// ──────────────────────────────────────────────────────────────────────────
val DarkBackground       = Color(0xFF0F1923)
val DarkSurface          = Color(0xFF1A2535)
val DarkSurfaceVariant   = Color(0xFF243040)
val OnDarkSurface        = Color(0xFFE8EDF2)
val DarkGlassSurface     = Color(0xCC1A2535)

// ──────────────────────────────────────────────────────────────────────────
// Light Color Scheme (Material 3 semantic roles)
// ──────────────────────────────────────────────────────────────────────────
val LightPrimary            = DeepTeal
val LightOnPrimary          = Color(0xFFFFFFFF)
val LightPrimaryContainer   = Color(0xFFCFE9EE)
val LightOnPrimaryContainer = DeepTealDark
val LightSecondary          = TealAccent
val LightOnSecondary        = Color(0xFFFFFFFF)
val LightBackground         = AestheticWhite
val LightSurface            = PaperWhite
val LightOnSurface          = TextPrimary
val LightSurfaceVariant     = OffWhite
val LightOnSurfaceVariant   = TextSecondary
val LightOutline            = SilverGray
val LightError              = ErrorRed
val LightOnError            = Color(0xFFFFFFFF)

// ──────────────────────────────────────────────────────────────────────────
// Dark Color Scheme (Material 3 semantic roles)
// ──────────────────────────────────────────────────────────────────────────
val DarkPrimary             = Color(0xFF4DCCE0)
val DarkOnPrimary           = Color(0xFF001F28)
val DarkPrimaryContainer    = DeepTeal
val DarkOnPrimaryContainer  = Color(0xFFB2EBF2)
val DarkSecondary           = Color(0xFF4DCCE0)
val DarkOnSecondary         = Color(0xFF001F28)
val DarkSurfaceColor        = DarkSurface
val DarkOnSurfaceColor      = OnDarkSurface
val DarkSurfaceVariantColor = DarkSurfaceVariant
val DarkOnSurfaceVariant    = MediumGray
val DarkOutline             = Color(0xFF455A64)
val DarkError               = Color(0xFFEF9A9A)
val DarkOnError             = Color(0xFF690005)

// ──────────────────────────────────────────────────────────────────────────
// Gender-selection (onboarding)
// ──────────────────────────────────────────────────────────────────────────
val FemaleRed = Color(0xFFE91E63)
val MaleTeal  = DeepTeal
val SoftAmber = WarningAmber
val LightBlue = InfoBlue

// Glass surface overlays (used by GradientBackground + CalorEaseCard)
val GlassSurfaceLight = GlassWhite
val GlassSurfaceDark  = DarkGlassSurface
val CardSurface       = Color(0xFFFFFFFF) // Pure white — always visible on any background
val TurquoiseGlass    = DeepTealGlass
val TurquoiseDark     = DeepTealDark
val TurquoiseLight    = DeepTealLight
