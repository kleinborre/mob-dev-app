package com.sample.calorease.presentation.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.sample.calorease.R
import com.sample.calorease.presentation.components.AuthScaffold
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseOutlinedButton
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.components.Render
import com.sample.calorease.presentation.components.StatusType
import com.sample.calorease.presentation.components.rememberStatusDialog
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.util.NetworkUtils
import com.sample.calorease.presentation.util.SoundPlayer
import com.sample.calorease.presentation.viewmodel.AuthState
import com.sample.calorease.presentation.viewmodel.AuthViewModel

// Web Client ID from google-services.json (oauth_client client_type=3)
private const val SIGNUP_WEB_CLIENT_ID = "371598324066-6njd7ji52kinoic77admrcslc704ogq3.apps.googleusercontent.com"

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState: AuthState by viewModel.authState.collectAsState()
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dialog         = rememberStatusDialog()
    val soundPlayer    = remember { SoundPlayer(context) }

    // ── Account created via email ──────────────────────────────────────────────
    LaunchedEffect(authState.isSignUpSuccess) {
        if (authState.isSignUpSuccess) {
            soundPlayer.playSuccess()
            dialog.showSuccess("Please check your email to verify your account.")
            kotlinx.coroutines.delay(1800L)
            dialog.dismiss()
            
            navController.popBackStack() // Send back to Login screen to log in after verifying
            viewModel.resetSuccessFlags()
        }
    }

    // ── Google sign-in result (new user → onboarding, existing → dashboard) ────
    LaunchedEffect(authState.isLoginSuccess, authState.navigateToDashboard, authState.navigateToOnboarding) {
        if (!authState.isLoginSuccess) return@LaunchedEffect
        when {
            authState.navigateToDashboard -> {
                soundPlayer.playSuccess()
                dialog.showSuccess("Signed in successfully")
                kotlinx.coroutines.delay(1800L)
                dialog.dismiss()
                navController.navigate(Screen.Dashboard.route) { popUpTo(0) { inclusive = true } }
                viewModel.resetSuccessFlags()
            }
            authState.navigateToOnboarding -> {
                soundPlayer.playSuccess()
                dialog.showSuccess("Account ready")
                kotlinx.coroutines.delay(1800L)
                dialog.dismiss()
                navController.navigate(Screen.OnboardingName.route) { popUpTo(0) { inclusive = true } }
                viewModel.resetSuccessFlags()
            }
        }
    }

    // AuthViewModel-level UiEvents (e.g. Offline Network blocks)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is com.sample.calorease.presentation.ui.UiEvent.ShowError -> {
                    soundPlayer.playError()
                    dialog.showError(event.message)
                }
                is com.sample.calorease.presentation.ui.UiEvent.ShowSuccess -> {
                    soundPlayer.playSuccess()
                    dialog.showSuccess(event.message)
                }
                else -> Unit
            }
        }
    }

    // ── AuthViewModel Google errors ────────────────────────────────────────────
    LaunchedEffect(authState.googleSignInError) {
        authState.googleSignInError?.let {
            soundPlayer.playError()
            dialog.showError(it)
            viewModel.clearGoogleSignInError()
        }
    }

    // ── Registration loading ───────────────────────────────────────────────────
    LaunchedEffect(authState.isLoading) {
        if (authState.isLoading && !authState.isLoginSuccess) dialog.showLoading("Creating account...")
        else if (dialog.state.type == StatusType.LOADING) dialog.dismiss()
    }

    // ── Google two-pass OAuth ──────────────────────────────────────────────────
    fun launchGoogleOAuth() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            dialog.showError("Network unavailable. Please connect to the internet.")
            return
        }
        coroutineScope.launch {
            dialog.showLoading("Waiting for accounts...")
            try {
                suspend fun credential(filterAuthorized: Boolean): String {
                    val option = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(filterAuthorized)
                        .setServerClientId(SIGNUP_WEB_CLIENT_ID)
                        .build()
                    val result = CredentialManager.create(context).getCredential(
                        context,
                        GetCredentialRequest.Builder().addCredentialOption(option).build()
                    )
                    return GoogleIdTokenCredential.createFrom(result.credential.data).idToken
                }

                val idToken = try {
                    credential(true)              // fast path — already authorised
                } catch (e: NoCredentialException) {
                    credential(false)             // slow path — full picker
                }

                viewModel.googleSignIn(idToken)

            } catch (e: GetCredentialCancellationException) {
                dialog.dismiss()
                Log.d("SignUpScreen", "Google Sign-In cancelled")
            } catch (e: NoCredentialException) {
                soundPlayer.playError()
                dialog.showError("No Google accounts found on this device.")
            } catch (e: Exception) {
                Log.e("SignUpScreen", "OAuth error", e)
                soundPlayer.playError()
                dialog.showError("Google Sign-In unavailable. Check your connection.")
            }
        }
    }

    // Render status dialog above everything
    dialog.Render()

    AuthScaffold(
        onBackClick = { navController.popBackStack() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text       = "calorease",
                style      = MaterialTheme.typography.displaySmall,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color      = DarkTurquoise
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text       = "Create Account",
                style      = MaterialTheme.typography.titleLarge,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color      = DarkTurquoise
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = "Sign up to get started",
                style      = MaterialTheme.typography.bodyLarge,
                fontFamily = Poppins
            )

            Spacer(modifier = Modifier.height(24.dp))

            CalorEaseTextField(
                value         = authState.email,
                onValueChange = viewModel::updateSignUpEmail,
                label         = "Email",
                placeholder   = "Enter your email",
                leadingIcon   = Icons.Default.Email,
                keyboardType  = KeyboardType.Email,
                isError       = authState.emailError != null,
                errorMessage  = authState.emailError ?: ""
            )

            Spacer(modifier = Modifier.height(16.dp))

            CalorEaseTextField(
                value         = authState.password,
                onValueChange = viewModel::updatePassword,
                label         = "Password",
                placeholder   = "Enter your password",
                isPassword    = true,
                isError       = authState.passwordError != null,
                errorMessage  = authState.passwordError ?: ""
            )

            Spacer(modifier = Modifier.height(16.dp))

            CalorEaseTextField(
                value         = authState.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                label         = "Confirm Password",
                placeholder   = "Re-enter your password",
                isPassword    = true,
                isError       = authState.confirmPasswordError != null,
                errorMessage  = authState.confirmPasswordError ?: ""
            )

            Spacer(modifier = Modifier.height(32.dp))

            CalorEaseButton(
                text      = "Create Account",
                onClick   = {
                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        dialog.showError("Network unavailable. Please connect to the internet to sign up.")
                        return@CalorEaseButton
                    }
                    viewModel.signUp()
                },
                isLoading = authState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text      = "  or  ",
                    style     = MaterialTheme.typography.bodySmall,
                    fontFamily = Poppins,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            CalorEaseOutlinedButton(
                text    = "Continue with Google",
                onClick = { launchGoogleOAuth() },
                icon    = {
                    Icon(
                        painter            = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google",
                        modifier           = Modifier.size(20.dp),
                        tint               = Color.Unspecified
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment    = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Already have an account? ",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text            = "Log In",
                    style           = MaterialTheme.typography.bodyMedium,
                    fontFamily      = Poppins,
                    fontWeight      = FontWeight.SemiBold,
                    color           = DarkTurquoise,
                    textDecoration  = TextDecoration.Underline,
                    modifier        = Modifier.clickable { navController.popBackStack() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
