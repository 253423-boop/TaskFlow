package com.ivanna.proyects.taskflow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val PrimaryPurple = Color(0xFF6438B4)
val TextPrimary = Color(0xFF25164C)
val TextSecondary = Color(0xFF706783)
val FilterInactiveText = Color(0xFF291A47)
val BackgroundWhite = Color(0xFFFFFFFF)
val SurfaceWhite = Color(0xFFFFFFFF)
val OutlineLavender = Color(0xFFDDD4F0)
val SurfaceLavender = Color(0xFFF5F0FF)
val ErrorCrimson = Color(0xFFC53A4D)
val ErrorInputBackground = Color(0xFFFFF3F4)
val ErrorBannerBackground = Color(0xFFFFF0F0)
val ErrorBannerText = Color(0xFFB63444)
val OverdueCrimson = Color(0xFFBF3345)
val SuccessSurface = Color(0xFFE8F7EE)
val SuccessText = Color(0xFF217A4A)
val SpinnerTrack = Color(0xFFE5D8F8)
val DialogScrim = Color(0xFF140D2B)
val TransparentColor = Color.Transparent

@Immutable
data class TaskFlowColors(
    val primaryPurple: Color = PrimaryPurple,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val filterInactiveText: Color = FilterInactiveText,
    val backgroundWhite: Color = BackgroundWhite,
    val surfaceWhite: Color = SurfaceWhite,
    val outlineLavender: Color = OutlineLavender,
    val surfaceLavender: Color = SurfaceLavender,
    val errorCrimson: Color = ErrorCrimson,
    val errorInputBackground: Color = ErrorInputBackground,
    val errorBannerBackground: Color = ErrorBannerBackground,
    val errorBannerText: Color = ErrorBannerText,
    val overdueCrimson: Color = OverdueCrimson,
    val successSurface: Color = SuccessSurface,
    val successText: Color = SuccessText,
    val spinnerTrack: Color = SpinnerTrack,
    val dialogScrim: Color = DialogScrim
)

val LocalTaskFlowColors = staticCompositionLocalOf { TaskFlowColors() }
