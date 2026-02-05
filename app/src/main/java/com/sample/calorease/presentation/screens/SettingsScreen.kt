package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color  // PHASE 3: For red delete buttons
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.domain.model.WeightGoal
import com.sample.calorease.presentation.components.BottomNavigationBar
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.settingsState.collectAsState()
    var showSwitchToAdminConfirm by remember { mutableStateOf(false) }  // PHASE 5: Switch confirmation
    
    // Navigate to getting started on account deletion
    LaunchedEffect(state.shouldNavigateToStart) {
        if (state.shouldNavigateToStart) {
            navController.navigate(Screen.GettingStarted.route) {
                popUpTo(0) { inclusive = true }
            }
            viewModel.resetNavigationFlag()
        }
    }
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkTurquoise)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))  // FIX-2e: Reduced from 16dp
                
                Text(
                    text = "Account Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Comprehensive User Stats Card with BMI
                CalorEaseCard {
                    Text(
                        text = "Your Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = DarkTurquoise
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Calculate BMI (real-time)
                    val bmi = state.userStats?.let { stats ->
                        val heightInMeters = stats.heightCm / 100.0
                        stats.weightKg / (heightInMeters * heightInMeters)
                    } ?: 0.0
                    
                    val bmiStatus = when {
                        bmi < 18.5 -> "Underweight"
                        bmi < 25.0 -> "Normal"
                        bmi < 30.0 -> "Overweight"
                        else -> "Obese"
                    }
                    
                    val bmiColor = when {
                        bmi < 18.5 -> MaterialTheme.colorScheme.primary
                        bmi < 25.0 -> DarkTurquoise
                        bmi < 30.0 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                    
                    // Gender
                    SettingRow(
                        "Gender",
                        when(state.userStats?.gender) {
                            com.sample.calorease.domain.model.Gender.MALE -> "Male"
                            com.sample.calorease.domain.model.Gender.FEMALE -> "Female"
                            else -> "Not set"
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Age
                    SettingRow(
                        "Age",
                        "${state.userStats?.age ?: 0} years"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Height
                    SettingRow(
                        "Height",
                        "${state.userStats?.heightCm ?: 0.0} cm"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Weight (updates in real-time)
                    SettingRow(
                        "Weight",
                        String.format("%.1f kg", state.userStats?.weightKg ?: 0.0)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // BMI (calculates in real-time)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "BMI",
                            fontFamily = Poppins,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = String.format("%.1f (%s)", bmi, bmiStatus),
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = bmiColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Activity Level
                    SettingRow(
                        "Activity Level",
                        when(state.userStats?.activityLevel) {
                            com.sample.calorease.domain.model.ActivityLevel.SEDENTARY -> "Sedentary"
                            com.sample.calorease.domain.model.ActivityLevel.LIGHTLY_ACTIVE -> "Lightly Active"
                            com.sample.calorease.domain.model.ActivityLevel.MODERATELY_ACTIVE -> "Moderately Active"
                            com.sample.calorease.domain.model.ActivityLevel.VERY_ACTIVE -> "Very Active"
                            com.sample.calorease.domain.model.ActivityLevel.EXTRA_ACTIVE -> "Extra Active"
                            else -> "Not set"
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Goal
                    SettingRow(
                        "Goal",
                        getGoalText(state.userStats?.weightGoal)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Daily Calorie Target (updates when weight/goal changes)
                    SettingRow(
                        "Daily Calorie Target",
                        "${state.userStats?.goalCalories?.toInt() ?: 0} cal"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ✅ Phase 4: Admin Mode Button (only for admin users)
                if (state.adminAccess) {
                    CalorEaseCard {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Admin Access",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface  // PART 2: Black
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "You have administrator privileges",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = Poppins
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            CalorEaseButton(
                                text = "Switch to Admin Mode",
                                onClick = { showSwitchToAdminConfirm = true }  // PHASE 5: Show confirmation
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // ✅ Phase G: Account Actions Card (Edit Weight, Change Goal, Sign Out)
                CalorEaseCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Account Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface  // PHASE 5: Black
                        )
                        
                        CalorEaseButton(
                            text = "Edit Weight",
                            onClick = viewModel::showEditWeightDialog
                        )
                        
                        CalorEaseButton(
                            text = "Change Goal",
                            onClick = viewModel::showChangeGoalDialog
                        )
                        
                        CalorEaseButton(
                            text = "Sign Out",
                            onClick = viewModel::showLogoutConfirmDialog,
                            backgroundColor = MaterialTheme.colorScheme.onSurface  // PHASE 5: Black button
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ✅ Phase G: Danger Zone Card (separate)
                CalorEaseCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Danger Zone",  // PHASE 5: Removed ⚠️ icon
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface  // PART 2: Black instead of error
                        )
                        
                        Text(
                            text = "Deleting your account is permanent and cannot be undone. All your data will be lost.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = Poppins,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        CalorEaseButton(
                            text = "Delete Account",
                            onClick = viewModel::showDeleteConfirmDialog,
                            backgroundColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
    
    // PHASE 5: Switch to Admin Mode Confirmation Dialog
    if (showSwitchToAdminConfirm) {
        AlertDialog(
            onDismissRequest = { showSwitchToAdminConfirm = false },
            title = {
                Text(
                    text = "Switch to Admin Mode?",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You will be redirected to the admin dashboard. You can return to user mode anytime from Admin Settings.",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSwitchToAdminConfirm = false
                        viewModel.saveAndSwitchToAdmin()
                        navController.navigate(Screen.AdminStats.route)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkTurquoise
                    )
                ) {
                    Text("Switch", fontFamily = Poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchToAdminConfirm = false }) {
                    Text("Cancel", fontFamily = Poppins, color = DarkTurquoise)
                }
            }
        )
    }
    
    // Edit Weight Dialog
    if (state.showEditWeightDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideEditWeightDialog,
            title = {
                Text(
                    text = "Update Weight",  // PART 3: Removed ⚠️
                    style = MaterialTheme.typography.bodyMedium,  // PART 3: Body size (bold)
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Changing your weight will recalculate your daily calorie target and reset your progress.",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CalorEaseTextField(
                        value = state.newWeight,
                        onValueChange = viewModel::updateNewWeight,
                        label = "New Weight (kg)",
                        placeholder = "e.g., 70",
                        keyboardType = KeyboardType.Number
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::requestWeightChange) {  // ✅ Show confirmation first
                    Text("Continue", fontFamily = Poppins, color = DarkTurquoise)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideEditWeightDialog) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
    
    // ✅ Edit Weight Confirmation Dialog
    if (state.showWeightConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideWeightConfirmDialog,
            title = {
                Text(
                    text = "⚠️ Confirm Weight Change",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to update your weight?",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "This will:",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Recalculate your BMR and TDEE",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Update your daily calorie target",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::saveNewWeight,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = DarkTurquoise
                    )
                ) {
                    Text("Yes, Update Weight", fontFamily = Poppins, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideWeightConfirmDialog) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
    
    // Change Goal Dialog
    if (state.showChangeGoalDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideChangeGoalDialog,
            title = {
                Text(
                    text = "Change Goal",  // PART 3: Removed ⚠️
                    style = MaterialTheme.typography.bodyMedium,  // PART 3: Body size (bold)
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Changing your goal will recalculate your daily calorie target.",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeightGoal.entries.forEach { goal ->
                            val goalText = getGoalText(goal)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (state.newWeightGoal == goal),
                                        onClick = { viewModel.updateNewWeightGoal(goal) }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (state.newWeightGoal == goal),
                                    onClick = { viewModel.updateNewWeightGoal(goal) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = DarkTurquoise
                                    )
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = goalText,
                                    fontFamily = Poppins,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::requestGoalChange) {  // ✅ CHANGED: Show second confirmation
                    Text("Continue", fontFamily = Poppins, color = DarkTurquoise)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideChangeGoalDialog) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (state.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteConfirmDialog,
            title = {
                Text(
                    text = "Delete Account",  // PART 3: Removed ⚠️
                    style = MaterialTheme.typography.bodyMedium,  // PART 3: Body size (bold)
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface  // PART 3: Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your account? This will permanently delete all your data and cannot be undone.",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::deleteAccount) {
                    Text(
                        "Delete",
                        fontFamily = Poppins,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteConfirmDialog) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
    
    // ✅ SECOND CONFIRMATION: Goal Change Final Warning
    if (state.showGoalConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideGoalConfirmDialog,
            title = {
                Text(
                    text = "⚠️ Confirm Goal Change",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column {
                    Text(
                        text = "⚠️ This action CANNOT be undone!",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Changing your goal will:",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Delete ALL your food log history",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Reset your progress stats to zero",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Recalculate your daily calorie target",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Are you absolutely sure you want to continue?",
                        fontFamily = Poppins,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::saveNewGoal,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Delete Progress", fontFamily = Poppins, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideGoalConfirmDialog) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
    
    // Logout Confirmation Dialog
    if (state.showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideLogoutConfirmDialog,
            title = {
                Text(
                    text = "Sign Out",  // Already correct
                    style = MaterialTheme.typography.bodyMedium,  // PART 3: Body size (bold)
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out?",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::logout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface  // PART 3: Black button
                    )
                ) {
                    Text(
                        "Yes",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideLogoutConfirmDialog) {
                    Text("No", fontFamily = Poppins)
                }
            }
        )
    }
    
    // PHASE 3: First Delete Account Confirmation
    if (state.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteConfirmDialog,
            title = {
                Text(
                    text = "Delete Account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your account?",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmFirstDelete,  // PHASE 3: Show second dialog
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC3545)
                    )
                ) {
                    Text(
                        "Delete",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteConfirmDialog) {
                    Text(
                        "Cancel",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
    
    // PHASE 3: Second Delete Confirmation (Final Warning)
    if (state.showDeleteFinalWarningDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideFinalWarningDialog,
            title = {
                Text(
                    text = "Final Warning",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This action cannot be undone. Your account will be permanently deactivated and you will lose access to all your data.",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::deleteAccount,  // PHASE 3: Final deletion
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC3545)
                    )
                ) {
                    Text(
                        "Delete Account",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideFinalWarningDialog) {
                    Text(
                        "Go Back",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun getGoalText(goal: WeightGoal?): String {
    return when (goal) {
        WeightGoal.LOSE_1_KG -> "Lose 1kg/week"
        WeightGoal.LOSE_0_5_KG -> "Lose 0.5kg/week"
        WeightGoal.LOSE_0_25_KG -> "Lose 0.25kg/week"
        WeightGoal.MAINTAIN -> "Maintain"
        WeightGoal.GAIN_0_25_KG -> "Gain 0.25kg/week"
        WeightGoal.GAIN_0_5_KG -> "Gain 0.5kg/week"
        WeightGoal.GAIN_1_KG -> "Gain 1kg/week"
        null -> "Not set"
    }
}
