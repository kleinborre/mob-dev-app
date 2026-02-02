package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseProgressBar
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingNameScreen(
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
            progress = 0.25f,
            stepText = "Step 1 of 4"
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "What should we call you?",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter a nickname we can use",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Nickname Field
        CalorEaseTextField(
            value = state.nickname,
            onValueChange = viewModel::updateNickname,
            label = "Nickname",
            placeholder = "e.g., John",
            isError = state.nicknameError != null,
            errorMessage = state.nicknameError ?: ""
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Next Button
        CalorEaseButton(
            text = "Next",
            onClick = {
                if (viewModel.validateName()) {
                    navController.navigate(Screen.OnboardingStats.route)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
