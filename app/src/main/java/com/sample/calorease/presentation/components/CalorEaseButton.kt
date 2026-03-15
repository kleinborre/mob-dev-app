package com.sample.calorease.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sample.calorease.presentation.theme.DeepTeal
import com.sample.calorease.presentation.theme.DeepTealDark
import com.sample.calorease.presentation.theme.DeepTealLight
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.theme.White

/**
 * CalorEaseButton — Industry-standard gradient branded button.
 *
 * WHY this approach (Box + Modifier.background):
 * ───────────────────────────────────────────────
 * The previous Button + Modifier.drawBehind approach showed an inner rectangle
 * because Modifier.clip(shape) does NOT clip drawBehind — it draws outside the
 * clip boundary in the parent layer.
 *
 * Modifier.background(brush, shape) draws AND clips to the shape in one pass,
 * producing a clean rounded gradient with zero artifacts.
 */
@Composable
fun CalorEaseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    shape: Shape = RoundedCornerShape(14.dp),
    gradientColors: List<Color> = listOf(DeepTealLight, DeepTeal, DeepTealDark)
) {
    val active = enabled && !isLoading
    val brush = if (active) {
        Brush.verticalGradient(gradientColors)
    } else {
        Brush.verticalGradient(gradientColors.map { it.copy(alpha = 0.40f) })
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(brush = brush, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication        = ripple(color = White.copy(alpha = 0.3f)),
                enabled           = active,
                role              = Role.Button,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(22.dp),
                color       = White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text       = text,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                style      = MaterialTheme.typography.bodyLarge,
                color      = White
            )
        }
    }
}
