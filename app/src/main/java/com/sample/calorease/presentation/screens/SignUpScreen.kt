package com.sample.calorease.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.R
import com.sample.calorease.presentation.components.AuthScaffold
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseOutlinedButton
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.AuthViewModel
import com.sample.calorease.presentation.viewmodel.AuthState

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState: AuthState by viewModel.authState.collectAsState()
    
    // Navigate on success
    LaunchedEffect(authState.isSignUpSuccess) {
        if (authState.isSignUpSuccess) {
            navController.navigate(Screen.OnboardingName.route) {
                popUpTo(Screen.GettingStarted.route) { inclusive = true }
            }
            viewModel.resetSuccessFlags()
        }
    }
    
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
            text = "Create Account",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sign up to get started",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Name Field
        CalorEaseTextField(
            value = authState.name,
            onValueChange = viewModel::updateName,
            label = "Full Name",
            placeholder = "Enter your name",
            leadingIcon = Icons.Default.Person,
            isError = authState.nameError != null,
            errorMessage = authState.nameError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Email Field
        CalorEaseTextField(
            value = authState.email,
            onValueChange = viewModel::updateEmail,
            label = "Email",
            placeholder = "Enter your email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            isError = authState.emailError != null,
            errorMessage = authState.emailError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password Field
        CalorEaseTextField(
            value = authState.password,
            onValueChange = viewModel::updatePassword,
            label = "Password",
            placeholder = "Enter your password",
            isPassword = true,
            isError = authState.passwordError != null,
            errorMessage = authState.passwordError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confirm Password Field
        CalorEaseTextField(
            value = authState.confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            label = "Confirm Password",
            placeholder = "Re-enter your password",
            isPassword = true,
            isError = authState.confirmPasswordError != null,
            errorMessage = authState.confirmPasswordError ?: ""
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Sign Up Button
        CalorEaseButton(
            text = "Sign Up",
            onClick = viewModel::signUp,
            isLoading = authState.isLoading
        )
        

        
        Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
