package com.sample.calorease.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sample.calorease.presentation.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Flow
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
        
        // Onboarding Flow
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
        
        // Main App Flow
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        
        composable(Screen.Statistics.route) {
            StatsScreen(navController = navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        // ✅ Phase 4: Admin Routes
        composable(Screen.AdminUsers.route) {
            AdminUsersScreen(navController = navController)
        }
        
        composable(Screen.AdminStats.route) {
            AdminStatsScreen(navController = navController)
        }
        
        composable(Screen.AdminSettings.route) {
            AdminSettingsScreen(navController = navController)
        }
        
        // Food Logs/History - Full implementation
        composable("food_logs") {
            FoodLogsScreen(navController = navController)
        }
    }
}
