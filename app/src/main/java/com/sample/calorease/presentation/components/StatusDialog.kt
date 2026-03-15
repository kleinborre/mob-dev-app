package com.sample.calorease.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import kotlinx.coroutines.delay

// ── Status types ──────────────────────────────────────────────────────────────
enum class StatusType { LOADING, SUCCESS, ERROR }

data class StatusDialogState(
    val visible: Boolean   = false,
    val type: StatusType   = StatusType.LOADING,
    val message: String    = ""
)

// ── Main StatusDialog composable ──────────────────────────────────────────────
/**
 * Unified status dialog for Loading, Success, and Error states.
 *
 * Behavior:
 *  - LOADING: spinning indicator + message, stays until dismissed externally
 *  - SUCCESS: animated check icon + message, auto-dismisses after [successAutoDismissMs]
 *  - ERROR:   themed X icon + message, auto-dismisses after [errorAutoDismissMs]
 *
 * Designed to be driven by a single [StatusDialogState] from the ViewModel or Screen.
 * No buttons — all states auto-dismiss or are dismissed by the caller on action complete.
 *
 * No emojis. Uses app theme colors throughout.
 */
@Composable
fun StatusDialog(
    state: StatusDialogState,
    onDismiss: () -> Unit,
    successAutoDismissMs: Long = 1800L,
    errorAutoDismissMs: Long   = 2500L
) {
    if (!state.visible) return

    // Auto-dismiss for SUCCESS and ERROR states
    LaunchedEffect(state.visible, state.type) {
        when (state.type) {
            StatusType.SUCCESS -> {
                delay(successAutoDismissMs)
                onDismiss()
            }
            StatusType.ERROR -> {
                delay(errorAutoDismissMs)
                onDismiss()
            }
            StatusType.LOADING -> Unit  // stays open until external dismissal
        }
    }

    Dialog(
        onDismissRequest = { /* non-dismissable by back-press or outside tap */ },
        properties = DialogProperties(
            dismissOnBackPress      = false,
            dismissOnClickOutside   = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .wrapContentHeight(),
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (state.type) {
                    StatusType.LOADING -> LoadingIndicator()
                    StatusType.SUCCESS -> SuccessIndicator()
                    StatusType.ERROR   -> ErrorIndicator()
                }

                Text(
                    text      = state.message,
                    fontFamily   = Poppins,
                    fontWeight   = FontWeight.Medium,
                    fontSize     = 14.sp,
                    textAlign    = TextAlign.Center,
                    color        = MaterialTheme.colorScheme.onSurface,
                    lineHeight   = 20.sp
                )
            }
        }
    }
}

// ── Individual state indicators ───────────────────────────────────────────────

@Composable
private fun LoadingIndicator() {
    // Pulsing ring animation around the CircularProgressIndicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier  = Modifier.size(52.dp),
            color     = DarkTurquoise,
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun SuccessIndicator() {
    // Pop-in scale animation for the check circle
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "successScale"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(DarkTurquoise),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Default.Check,
            contentDescription = "Success",
            tint               = Color.White,
            modifier           = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun ErrorIndicator() {
    // Shake animation on appear
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "errorScale"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Default.Close,
            contentDescription = "Error",
            tint               = MaterialTheme.colorScheme.onErrorContainer,
            modifier           = Modifier.size(36.dp)
        )
    }
}

// ── Convenience extension ─────────────────────────────────────────────────────
/**
 * Helper class to manage StatusDialog state inside a Composable.
 * Use [rememberStatusDialog] to create and [StatusDialogController.dialog] to render.
 */
class StatusDialogController {
    var state by mutableStateOf(StatusDialogState())
        private set

    fun showLoading(message: String) {
        state = StatusDialogState(visible = true, type = StatusType.LOADING, message = message)
    }

    fun showSuccess(message: String) {
        state = StatusDialogState(visible = true, type = StatusType.SUCCESS, message = message)
    }

    fun showError(message: String) {
        state = StatusDialogState(visible = true, type = StatusType.ERROR, message = message)
    }

    fun dismiss() {
        state = state.copy(visible = false)
    }
}

@Composable
fun rememberStatusDialog(): StatusDialogController {
    return remember { StatusDialogController() }
}

/**
 * Renders the dialog. Call at the top level of your composable:
 *   val dialog = rememberStatusDialog()
 *   dialog.Render()
 *   // Then: dialog.showLoading("Signing in...") / dialog.showSuccess("Done") etc.
 */
@Composable
fun StatusDialogController.Render(
    successAutoDismissMs: Long = 1800L,
    errorAutoDismissMs: Long   = 2500L
) {
    StatusDialog(
        state = this.state,
        onDismiss = { this.dismiss() },
        successAutoDismissMs = successAutoDismissMs,
        errorAutoDismissMs   = errorAutoDismissMs
    )
}
