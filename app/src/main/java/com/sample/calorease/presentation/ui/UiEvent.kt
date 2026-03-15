package com.sample.calorease.presentation.ui

/**
 * UiEvent — Sealed class representing one-shot UI feedback events.
 * Emitted by ViewModels via a SharedFlow and consumed by Composable screens
 * to show Snackbars, play sounds, or trigger other non-persistent side-effects.
 *
 * Usage in ViewModel:
 *   private val _uiEvent = MutableSharedFlow<UiEvent>()
 *   val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()
 *
 * Usage in Screen:
 *   LaunchedEffect(Unit) {
 *       viewModel.uiEvent.collect { event -> ... }
 *   }
 */
sealed class UiEvent {
    /** Show a green success Snackbar with optional action label. */
    data class ShowSuccess(
        val message: String,
        val actionLabel: String? = null
    ) : UiEvent()

    /** Show a red error Snackbar with optional action label. */
    data class ShowError(
        val message: String,
        val actionLabel: String? = null
    ) : UiEvent()

    /** Show a neutral info Snackbar. */
    data class ShowInfo(
        val message: String
    ) : UiEvent()

    /** Navigate to a route, optionally clearing the back stack. */
    data class Navigate(
        val route: String,
        val clearBackStack: Boolean = false
    ) : UiEvent()

    /** Dismiss the current screen (popBackStack). */
    object NavigateBack : UiEvent()
}
