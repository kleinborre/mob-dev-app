package com.sample.calorease.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember  // PHASE 3: For snackbarHostState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState: AuthState by viewModel.authState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    
    // BUGFIX Issue 2: Only show back button for NEW users coming from Getting Started
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val previousRoute = navController.previousBackStackEntry?.destination?.route
    
    // Session manager for both account deletion check and login history
    val sessionManager = remember { com.sample.calorease.data.session.SessionManager(context) }
    var hasEverLoggedIn by remember { mutableStateOf<Boolean>(false) }
    
    // Combined LaunchedEffect: Check account deletion & login history & load last email
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // PHASE 3: Show account deletion success message
        if (sessionManager.wasAccountDeleted()) {
            snackbarHostState.showSnackbar(
                message = "Account successfully deleted",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            sessionManager.clearAccountDeletionFlag()
        }
        
        // BUGFIX Issue 2: Check if user has ever logged in (new vs returning user)
        hasEverLoggedIn = sessionManager.hasEverLoggedIn()
        
        // BUGFIX Issue 7: Load last login email for auto-fill (if exists)
        val lastEmail = sessionManager.getLastLoginEmail()
        if (!lastEmail.isNullOrEmpty()) {
            viewModel.updateEmail(lastEmail)
        }
    }
    
    // Only show back if: came from Getting Started AND user has never logged in
    val canGoBack = previousRoute == Screen.GettingStarted.route && !hasEverLoggedIn
    
    // CRITICAL: Navigate on login success. 
    // Key is isLoginSuccess (false→true→false on every login) — never stale.
    // navigateToDashboard/navigateToOnboarding alone could be cached from a previous
    // ViewModel instance and not re-fire the effect on subsequent logins.
    LaunchedEffect(authState.isLoginSuccess) {
        if (!authState.isLoginSuccess) return@LaunchedEffect
        when {
            authState.navigateToDashboard -> {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(0) { inclusive = true }  // Full clear — works regardless of backstack state
                }
            }
            authState.navigateToOnboarding -> {
                navController.navigate(Screen.OnboardingName.route) {
                    popUpTo(0) { inclusive = true }  // Full clear
                }
            }
        }
        viewModel.resetSuccessFlags()
    }
    
    AuthScaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },  // PHASE 3
        // BUGFIX Issue 2: Only show back button for new users from Getting Started
        onBackClick = if (canGoBack) {
            {
                // Navigate to Getting Started explicitly, clear backstack
                navController.navigate(Screen.GettingStarted.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else null  // No back button for returning users or direct login access
    ) { paddingValues ->
        // Main Container acts as the structural column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Vertically center content
        ) {
            // Minimized top spacer (was 40.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            // CalorEase Logo Text
            Text(
                text = "calorease",
                style = MaterialTheme.typography.displaySmall,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = DarkTurquoise
            )
            
            // Reduced spacer (was 40.dp)
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
            
            // Reduced spacer (was 48.dp)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Email Field
            CalorEaseTextField(
                value = authState.email,
                onValueChange = { email ->
                    // REAL FIX: Lambda wrapper ensures immediate state update
                    viewModel.updateEmail(email)
                },
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
                onValueChange = { password ->
                    viewModel.updatePassword(password)
                },
                label = "Password",
                placeholder = "Enter your password",
                isPassword = true,
                isError = authState.passwordError != null,
                errorMessage = authState.passwordError ?: ""
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Forgot Password Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins,
                    color = DarkTurquoise,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Button
            CalorEaseButton(
                text = "Login",
                onClick = {
                    // REAL FIX: Capture email/password from current authState snapshot
                    // This ensures we use the EXACT state at click time, not stale state
                    val currentEmail = authState.email
                    val currentPassword = authState.password
                    viewModel.login(email = currentEmail, password = currentPassword)
                },
                isLoading = authState.isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Google Sign In Button
            CalorEaseOutlinedButton(
                text = "Sign in with Google",
                onClick = viewModel::googleSignIn,
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google logo",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create Account Link
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
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
