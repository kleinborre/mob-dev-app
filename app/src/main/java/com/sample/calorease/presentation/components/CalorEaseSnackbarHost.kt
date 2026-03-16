package com.sample.calorease.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.CoralRed
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.theme.White

/**
 * CalorEaseSnackbarHost — Custom themed SnackbarHost.
 * Shows "success:" prefixed messages in DarkTurquoise, others in CoralRed.
 * Both styles use rounded corners and Poppins font.
 *
 * Convention: prefix your message with "success:" for green Snackbar.
 *   e.g., emit UiEvent.ShowSuccess("success:Food entry saved!")
 *
 * Non-prefixed messages render as error (red).
 */
@Composable
fun CalorEaseSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { snackbarData ->
        CalorEaseSnackbar(snackbarData = snackbarData)
    }
}

@Composable
fun CalorEaseSnackbar(snackbarData: SnackbarData) {
    val message = snackbarData.visuals.message
    val isSuccess = message.startsWith("success:", ignoreCase = true)
    val displayMessage = if (isSuccess) message.removePrefix("success:").trim() else message

    val containerColor = if (isSuccess) DarkTurquoise else CoralRed

    Snackbar(
        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        shape      = RoundedCornerShape(12.dp),
        containerColor = containerColor,
        contentColor   = White,
        action = snackbarData.visuals.actionLabel?.let {
            {
                TextButton(onClick = { snackbarData.performAction() }) {
                    Text(
                        text       = it,
                        color      = White,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) {
        Text(
            text       = displayMessage,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            style      = MaterialTheme.typography.bodyMedium
        )
    }
}
