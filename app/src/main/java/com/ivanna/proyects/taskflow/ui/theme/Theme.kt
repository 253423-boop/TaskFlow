package com.ivanna.proyects.taskflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.colorResource
import com.upchiapas.taskflow.R

@Composable
fun TaskFlowTheme(
    content: @Composable () -> Unit
) {
    val colors = TaskFlowColors(
        primaryPurple = colorResource(id = R.color.primary_purple),
        textPrimary = colorResource(id = R.color.text_primary),
        textSecondary = colorResource(id = R.color.text_secondary),
        filterInactiveText = colorResource(id = R.color.filter_inactive_text),
        backgroundWhite = colorResource(id = R.color.background_white),
        surfaceWhite = colorResource(id = R.color.surface_white),
        outlineLavender = colorResource(id = R.color.outline_lavender),
        surfaceLavender = colorResource(id = R.color.surface_lavender),
        errorCrimson = colorResource(id = R.color.error_crimson),
        errorInputBackground = colorResource(id = R.color.error_input_bg),
        errorBannerBackground = colorResource(id = R.color.error_banner_bg),
        errorBannerText = colorResource(id = R.color.error_banner_text),
        overdueCrimson = colorResource(id = R.color.overdue_crimson),
        successSurface = colorResource(id = R.color.success_surface),
        successText = colorResource(id = R.color.success_text),
        spinnerTrack = colorResource(id = R.color.spinner_track),
        dialogScrim = colorResource(id = R.color.dialog_scrim)
    )

    val colorScheme = lightColorScheme(
        primary = colors.primaryPurple,
        onPrimary = colors.backgroundWhite,
        primaryContainer = colors.surfaceLavender,
        onPrimaryContainer = colors.primaryPurple,
        secondary = colors.surfaceLavender,
        onSecondary = colors.primaryPurple,
        secondaryContainer = colors.surfaceLavender,
        onSecondaryContainer = colors.textPrimary,
        background = colors.backgroundWhite,
        onBackground = colors.textPrimary,
        surface = colors.surfaceWhite,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.surfaceLavender,
        onSurfaceVariant = colors.textSecondary,
        outline = colors.outlineLavender,
        outlineVariant = colors.outlineLavender,
        error = colors.errorCrimson,
        onError = colors.backgroundWhite,
        errorContainer = colors.errorBannerBackground,
        onErrorContainer = colors.errorBannerText
    )

    CompositionLocalProvider(LocalTaskFlowColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TaskFlowTypography,
            content = content
        )
    }
}
