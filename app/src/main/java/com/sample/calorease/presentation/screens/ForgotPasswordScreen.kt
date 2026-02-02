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
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.AuthScaffold
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.util.ValidationUtils

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
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
                emailError = null
            },
            label = "Email",
            placeholder = "Enter your email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            errorMessage = emailError ?: ""
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Reset Password Button
        CalorEaseButton(
            text = "Reset Password",
            onClick = {
                if (ValidationUtils.isValidEmail(email)) {
                    showSuccessDialog = true
                } else {
                    emailError = "Invalid email address"
                }
            }
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
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    text = "Email Sent",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "A password reset link has been sent to $email. Please check your inbox.",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text(
                        text = "OK",
                        fontFamily = Poppins,
                        color = DarkTurquoise
                    )
                }
            }
        )
    }
}
