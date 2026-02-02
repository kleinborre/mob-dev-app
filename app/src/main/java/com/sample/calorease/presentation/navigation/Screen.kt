package com.sample.calorease.presentation.navigation

sealed class Screen(val route: String) {
    data object GettingStarted : Screen("getting_started")
    data object Login : Screen("login")
    data object SignUp : Screen("sign_up")
    data object ForgotPassword : Screen("forgot_password")
    data object OnboardingName : Screen("onboarding_name")
    data object OnboardingStats : Screen("onboarding_stats")
    data object OnboardingGoals : Screen("onboarding_goals")
    data object OnboardingResults : Screen("onboarding_results")
    data object Dashboard : Screen("dashboard")
    data object Statistics : Screen("statistics")
    data object Settings : Screen("settings")
}
