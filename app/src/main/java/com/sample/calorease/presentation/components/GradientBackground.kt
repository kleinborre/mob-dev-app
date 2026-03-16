package com.sample.calorease.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.sample.calorease.presentation.theme.AestheticWhite
import com.sample.calorease.presentation.theme.OffWhite
import com.sample.calorease.presentation.theme.PaperWhite
import com.sample.calorease.presentation.theme.SubtleGray

/**
 * GradientBackground — Wraps content with the app's signature aesthetic
 * warm-white → off-white gradient. Apply this as the root container for
 * every screen to ensure a consistent, modern look throughout the app.
 *
 * Usage:
 *   GradientBackground {
 *       // your screen content
 *   }
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AestheticWhite,  // Top   — warm white
                        PaperWhite,      // Mid   — paper white
                        OffWhite,        // Lower — very subtle cool tint
                        SubtleGray       // Base  — lightest possible gray
                    ),
                    startY = 0f,
                    endY   = Float.POSITIVE_INFINITY
                )
            ),
        content = content
    )
}
