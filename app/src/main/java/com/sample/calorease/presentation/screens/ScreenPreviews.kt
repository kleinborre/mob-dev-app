package com.sample.calorease.presentation.screens

/**
 * ScreenPreviews.kt
 *
 * Contains @Preview composables for all 17 screens.
 * These previews intentionally do NOT use hiltViewModel() — they inline mock
 * state directly so the Android Studio preview renderer can display them without
 * a running Dagger/Hilt graph.
 *
 * Strategy:
 *  - Auth screens that use AuthViewModel → inline UI with AuthState()
 *  - Onboarding screens that use OnboardingViewModel → inline UI with OnboardingState()
 *  - Dashboard/Settings → replicate Scaffold + minimal content blocks
 *  - Admin screens → replicate Scaffold + stub content
 *  - ForgotPasswordScreen has no ViewModel → calls screen directly
 */

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseOutlinedButton
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.theme.CalorEaseTheme
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins

// ──────────────────────────────────────────────────────────────
// AUTH SCREENS
// ──────────────────────────────────────────────────────────────

@Preview(name = "Login Screen - Light", showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
                Text(text = "Login to continue", fontFamily = Poppins)
                Spacer(modifier = Modifier.height(24.dp))
                CalorEaseTextField(
                    value = "",
                    onValueChange = {},
                    label = "Email",
                    placeholder = "Enter your email",
                    leadingIcon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseTextField(
                    value = "",
                    onValueChange = {},
                    label = "Password",
                    placeholder = "Enter your password",
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                CalorEaseButton(text = "Login", onClick = {})
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseOutlinedButton(text = "Sign in with Google", onClick = {})
            }
        }
    }
}

@Preview(name = "Login Screen - Dark", showBackground = true, showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenDarkPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "calorease",
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(24.dp))
                CalorEaseTextField(value = "", onValueChange = {}, label = "Email")
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseTextField(value = "", onValueChange = {}, label = "Password", isPassword = true)
                Spacer(modifier = Modifier.height(24.dp))
                CalorEaseButton(text = "Login", onClick = {})
            }
        }
    }
}

@Preview(name = "Login Screen - Error State", showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenErrorPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "calorease",
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(24.dp))
                CalorEaseTextField(
                    value = "notanemail",
                    onValueChange = {},
                    label = "Email",
                    isError = true,
                    errorMessage = "No account found with this email"
                )
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseTextField(value = "pass", onValueChange = {}, label = "Password", isPassword = true)
                Spacer(modifier = Modifier.height(24.dp))
                CalorEaseButton(text = "Login", onClick = {})
            }
        }
    }
}

@Preview(name = "Sign Up Screen - Light", showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "calorease",
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Sign up to get started", fontFamily = Poppins)
                Spacer(modifier = Modifier.height(24.dp))
                CalorEaseTextField(value = "", onValueChange = {}, label = "Email", leadingIcon = Icons.Default.Email)
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseTextField(value = "", onValueChange = {}, label = "Password", isPassword = true)
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseTextField(value = "", onValueChange = {}, label = "Confirm Password", isPassword = true)
                Spacer(modifier = Modifier.height(32.dp))
                CalorEaseButton(text = "Sign Up", onClick = {})
            }
        }
    }
}

// ForgotPasswordScreen has no hiltViewModel — can preview directly
@Preview(name = "Forgot Password Screen - Light", showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    CalorEaseTheme {
        ForgotPasswordScreen(navController = rememberNavController())
    }
}

// ──────────────────────────────────────────────────────────────
// ONBOARDING SCREENS
// ──────────────────────────────────────────────────────────────

@Preview(name = "Onboarding Name - Light", showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingNameScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "What's your name?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Step 1 of 4", fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(32.dp))
                CalorEaseTextField(value = "", onValueChange = {}, label = "First Name",
                    leadingIcon = Icons.Default.Person)
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseTextField(value = "", onValueChange = {}, label = "Last Name",
                    leadingIcon = Icons.Default.Person)
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseTextField(value = "", onValueChange = {}, label = "Nickname (optional)")
                Spacer(modifier = Modifier.height(32.dp))
                CalorEaseButton(text = "Next", onClick = {})
            }
        }
    }
}

@Preview(name = "Onboarding Stats - Light", showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingStatsScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Your Physical Stats",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Step 2 of 4", fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(32.dp))
                CalorEaseTextField(value = "25", onValueChange = {}, label = "Age")
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseTextField(value = "175", onValueChange = {}, label = "Height (cm)")
                Spacer(modifier = Modifier.height(16.dp))
                CalorEaseTextField(value = "70", onValueChange = {}, label = "Weight (kg)")
                Spacer(modifier = Modifier.height(32.dp))
                // Gender selection buttons row preview
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTurquoise),
                        modifier = Modifier.weight(1f)
                    ) { Text("Male", fontFamily = Poppins) }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                        Text("Female", fontFamily = Poppins)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                CalorEaseButton(text = "Next", onClick = {})
            }
        }
    }
}

@Preview(name = "Onboarding Goals - Light", showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingGoalsScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "What's your goal?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Step 3 of 4", fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(32.dp))
                listOf("Lose Weight (Fast)", "Lose Weight (Slow)", "Maintain Weight",
                    "Gain Weight (Slow)", "Gain Weight (Fast)").forEach { goal ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (goal == "Lose Weight (Fast)") DarkTurquoise else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = goal,
                            fontFamily = Poppins,
                            color = if (goal == "Lose Weight (Fast)") Color.White
                                else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                CalorEaseButton(text = "Next", onClick = {})
            }
        }
    }
}

