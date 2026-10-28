package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Ustaca tasarım dili: "Minimal White & Emerald" (MüşteriCep ile aynı aile).
 * Ekranlar renkleri doğrudan hex ile değil, bu palet üzerinden okur; açık/koyu moda göre değişir.
 */
data class AppPalette(
    val isDark: Boolean,
    val ink: Color,
    val onInk: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
    val success: Color,
    val successSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val neutral: Color,
    val neutralSoft: Color,
    val text: Color,
    val muted: Color,
    val inactive: Color,
    val cardBorder: Color
)

/** Açık mod: beyaz kartlar, açık gri zemin, petrol/zümrüt yeşili vurgu. */
val DefterPalette = AppPalette(
    isDark = false,
    ink = Color(0xFF0F766E),
    onInk = Color(0xFFFFFFFF),
    accent = Color(0xFF10B981),
    accentSoft = Color(0xFFECFDF5),
    onAccentSoft = Color(0xFF0F766E),
    success = Color(0xFF16A34A),
    successSoft = Color(0xFFF0FDF4),
    danger = Color(0xFFDC2626),
    dangerSoft = Color(0xFFFEF2F2),
    warning = Color(0xFFD97706),
    warningSoft = Color(0xFFFFFBEB),
    info = Color(0xFF2563EB),
    infoSoft = Color(0xFFEFF6FF),
    neutral = Color(0xFF475569),
    neutralSoft = Color(0xFFF1F4F8),
    text = Color(0xFF172033),
    muted = Color(0xFF64748B),
    inactive = Color(0xFF94A3B8),
    cardBorder = Color(0xFFE5EAF0)
)

/** Koyu mod: aynı dil, gece için. */
val KomutaPalette = AppPalette(
    isDark = true,
    ink = Color(0xFF34D399),
    onInk = Color(0xFF052E2B),
    accent = Color(0xFF10B981),
    accentSoft = Color(0xFF0F2E2A),
    onAccentSoft = Color(0xFFA7F3D0),
    success = Color(0xFF4ADE80),
    successSoft = Color(0xFF12301F),
    danger = Color(0xFFF87171),
    dangerSoft = Color(0xFF3A1A1C),
    warning = Color(0xFFFBBF24),
    warningSoft = Color(0xFF3A2C10),
    info = Color(0xFF60A5FA),
    infoSoft = Color(0xFF172A45),
    neutral = Color(0xFFCBD5E1),
    neutralSoft = Color(0xFF1E2833),
    text = Color(0xFFE6EBF2),
    muted = Color(0xFF94A3B8),
    inactive = Color(0xFF64748B),
    cardBorder = Color(0xFF263241)
)

val LocalAppPalette = staticCompositionLocalOf { DefterPalette }

/** WhatsApp marka yeşili (tema bağımsız). */
val WhatsAppGreen = Color(0xFF25D366)

// Ekranların kullandığı eski isimler korunuyor; artık aktif temaya (açık/koyu) göre değişiyorlar.
val ForestGreenPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.ink

val ForestGreenContainer: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.accentSoft

val ForestGreenOnContainer: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.onAccentSoft

val StatusSuccess: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.success

val StatusSuccessContainer: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.successSoft

val StatusWarning: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.warning

val StatusWarningContainer: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.warningSoft

val StatusError: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.danger

val StatusErrorContainer: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.dangerSoft

val StatusInfo: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.info

val StatusInfoContainer: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.infoSoft

val StatusNeutral: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.neutral

val StatusNeutralContainer: Color
    @Composable @ReadOnlyComposable get() = LocalAppPalette.current.neutralSoft
