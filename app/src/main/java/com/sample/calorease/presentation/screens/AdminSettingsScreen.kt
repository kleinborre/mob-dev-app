package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.AdminBottomNavigationBar
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins

/**
 * Admin Settings Screen
 * Contains: User Mode button, Sign Out button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    navController: NavController
) {
   // PHASE 1: Confirmation dialog states
    var showSwitchConfirm by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var shouldSwitchToUser by remember { mutableStateOf(false) }  // PHASE 3: Navigation trigger
    var shouldSignOut by remember { mutableStateOf(false) }  // PART 3: Sign Out trigger
    
   // PHASE 3: Save preference when switch is confirmed
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(shouldSwitchToUser) {
        if (shouldSwitchToUser) {
            val sessionManager = com.sample.calorease.data.session.SessionManager(context)
            sessionManager.saveLastDashboardMode("user")
        }
    }
    
    // PART 3: Clear session when signing out
    androidx.compose.runtime.LaunchedEffect(shouldSignOut) {
        if (shouldSignOut) {
            val sessionManager = com.sample.calorease.data.session.SessionManager(context)
            sessionManager.clearSession()
        }
    }
    
    Scaffold(
        bottomBar = { AdminBottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // FIX-1c: Single header with minimal spacing  
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Admin Settings",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = DarkTurquoise
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Mode Button
            CalorEaseCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "User Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface  // PHASE 4: Black
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Switch to your user account to use the app as a regular user",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Poppins
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CalorEaseButton(
                        text = "Switch to User Mode",
                        onClick = { showSwitchConfirm = true }  // PHASE 1: Show confirmation
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sign Out Button
            CalorEaseCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface  // PHASE 4: Black instead of error
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Log out from your admin account",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Poppins
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CalorEaseButton(
                        text = "Sign Out",
                        onClick = { showSignOutConfirm = true },
                        backgroundColor = MaterialTheme.colorScheme.onSurface  // PHASE 4: Black button
                    )
                }
            }
        }
    }
    
    // PHASE 1: Switch to User Mode Confirmation
    if (showSwitchConfirm) {
        AlertDialog(
            onDismissRequest = { showSwitchConfirm = false },
            title = {
                Text(
                    text = "Switch to User Mode?",
                    style = MaterialTheme.typography.titleMedium,  // PHASE 4: Smaller header
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You will be redirected to the user dashboard. You can return to admin mode anytime from Settings.",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSwitchConfirm = false
                        shouldSwitchToUser = true  // PHASE 3: Trigger save via LaunchedEffect
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.AdminStats.route) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkTurquoise
                    )
                ) {
                    Text("Switch", fontFamily = Poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchConfirm = false }) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
    
    // PHASE 1: Sign Out Confirmation
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = {
                Text(
                    text = "Sign Out",  // PART 3: Removed ⚠️
                    style = MaterialTheme.typography.bodyMedium,  // PART 3: Body size (bold)
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out?",  // PART 3: New message
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        shouldSignOut = true  // PART 3: Trigger LaunchedEffect to clear session
                        navController.navigate(Screen.GettingStarted.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface  // PART 3: Black button
                    )
                ) {
                    Text("Sign Out", fontFamily = Poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
}
