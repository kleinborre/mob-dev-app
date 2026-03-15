package com.sample.calorease.presentation.theme

import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────
// Brand Colors
// ──────────────────────────────────────────
val DarkTurquoise  = Color(0xFF00CED1)   // Primary — corrected to spec #00CED1
val TurquoiseLight = Color(0xFF33D9DC)   // Lighter tint for gradient starts
val TurquoiseDark  = Color(0xFF009496)   // Deeper tint for gradient ends / pressed state
val TurquoiseGlass = Color(0x3300CED1)   // Semi-transparent for glassy overlays (20% alpha)

// ──────────────────────────────────────────
// Accent / Utility Colors
// ──────────────────────────────────────────
val CoralRed   = Color(0xFFFF6B6B)   // Error / destructive action hint
val MintGreen  = Color(0xFF3EB489)   // Success state
val SoftAmber  = Color(0xFFFFB347)   // Warning / caution
val LightBlue  = Color(0xFF87CEEB)   // Info

// ──────────────────────────────────────────
// Neutral / Surface Colors
// ──────────────────────────────────────────
val White         = Color(0xFFFFFFFF)
val Black         = Color(0xFF000000)
val SoftGray      = Color(0xFFF5F7FA)   // Light background
val CardSurface   = Color(0xFFFAFAFA)   // Slightly off-white card bg
val CharcoalGray  = Color(0xFF2C3E50)   // Dark text on light bg
val DividerColor  = Color(0xFFE0E0E0)   // Dividers & outlines

// Dark mode surfaces
val DarkBackground    = Color(0xFF0F1923)   // Deep dark bg
val DarkSurface       = Color(0xFF1A2535)   // Card surfaces in dark mode
val DarkSurfaceVariant = Color(0xFF243040)  // Elevated surfaces
val OnDarkSurface     = Color(0xFFE0F7F7)   // Text on dark surfaces

// Glassy surface overlays
val GlassSurfaceLight = Color(0xCCFFFFFF)   // 80% white — glassy card in light mode
val GlassSurfaceDark  = Color(0xCC1A2535)   // 80% dark surface — glassy card in dark mode

// Gender button colors (design-specified)
val FemaleRed  = Color(0xFFC62828)   // Dark red for female gender selection
val MaleTeal   = DarkTurquoise       // Teal for male gender selection

// ──────────────────────────────────────────
// Light Color Scheme Roles
// ──────────────────────────────────────────
val LightPrimary           = DarkTurquoise
val LightOnPrimary         = White
val LightPrimaryContainer  = Color(0xFFE0FAFA)
val LightOnPrimaryContainer = TurquoiseDark
val LightSecondary         = TurquoiseDark
val LightOnSecondary       = White
val LightBackground        = SoftGray
val LightSurface           = White
val LightOnSurface         = CharcoalGray
val LightSurfaceVariant    = Color(0xFFEEF6F6)
val LightOnSurfaceVariant  = Color(0xFF4A6572)
val LightOutline           = Color(0xFFB0BEC5)
val LightError             = CoralRed
val LightOnError           = White

// ──────────────────────────────────────────
// Dark Color Scheme Roles
// ──────────────────────────────────────────
val DarkPrimary            = TurquoiseLight
val DarkOnPrimary          = Color(0xFF003535)
val DarkPrimaryContainer   = TurquoiseDark
val DarkOnPrimaryContainer = Color(0xFFB2FFFF)
val DarkSecondary          = TurquoiseLight
val DarkOnSecondary        = Color(0xFF003535)
val DarkSurfaceColor       = DarkSurface
val DarkOnSurfaceColor     = OnDarkSurface
val DarkSurfaceVariantColor = DarkSurfaceVariant
val DarkOnSurfaceVariant   = Color(0xFF90A4AE)
val DarkOutline            = Color(0xFF455A64)
val DarkError              = Color(0xFFFF8A80)
val DarkOnError            = Color(0xFF690005)

