package com.sample.calorease.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sample.calorease.presentation.theme.CardSurface
import com.sample.calorease.presentation.theme.DeepTeal

/**
 * CalorEaseCard — Clean elevated card component used app-wide.
 *
 * Design:
 *  - Pure white surface (#FFFFFF) — always visible on any background
 *  - 4dp elevation for material shadow
 *  - 1dp DeepTeal border at 12% alpha for brand identity
 *  - 16dp rounded corners
 *
 * [innerPadding]: Default 16.dp gives standard Material content inset.
 * Pass 0.dp only for full-bleed or custom-padded content.
 */
@Composable
fun CalorEaseCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 4.dp,
    innerPadding: Dp = 0.dp,   // callers manage their own padding for backward compat
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape     = shape,
        colors    = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(
            defaultElevation  = elevation,
            pressedElevation  = (elevation.value * 0.6f).dp,
            disabledElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = DeepTeal.copy(alpha = 0.12f)
        )
    ) {
        if (innerPadding > 0.dp) {
            Box(modifier = Modifier.padding(innerPadding)) {
                content()
            }
        } else {
            content()
        }
    }
}