@Preview(name = "Onboarding Results - Light", showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingResultsScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Your Daily Target",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Step 4 of 4", fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "2,124",
                    style = MaterialTheme.typography.displayLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Text(text = "kcal / day", fontFamily = Poppins)
                Spacer(modifier = Modifier.height(32.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        listOf("BMR" to "1,700 kcal", "Activity" to "Lightly Active",
                            "Goal" to "Lose Weight").forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontFamily = Poppins)
                                Text(value, fontFamily = Poppins, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                CalorEaseButton(text = "Start Tracking", onClick = {})
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// USER SCREENS
// ──────────────────────────────────────────────────────────────

@Preview(name = "Dashboard - Light", showBackground = true, showSystemUi = true)
@Composable
private fun DashboardScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Good Morning, TestUser",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Progress card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkTurquoise)
                ) {
                    Column(modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("1,200 / 2,078 kcal", fontFamily = Poppins,
                            fontWeight = FontWeight.Bold, color = Color.White,
                            style = MaterialTheme.typography.headlineMedium)
                        Text("You're doing great!", fontFamily = Poppins, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Stub food entries
                repeat(3) { idx ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Food Item ${idx + 1}", fontFamily = Poppins)
                            Text("${(idx + 1) * 300} kcal", fontFamily = Poppins,
                                color = DarkTurquoise, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Food Logs - Light", showBackground = true, showSystemUi = true)
@Composable
private fun FoodLogsScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Food History",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(16.dp))
                repeat(5) { idx ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Food Entry ${idx + 1}", fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold)
                                Text("2026-03-1${idx + 1}", fontFamily = Poppins,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${(idx + 1) * 250} kcal", fontFamily = Poppins,
                                color = DarkTurquoise, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Settings - Light", showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(24.dp))
                // Profile section stub
                listOf("Current Weight" to "75 kg", "Daily Goal" to "2,078 kcal",
                    "Weight Goal" to "Lose Weight").forEach { (label, value) ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, fontFamily = Poppins)
                            Text(value, fontFamily = Poppins, color = DarkTurquoise,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTurquoise),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Sign Out", fontFamily = Poppins) }
            }
        }
    }
}

@Preview(name = "Statistics - Light", showBackground = true, showSystemUi = true)
@Composable
private fun StatisticsScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Weekly Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(
                        DarkTurquoise.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Chart Placeholder", fontFamily = Poppins,
                                color = DarkTurquoise, fontWeight = FontWeight.Bold)
                            Text("(Vico chart renders at runtime)",
                                fontFamily = Poppins,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Summary", fontFamily = Poppins, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total (7 days)", fontFamily = Poppins)
                            Text("12,450 cal", fontFamily = Poppins, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Daily Average", fontFamily = Poppins)
                            Text("1,778 cal", fontFamily = Poppins, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Stats Chart - Light", showBackground = true, showSystemUi = true)
@Composable
private fun StatsScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Calorie Stats",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Bar chart preview stubs
                listOf("Mon" to 0.6f, "Tue" to 0.8f, "Wed" to 0.4f,
                    "Thu" to 1.0f, "Fri" to 0.7f, "Sat" to 0.3f, "Sun" to 0.9f)
                    .forEach { (day, pct) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(day, fontFamily = Poppins,
                                modifier = Modifier.width(40.dp),
                                style = MaterialTheme.typography.bodySmall)
                            Box(
                                modifier = Modifier
                                    .weight(pct)
                                    .height(20.dp)
                                    .background(DarkTurquoise, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.weight(1f - pct))
                        }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// ADMIN SCREENS
// ──────────────────────────────────────────────────────────────

@Preview(name = "Admin Stats - Light", showBackground = true, showSystemUi = true)
@Composable
private fun AdminStatsScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Admin Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Total Users" to "24", "Active" to "21", "Inactive" to "3")
                        .forEach { (label, count) ->
                            Card(modifier = Modifier.weight(1f)) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(count, fontFamily = Poppins,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkTurquoise)
                                    Text(label, fontFamily = Poppins,
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp)
                        .background(DarkTurquoise.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center) {
                        Text("Registration Trend Chart", fontFamily = Poppins,
                            color = DarkTurquoise)
                    }
                }
            }
        }
    }
}

@Preview(name = "Admin Users - Light", showBackground = true, showSystemUi = true)
@Composable
private fun AdminUsersScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Manage Users",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name, email...", fontFamily = Poppins) },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("5 user(s) found", fontFamily = Poppins,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                repeat(4) { idx ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("User ${idx + 1}", fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold)
                                Text("user${idx + 1}@test.com", fontFamily = Poppins,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkTurquoise.copy(alpha = 0.1f)
                            ) {
                                Text("ACTIVE", fontFamily = Poppins,
                                    color = DarkTurquoise,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Admin Settings - Light", showBackground = true, showSystemUi = true)
@Composable
private fun AdminSettingsScreenPreview() {
    CalorEaseTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Admin Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Account", fontFamily = Poppins, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf("Email" to "admin@calorease.com", "Role" to "Administrator")
                            .forEach { (label, value) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(label, fontFamily = Poppins)
                                    Text(value, fontFamily = Poppins, color = DarkTurquoise)
                                }
                            }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = DarkTurquoise),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Switch to User Mode", fontFamily = Poppins) }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Sign Out", fontFamily = Poppins) }
            }
        }
    }
}
