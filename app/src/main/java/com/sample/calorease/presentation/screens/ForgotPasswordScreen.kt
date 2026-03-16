package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.AuthScaffold
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.components.rememberStatusDialog
import com.sample.calorease.presentation.components.Render
import androidx.hilt.navigation.compose.hiltViewModel
import com.sample.calorease.presentation.viewmodel.AuthViewModel
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.util.NetworkUtils
import com.sample.calorease.presentation.util.SoundPlayer
import com.sample.calorease.util.ValidationUtils

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var userTriggeredRequest by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val statusDialog = rememberStatusDialog()
    val soundPlayer = remember { SoundPlayer(context) }
    
    // Listen to ViewModel authState for loading/success
    LaunchedEffect(authState.isLoading, authState.isLoginSuccess) {
        if (userTriggeredRequest) {
            if (authState.isLoading) {
                statusDialog.showLoading("Waiting for network...")
            } else if (authState.isLoginSuccess) {
                soundPlayer.playSuccess()
                statusDialog.showSuccess("Verification code has been sent to your email.")
                kotlinx.coroutines.delay(1800L)
                statusDialog.dismiss()
                viewModel.resetSuccessFlags()
                userTriggeredRequest = false
                navController.popBackStack()
            } else if (authState.emailError != null) {
                soundPlayer.playError()
                statusDialog.showError(authState.emailError ?: "Failed to send reset email")
                kotlinx.coroutines.delay(1800L)
                statusDialog.dismiss()
                userTriggeredRequest = false
            }
        }
    }
    
    statusDialog.Render()
    
    AuthScaffold(
        onBackClick = { navController.popBackStack() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // CalorEase Logo Text
        Text(
            text = "calorease",
            style = MaterialTheme.typography.displaySmall,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter your email to reset your password",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Email Field
        CalorEaseTextField(
            value = email,
            onValueChange = {
                email = it
                if (authState.emailError != null) viewModel.updateEmail(it) // Re-trigger update to clear error
            },
            label = "Email",
            placeholder = "Enter your email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            isError = authState.emailError != null,
            errorMessage = authState.emailError ?: ""
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Reset Password Button
        CalorEaseButton(
            text = "Reset Password",
            onClick = {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    statusDialog.showError("Network unavailable. Please connect to the internet to reset password.")
                    return@CalorEaseButton
                }
                
                // Block offline manual checks using UI Provider logic matching ViewModel
                if (email.isBlank()) {
                    viewModel.updateEmail("") // Trigger empty error
                    viewModel.login("", "dummy") // Hacky way to trip ValidationUtils from UI
                } else if (!ValidationUtils.isAcceptedEmailProvider(email)) {
                    // Update email triggers the provider check block downstream
                    viewModel.updateEmail(email)
                    viewModel.resetPassword(email)
                } else {
                    userTriggeredRequest = true
                    viewModel.resetPassword(email)
                }
            },
            isLoading = authState.isLoading && userTriggeredRequest
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Back to Login
        TextButton(onClick = { navController.popBackStack() }) {
            Text(
                text = "Back to Login",
                fontFamily = Poppins,
                color = DarkTurquoise
            )
        }
        }
    }
}
