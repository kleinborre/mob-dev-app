package com.sample.calorease.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise

/**
 * Bottom Navigation Bar for Admin screens
 * Provides consistent navigation between admin pages
 */
@Composable
fun AdminBottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = DarkTurquoise
    ) {
        AdminNavItem.values().forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the admin stats screen to avoid building up a large stack
                            popUpTo(Screen.AdminStats.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkTurquoise,
                    selectedTextColor = DarkTurquoise,
                    indicatorColor = DarkTurquoise.copy(alpha = 0.1f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Admin navigation items
 */
enum class AdminNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    STATISTICS("Statistics", Icons.AutoMirrored.Filled.ShowChart, Screen.AdminStats.route),
    USERS("Manage Users", Icons.Default.People, Screen.AdminUsers.route),
    SETTINGS("Settings", Icons.Default.Settings, Screen.AdminSettings.route)
}
