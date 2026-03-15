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
    /** Show a loading modal with a context-specific message. */
    data class ShowLoading(
        val message: String
    ) : UiEvent()

    /** Show a green success modal with auto-dismiss. */
    data class ShowSuccess(
        val message: String,
        val actionLabel: String? = null
    ) : UiEvent()

    /** Show a red error modal with auto-dismiss. */
    data class ShowError(
        val message: String,
        val actionLabel: String? = null
    ) : UiEvent()

    /** Dismiss any currently-showing modal. */
    object DismissDialog : UiEvent()

    /** Navigate to a route, optionally clearing the back stack. */
    data class Navigate(
        val route: String,
        val clearBackStack: Boolean = false
    ) : UiEvent()

    /** Dismiss the current screen (popBackStack). */
    object NavigateBack : UiEvent()
}
