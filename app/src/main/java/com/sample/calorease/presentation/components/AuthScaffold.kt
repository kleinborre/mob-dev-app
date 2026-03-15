package com.sample.calorease.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * AuthScaffold — Reusable scaffold for authentication and onboarding screens.
 *
 * Now wraps the entire screen in [GradientBackground] and sets
 * [Scaffold.containerColor] to transparent so the gradient bleeds through.
 * This gives every auth/onboarding screen the aesthetic white gradient background
 * without any per-screen change.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScaffold(
    title: String = "",
    onBackClick: (() -> Unit)? = null,
    showBackButton: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,   // let GradientBackground show through
            snackbarHost   = snackbarHost,
            topBar = {
                TopAppBar(
                    title = {
                        if (title.isNotEmpty()) {
                            Text(text = title)
                        }
                    },
                    navigationIcon = {
                        if (showBackButton && onBackClick != null) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = bottomBar,
            content   = content
        )
    }
}
