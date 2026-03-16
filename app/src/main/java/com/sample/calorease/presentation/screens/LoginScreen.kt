package com.sample.calorease.presentation.screens

import android.util.Log
import androidx.compose.animation.core.*
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
import androidx.navigation.compose.currentBackStackEntryAsState
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
private const val WEB_CLIENT_ID = "371598324066-6njd7ji52kinoic77admrcslc704ogq3.apps.googleusercontent.com"

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState: AuthState by viewModel.authState.collectAsState()
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dialog         = rememberStatusDialog()
    val soundPlayer    = remember { SoundPlayer(context) }

    var localSnackMsg       by remember { mutableStateOf<String?>(null) }
    val snackbarHostState   = remember { SnackbarHostState() }

    val navBackStackEntry   by navController.currentBackStackEntryAsState()
    val previousRoute       = navController.previousBackStackEntry?.destination?.route
    val sessionManager      = remember { com.sample.calorease.data.session.SessionManager(context) }
    var hasEverLoggedIn     by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (sessionManager.wasAccountDeleted()) {
            snackbarHostState.showSnackbar("Account successfully deleted", duration = SnackbarDuration.Short)
            sessionManager.clearAccountDeletionFlag()
        }
        hasEverLoggedIn = sessionManager.hasEverLoggedIn()
        val lastEmail = sessionManager.getLastLoginEmail()
        if (!lastEmail.isNullOrEmpty()) viewModel.updateEmail(lastEmail)
    }

    // Snackbar for local Credential Manager errors
    LaunchedEffect(localSnackMsg) {
        localSnackMsg?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            localSnackMsg = null
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

    // AuthViewModel-level Google errors -> snackbar
    LaunchedEffect(authState.googleSignInError) {
        authState.googleSignInError?.let {
            soundPlayer.playError()
            dialog.showError(it)
            viewModel.clearGoogleSignInError()
        }
    }

    val canGoBack = previousRoute == Screen.GettingStarted.route && !hasEverLoggedIn

    // Navigate on successful login (email OR Google — called from either flow)
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

    // ── Google Sign-In (two-pass for speed) ──────────────────────────────────
    // Pass 1: filterByAuthorizedAccounts=true (instant for returning users)
    // Pass 2: on NoCredentialException, retry with false (full account picker)
    fun launchGoogleOAuth() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            dialog.showError("Network unavailable. Please connect to the internet.")
            return
        }
        coroutineScope.launch {
            dialog.showLoading("Waiting for accounts...")
            try {
                suspend fun getCredential(filterAuthorized: Boolean): String {
                    val option = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(filterAuthorized)
                        .setServerClientId(WEB_CLIENT_ID)
                        .build()
                    val result = CredentialManager.create(context).getCredential(
                        context,
                        GetCredentialRequest.Builder().addCredentialOption(option).build()
                    )
                    return GoogleIdTokenCredential.createFrom(result.credential.data).idToken
                }

                val idToken = try {
                    getCredential(true)       // fast path — already authorised
                } catch (e: NoCredentialException) {
                    getCredential(false)      // slow path — full picker
                }

                // ViewModel takes over — isLoginSuccess LaunchedEffect handles navigation
                viewModel.googleSignIn(idToken)

            } catch (e: GetCredentialCancellationException) {
                dialog.dismiss()
                Log.d("LoginScreen", "Google Sign-In cancelled")
            } catch (e: NoCredentialException) {
                soundPlayer.playError()
                dialog.showError("No Google accounts found on this device.")
            } catch (e: Exception) {
                Log.e("LoginScreen", "OAuth error", e)
                soundPlayer.playError()
                dialog.showError("Google Sign-In unavailable. Check your connection.")
            }
        }
    }

    // Render the status dialog above everything
    dialog.Render()

    AuthScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        onBackClick = if (canGoBack) {
            { navController.navigate(Screen.GettingStarted.route) { popUpTo(0) { inclusive = true } } }
        } else null
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "calorease",
                style = MaterialTheme.typography.displaySmall,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = DarkTurquoise
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = DarkTurquoise
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Login to continue",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = Poppins
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            CalorEaseTextField(
                value = authState.password,
                onValueChange = viewModel::updatePassword,
                label = "Password",
                placeholder = "Enter your password",
                isPassword = true,
                isError = authState.passwordError != null,
                errorMessage = authState.passwordError ?: ""
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins,
                    color = DarkTurquoise,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { navController.navigate(Screen.ForgotPassword.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            CalorEaseButton(
                text = "Login",
                onClick = { 
                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        dialog.showError("Network unavailable. Please connect to the internet to log in.")
                        return@CalorEaseButton
                    }
                    viewModel.login(email = authState.email, password = authState.password) 
                },
                isLoading = authState.isLoading
            )

            if (authState.showResendVerification) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { viewModel.resendVerificationEmail() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Haven't received it? Resend Verification Email",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = Poppins
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  or  ",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            CalorEaseOutlinedButton(
                text = "Continue with Google",
                onClick = { launchGoogleOAuth() },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Create an Account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTurquoise,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { navController.navigate(Screen.SignUp.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

}
