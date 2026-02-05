package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.domain.model.WeightGoal
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseOutlinedButton
import com.sample.calorease.presentation.components.CalorEaseProgressBar
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingGoalsScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.onboardingState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // ✅ FIX STEP 3: Load saved state when screen opens (including back navigation)
    LaunchedEffect(Unit) {
        android.util.Log.d("OnboardingGoals", "🔄 Loading saved progress...")
        viewModel.loadProgress()
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
            progress = 0.75f,
            stepText = "Step 3 of 4"
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Set Your Goals",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Target Weight Section
        Text(
            text = "Target Weight",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        CalorEaseTextField(
            value = state.targetWeight,
            onValueChange = viewModel::updateTargetWeight,
            label = "Target Weight (kg)",
            placeholder = "e.g., 65",
            keyboardType = KeyboardType.Number,
            isError = state.targetWeightError != null,
            errorMessage = state.targetWeightError ?: ""
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Weight Goal Selection
        Text(
            text = "Weekly Goal",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        WeightGoalSelector(
            selectedGoal = state.weightGoal,
            onGoalSelected = viewModel::updateWeightGoal
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalorEaseOutlinedButton(
                text = "Back",
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            )
            
            CalorEaseButton(
                text = "Next",
                onClick = {
                    if (viewModel.validateGoals()) {
                        // ✅ PHASE J FIX: Await saveStepThree before navigation
                        coroutineScope.launch {
                            viewModel.saveStepThree()  // Suspends until save completes
                            viewModel.calculateResults()
                            delay(100)  // Small delay for calculations
                            navController.navigate(Screen.OnboardingResults.route)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun WeightGoalSelector(
    selectedGoal: WeightGoal,
    onGoalSelected: (WeightGoal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(4.dp)  // Reduced from 8dp
    ) {
        WeightGoal.entries.forEach { goal ->
            val goalText = when (goal) {
                WeightGoal.LOSE_1_KG -> "Lose 1kg per week"
                WeightGoal.LOSE_0_5_KG -> "Lose 0.5kg per week"
                WeightGoal.LOSE_0_25_KG -> "Lose 0.25kg per week"
                WeightGoal.MAINTAIN -> "Maintain current weight"
                WeightGoal.GAIN_0_25_KG -> "Gain 0.25kg per week"
                WeightGoal.GAIN_0_5_KG -> "Gain 0.5kg per week"
                WeightGoal.GAIN_1_KG -> "Gain 1kg per week"
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedGoal == goal),
                        onClick = { onGoalSelected(goal) }
                    )
                    .padding(vertical = 4.dp),  // Reduced from 8dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedGoal == goal),
                    onClick = { onGoalSelected(goal) },
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
