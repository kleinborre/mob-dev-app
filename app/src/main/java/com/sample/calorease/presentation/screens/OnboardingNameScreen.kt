package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseOutlinedButton
import com.sample.calorease.presentation.components.CalorEaseProgressBar
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingNameScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.onboardingState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    
    // ✅ Load saved progress when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadProgress()
    }
    
    // Get SessionManager and CoroutineScope
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember {
        com.sample.calorease.data.session.SessionManager(context)
    }
    val coroutineScope = rememberCoroutineScope()
    
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
            text = "What's your name?",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "We'll use this to personalize your experience",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // First Name Field
        CalorEaseTextField(
            value = state.firstName,
            onValueChange = viewModel::updateFirstName,
            label = "First Name",
            placeholder = "e.g., John",
            isError = state.firstNameError != null,
            errorMessage = state.firstNameError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Last Name Field
        CalorEaseTextField(
            value = state.lastName,
            onValueChange = viewModel::updateLastName,
            label = "Last Name",
            placeholder = "e.g., Doe",
            isError = state.lastNameError != null,
            errorMessage = state.lastNameError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Nickname Field (Optional)
        CalorEaseTextField(
            value = state.nickname,
            onValueChange = viewModel::updateNickname,
            label = "Nickname (Optional)",
            placeholder = "e.g., Johnny",
            isError = false,
            errorMessage = ""
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalorEaseOutlinedButton(
                text = "Back",
                onClick = { showExitDialog = true },  // Show dialog instead of direct navigation
                modifier = Modifier.weight(1f)
            )
            
            CalorEaseButton(
                text = "Next",
                onClick = {
                    if (viewModel.validateName()) {
                        viewModel.saveStepOne()  // ✅ PHASE 2: Save before navigate
                        navController.navigate(Screen.OnboardingStats.route)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    // Exit Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Exit Onboarding?",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "All unsaved information will be lost. Are you sure you want to exit?",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // ✅ PHASE G FIX: Clear session and navigate to Login with cleared backstack
                        // This prevents Login's back button from looping to Onboarding
                        coroutineScope.launch {
                            sessionManager.clearSession()
                            showExitDialog = false
                            navController.navigate("login") {
                                popUpTo("getting_started") { inclusive = false }  // Keep Getting Started in stack
                            }
                        }
                    }
                ) {
                    Text("Yes, Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
