package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.components.CalorEaseProgressBar
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingResultsScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.onboardingState.collectAsState()
    
    // Navigate on save success
    LaunchedEffect(state.isSaveSuccess) {
        if (state.isSaveSuccess) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
            viewModel.resetSuccessFlag()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Progress Bar
        CalorEaseProgressBar(
            progress = 1f,
            stepText = "Step 4 of 4"
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Your Health Profile",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Card
        CalorEaseCard {
            ResultRow(label = "BMI", value = "%.1f (%s)".format(state.bmiValue, state.bmiStatus))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ResultRow(label = "Ideal Weight", value = "%.1f kg".format(state.idealWeight))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ResultRow(label = "BMR", value = "%.0f cal/day".format(state.bmr))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ResultRow(label = "TDEE", value = "%.0f cal/day".format(state.tdee))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ResultRow(
                label = "Daily Calorie Goal",
                value = "%.0f cal/day".format(state.goalCalories),
                highlight = true
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Based on your goals, you should consume approximately ${state.goalCalories.toInt()} calories per day.",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Poppins,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Get Started Button
        CalorEaseButton(
            text = "Get Started",
            onClick = { viewModel.saveUserStats() },
            isLoading = state.isLoading
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ResultRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = if (highlight) DarkTurquoise else MaterialTheme.colorScheme.onSurface
        )
    }
}
