package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sample.calorease.presentation.components.BottomNavigationBar
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.DashboardViewModel
import com.sample.calorease.util.DateUtils

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    var showCalorieDialog by remember { mutableStateOf(false) }
    var calorieInput by remember { mutableStateOf("") }
    
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
                
                // Header
                Text(
                    text = "Welcome, ${state.nickname}",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = DateUtils.formatReadable(DateUtils.getTodayString()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Calorie Summary Card
                CalorEaseCard {
                    Text(
                        text = "Today's Calories",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CalorieStat("Target", "${state.goalCalories.toInt()}")
                        CalorieStat("Eaten", "${state.consumedCalories.toInt()}")
                        CalorieStat(
                            "Remaining",
                            "${state.remainingCalories.toInt()}",
                            highlight = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = DarkTurquoise,
                        trackColor = DarkTurquoise.copy(alpha = 0.2f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            calorieInput = state.consumedCalories.toInt().toString()
                            showCalorieDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkTurquoise
                        )
                    ) {
                        Text(
                            text = "Log Calories",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Daily Tasks Card
                CalorEaseCard {
                    Text(
                        text = "Daily Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DailyTask("Drink 8 glasses of water")
                    DailyTask("Eat ${state.goalCalories.toInt()} calories")
                    DailyTask("Log today's weight")
                }
            }
        }
    }
    
    // Calorie Input Dialog
    if (showCalorieDialog) {
        AlertDialog(
            onDismissRequest = { showCalorieDialog = false },
            title = {
                Text(
                    text = "Log Calories",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter total calories consumed today:",
                        fontFamily = Poppins
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CalorEaseTextField(
                        value = calorieInput,
                        onValueChange = { calorieInput = it },
                        label = "Calories",
                        placeholder = "e.g., 1500",
                        keyboardType = KeyboardType.Number
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calories = calorieInput.toDoubleOrNull() ?: 0.0
                        viewModel.updateCalories(calories)
                        showCalorieDialog = false
                    }
                ) {
                    Text("Save", fontFamily = Poppins, color = DarkTurquoise)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalorieDialog = false }) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
}

@Composable
fun CalorieStat(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = if (highlight) DarkTurquoise else MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = Poppins
        )
    }
}

@Composable
fun DailyTask(task: String) {
    var checked by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = CheckboxDefaults.colors(
                checkedColor = DarkTurquoise
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = task,
            fontFamily = Poppins,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
