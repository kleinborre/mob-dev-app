package com.sample.calorease.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CalorEaseCard — Elevated glassy card component.
 * Uses Material3 Card with surfaceContainerHigh-tinted background and
 * increased rounding for a modern aesthetic.
 */
@Composable
fun CalorEaseCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    if (onClick != null) {
        Card(
            onClick   = onClick,
            modifier  = modifier.fillMaxWidth(),
            shape     = shape,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp,
                pressedElevation = 6.dp,
                hoveredElevation = 5.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content  = content
            )
        }
    } else {
        Card(
            modifier  = modifier.fillMaxWidth(),
            shape     = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content  = content
            )
        }
    }
}

