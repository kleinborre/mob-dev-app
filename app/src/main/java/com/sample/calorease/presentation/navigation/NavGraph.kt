package com.sample.calorease.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sample.calorease.presentation.screens.AdminSettingsScreen
import com.sample.calorease.presentation.screens.AdminStatsScreen
import com.sample.calorease.presentation.screens.AdminUsersScreen
import com.sample.calorease.presentation.screens.DashboardScreen
import com.sample.calorease.presentation.screens.FoodLogsScreen
import com.sample.calorease.presentation.screens.ForgotPasswordScreen
import com.sample.calorease.presentation.screens.GettingStartedScreen
import com.sample.calorease.presentation.screens.LoginScreen
import com.sample.calorease.presentation.screens.OnboardingGoalsScreen
import com.sample.calorease.presentation.screens.OnboardingNameScreen
import com.sample.calorease.presentation.screens.OnboardingResultsScreen
import com.sample.calorease.presentation.screens.OnboardingStatsScreen
import com.sample.calorease.presentation.screens.SettingsScreen
import com.sample.calorease.presentation.screens.SignUpScreen
import com.sample.calorease.presentation.screens.StatsScreen

// ─────────────────────────────────────────────────────
// Transition helpers — 300ms slide + fade
// Typed as AnimatedContentTransitionScope<NavBackStackEntry>
// to match navigation‑compose 2.8.x API
// ─────────────────────────────────────────────────────
private const val T = 300

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterSlide(): EnterTransition =
    slideInHorizontally(tween(T)) { it } + fadeIn(tween(T))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitSlide(): ExitTransition =
    slideOutHorizontally(tween(T)) { -it / 3 } + fadeOut(tween(T))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterSlide(): EnterTransition =
    slideInHorizontally(tween(T)) { -it / 3 } + fadeIn(tween(T))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitSlide(): ExitTransition =
    slideOutHorizontally(tween(T)) { it } + fadeOut(tween(T))

private fun crossEnter(): EnterTransition = fadeIn(tween(T))
private fun crossExit(): ExitTransition  = fadeOut(tween(T))

// ─────────────────────────────────────────────────────
// NavGraph
// ─────────────────────────────────────────────────────
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController      = navController,
        startDestination   = startDestination,
        enterTransition    = { enterSlide() },
        exitTransition     = { exitSlide() },
        popEnterTransition = { popEnterSlide() },
        popExitTransition  = { popExitSlide() }
    ) {
        // ── Auth Flow ─────────────────────────────────────────────
        composable(Screen.GettingStarted.route) {
            GettingStartedScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        // ── Onboarding Flow ──────────────────────────────────────
        composable(Screen.OnboardingName.route) {
            OnboardingNameScreen(navController = navController)
        }
        composable(Screen.OnboardingStats.route) {
            OnboardingStatsScreen(navController = navController)
        }
        composable(Screen.OnboardingGoals.route) {
            OnboardingGoalsScreen(navController = navController)
        }
        composable(Screen.OnboardingResults.route) {
            OnboardingResultsScreen(navController = navController)
        }

        // ── Main App — Bottom Nav (crossfade) ────────────────────
        composable(
            route = Screen.Dashboard.route,
            enterTransition    = { crossEnter() },
            exitTransition     = { crossExit() },
            popEnterTransition = { crossEnter() },
            popExitTransition  = { crossExit() }
        ) { DashboardScreen(navController = navController) }

        composable(
            route = Screen.Statistics.route,
            enterTransition    = { crossEnter() },
            exitTransition     = { crossExit() },
            popEnterTransition = { crossEnter() },
            popExitTransition  = { crossExit() }
        ) { StatsScreen(navController = navController) }

        composable(
            route = Screen.Settings.route,
            enterTransition    = { crossEnter() },
            exitTransition     = { crossExit() },
            popEnterTransition = { crossEnter() },
            popExitTransition  = { crossExit() }
        ) { SettingsScreen(navController = navController) }

        // ── Admin Routes (crossfade within admin tabs) ────────────
        composable(
            route = Screen.AdminUsers.route,
            enterTransition    = { crossEnter() },
            exitTransition     = { crossExit() },
            popEnterTransition = { crossEnter() },
            popExitTransition  = { crossExit() }
        ) { AdminUsersScreen(navController = navController) }

        composable(
            route = Screen.AdminStats.route,
            enterTransition    = { crossEnter() },
            exitTransition     = { crossExit() },
            popEnterTransition = { crossEnter() },
            popExitTransition  = { crossExit() }
        ) { AdminStatsScreen(navController = navController) }

        composable(
            route = Screen.AdminSettings.route,
            enterTransition    = { crossEnter() },
            exitTransition     = { crossExit() },
            popEnterTransition = { crossEnter() },
            popExitTransition  = { crossExit() }
        ) { AdminSettingsScreen(navController = navController) }

        // ── Phase 4 / Sprint 3.1 ──────────────────────────────────
        composable(
            route = Screen.UpdateCredentials.route,
            enterTransition    = { crossEnter() },
            exitTransition     = { crossExit() },
            popEnterTransition = { crossEnter() },
            popExitTransition  = { crossExit() }
        ) { com.sample.calorease.presentation.screens.UpdateCredentialsScreen(navController = navController) }

        // ── Food Logs ─────────────────────────────────────────────
        composable("food_logs") {
            FoodLogsScreen(navController = navController)
        }
    }
}
