package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.Gender
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseOutlinedButton
import com.sample.calorease.presentation.components.CalorEaseProgressBar
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingStatsScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.onboardingState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Progress Bar
        CalorEaseProgressBar(
            progress = 0.5f,
            stepText = "Step 2 of 4"
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Tell us about yourself",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Gender Selection
        Text(
            text = "Gender",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderButton(
                text = "Male",
                isSelected = state.gender == Gender.MALE,
                onClick = { viewModel.updateGender(Gender.MALE) },
                modifier = Modifier.weight(1f)
            )
            
            GenderButton(
                text = "Female",
                isSelected = state.gender == Gender.FEMALE,
                onClick = { viewModel.updateGender(Gender.FEMALE) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Height
        CalorEaseTextField(
            value = state.height,
            onValueChange = viewModel::updateHeight,
            label = "Height (cm)",
            placeholder = "e.g., 170",
            keyboardType = KeyboardType.Number,
            isError = state.heightError != null,
            errorMessage = state.heightError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weight
        CalorEaseTextField(
            value = state.weight,
            onValueChange = viewModel::updateWeight,
            label = "Current Weight (kg)",
            placeholder = "e.g., 70",
            keyboardType = KeyboardType.Number,
            isError = state.weightError != null,
            errorMessage = state.weightError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Age
        CalorEaseTextField(
            value = state.age,
            onValueChange = viewModel::updateAge,
            label = "Age",
            placeholder = "e.g., 25",
            keyboardType = KeyboardType.Number,
            isError = state.ageError != null,
            errorMessage = state.ageError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Activity Level
        Text(
            text = "Activity Level",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ActivityLevelSelector(
            selectedLevel = state.activityLevel,
            onLevelSelected = viewModel::updateActivityLevel
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
                    if (viewModel.validateStats()) {
                        navController.navigate(Screen.OnboardingGoals.route)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun GenderButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DarkTurquoise else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = text,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActivityLevelSelector(
    selectedLevel: ActivityLevel,
    onLevelSelected: (ActivityLevel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActivityLevel.entries.forEach { level ->
            val levelText = when (level) {
                ActivityLevel.SEDENTARY -> "Sedentary (little/no exercise)"
                ActivityLevel.LIGHTLY_ACTIVE -> "Lightly Active (1-3 days/week)"
                ActivityLevel.MODERATELY_ACTIVE -> "Moderately Active (3-5 days/week)"
                ActivityLevel.VERY_ACTIVE -> "Very Active (6-7 days/week)"
                ActivityLevel.EXTRA_ACTIVE -> "Extra Active (physical job + training)"
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedLevel == level),
                        onClick = { onLevelSelected(level) }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedLevel == level),
                    onClick = { onLevelSelected(level) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = DarkTurquoise
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = levelText,
                    fontFamily = Poppins,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
