package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.components.Render
import com.sample.calorease.presentation.components.rememberStatusDialog
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.DeepTeal
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.UpdateCredentialsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCredentialsScreen(
    navController: NavController,
    viewModel: UpdateCredentialsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val dialog = rememberStatusDialog()

    LaunchedEffect(state.isUpdating) {
        if (state.isUpdating) {
            dialog.showLoading("Updating credentials...")
        }
    }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            dialog.showSuccess("Credentials updated successfully!")
            kotlinx.coroutines.delay(1800L)
            dialog.dismiss()
            viewModel.resetState()
            navController.popBackStack()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            dialog.showError(it)
            kotlinx.coroutines.delay(2000L)
            dialog.dismiss()
            viewModel.resetState()
        }
    }

    dialog.Render()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Update Credentials",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = DarkTurquoise
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkTurquoise
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // -- EMAIL FIELD --
            CalorEaseTextField(
                value = state.email,
                onValueChange = viewModel::updateEmail,
                label = "Email Address",
                placeholder = "Enter your current email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                isError = state.emailError != null,
                errorMessage = state.emailError ?: ""
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Change Password",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Text(
                "Enter your current password to unlock these fields.",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = Poppins,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // -- CURRENT PASSWORD --
            CalorEaseTextField(
                value = state.currentPasswordInput,
                onValueChange = { 
                    viewModel.updateCurrentPassword(it)
                    // Auto-unlock the below fields if current password is provided
                    viewModel.unlockPasswordFields(it.isNotBlank())
                },
                label = "Current Password",
                placeholder = "Enter current password to edit",
                isPassword = true,
                isError = state.currentPasswordError != null,
                errorMessage = state.currentPasswordError ?: ""
            )

            Spacer(modifier = Modifier.height(16.dp))

            // -- NEW PASSWORD --
            CalorEaseTextField(
                value = state.newPasswordInput,
                onValueChange = viewModel::updateNewPassword,
                label = "New Password",
                placeholder = if (!state.isPasswordFieldsUnlocked) "Locked" else "Enter new password",
                isPassword = true,
                enabled = state.isPasswordFieldsUnlocked,
                isError = state.newPasswordError != null,
                errorMessage = state.newPasswordError ?: ""
            )

            Spacer(modifier = Modifier.height(16.dp))

            // -- RE-ENTER PASSWORD --
            CalorEaseTextField(
                value = state.confirmPasswordInput,
                onValueChange = viewModel::updateConfirmPassword,
                label = "Re-enter New Password",
                placeholder = if (!state.isPasswordFieldsUnlocked) "Locked" else "Confirm new password",
                isPassword = true,
                enabled = state.isPasswordFieldsUnlocked,
                isError = state.confirmPasswordError != null,
                errorMessage = state.confirmPasswordError ?: ""
            )

            Spacer(modifier = Modifier.height(32.dp))

            CalorEaseButton(
                text = "Update",
                onClick = {
                    // Check local validation before surfacing dialog
                    val hasEmailError = state.emailError != null
                    val hasPassError = state.isPasswordFieldsUnlocked && 
                        (state.newPasswordError != null || state.confirmPasswordError != null || 
                         state.newPasswordInput.isBlank() || state.confirmPasswordInput.isBlank())
                         
                    if (hasEmailError || hasPassError) return@CalorEaseButton
                    
                    showConfirmDialog = true 
                },
                isLoading = state.isUpdating
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Update Credentials?",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to update your credentials? These changes will take effect immediately.",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.updateCredentials()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                ) {
                    Text("Confirm", fontFamily = Poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", fontFamily = Poppins, color = DeepTeal)
                }
            }
        )
    }
}
