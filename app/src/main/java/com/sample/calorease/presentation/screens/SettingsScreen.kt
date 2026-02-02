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
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Account Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Current Stats
                CalorEaseCard {
                    Text(
                        text = "Current Stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SettingRow(
                        "Weight",
                        "${state.userStats?.weightKg ?: 0.0} kg"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingRow(
                        "Goal",
                        getGoalText(state.userStats?.weightGoal)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingRow(
                        "Daily Calorie Target",
                        "${state.userStats?.goalCalories?.toInt() ?: 0} cal"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Edit Weight Button
                CalorEaseButton(
                    text = "Edit Weight",
                    onClick = viewModel::showEditWeightDialog
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Change Goal Button
                CalorEaseButton(
                    text = "Change Goal",
                    onClick = viewModel::showChangeGoalDialog
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Sign Out Button
                CalorEaseButton(
                    text = "Sign Out",
                    onClick = viewModel::showLogoutConfirmDialog
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Danger Zone
                Text(
                    text = "Danger Zone",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Delete Account Button
                Button(
                    onClick = viewModel::showDeleteConfirmDialog,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Delete Account",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    
    // Edit Weight Dialog
    if (state.showEditWeightDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideEditWeightDialog,
            title = {
                Text(
                    text = "⚠️ Update Weight",
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
                TextButton(onClick = viewModel::saveNewWeight) {
                    Text("Confirm", fontFamily = Poppins, color = DarkTurquoise)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideEditWeightDialog) {
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
                    text = "⚠️ Change Goal",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).height(400.dp)) {
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
                TextButton(onClick = viewModel::saveNewGoal) {
                    Text("Confirm", fontFamily = Poppins, color = DarkTurquoise)
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
                    text = "⚠️ Delete Account",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
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
    // Logout Confirmation Dialog
    if (state.showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideLogoutConfirmDialog,
            title = {
                Text(
                    text = "Sign Out",
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
                TextButton(onClick = viewModel::logout) {
                    Text(
                        "Yes",
                        fontFamily = Poppins,
                        color = DarkTurquoise,
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
