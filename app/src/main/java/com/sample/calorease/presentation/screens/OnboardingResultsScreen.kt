package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.components.CalorEaseProgressBar
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingResultsScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.onboardingState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Track whether the congratulations dialog is showing
    var showCongratsDialog by remember { mutableStateOf(false) }

    // REALTIME AUTO-CALCULATE: runs when input data arrives from previous steps
    LaunchedEffect(state.height, state.weight, state.age, state.targetWeight) {
        val hasInputData = state.height.isNotBlank() &&
                           state.weight.isNotBlank() &&
                           state.age.isNotBlank() &&
                           state.targetWeight.isNotBlank()

        val needsCalculation = state.bmiValue == 0.0 ||
                               state.bmr == 0.0 ||
                               state.tdee == 0.0 ||
                               state.goalCalories == 0.0

        if (hasInputData && needsCalculation) {
            viewModel.calculateResults()
        }
    }

    // After full save succeeds — show the congratulations dialog (NOT nav yet)
    LaunchedEffect(state.isSaveSuccess) {
        if (state.isSaveSuccess) {
            viewModel.resetSuccessFlag()
            showCongratsDialog = true   // ← dialog is the definitive navigation trigger
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        CalorEaseProgressBar(
            progress = 1f,
            stepText = "Step 4 of 4"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your Health Profile",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Results Card — Column ensures rows stack vertically (CalorEaseCard uses Box internally)
        CalorEaseCard(innerPadding = 16.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ResultRow(label = "BMI", value = "%.1f (%s)".format(state.bmiValue, state.bmiStatus))
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow(label = "Ideal Weight", value = "%.1f kg".format(state.idealWeight))
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow(label = "BMR", value = "%.0f cal/day".format(state.bmr))
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow(label = "TDEE", value = "%.0f cal/day".format(state.tdee))
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow(
                    label = "Daily Calorie Goal",
                    value = "%.0f cal/day".format(state.goalCalories),
                    highlight = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Based on your goals, you should consume approximately " +
                   "${state.goalCalories.toInt()} calories per day.",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Poppins,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        CalorEaseButton(
            text = "Get Started",
            onClick = {
                if (state.bmiValue == 0.0 || state.goalCalories == 0.0) {
                    android.widget.Toast.makeText(
                        context,
                        "Error calculating health metrics. Please go back and verify your information.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } else {
                    viewModel.saveUserStats()   // writes full row + isSaveSuccess→true→dialog
                }
            },
            isLoading = state.isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ── Congratulations Dialog ─────────────────────────────────────────────────
    // This dialog is the DEFINITIVE navigation trigger.
    // It appears ONLY after saveUserStats() succeeds (DB write confirmed).
    // "Let's Go!" calls markOnboardingComplete() (direct SQL UPDATE) then navigates.
    // Two-step write = no silent failure: REPLACE (full row) + UPDATE (flag only).
    if (showCongratsDialog) {
        AlertDialog(
            onDismissRequest = { /* Non-dismissable — must click the button to go to dashboard */ },
            title = {
                Text(
                    text = "Welcome to calorease!",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your health profile is all set.\n" +
                               "Daily calorie goal: ${state.goalCalories.toInt()} cal/day",
                        fontFamily = Poppins,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCongratsDialog = false
                        // Step 2: direct SQL UPDATE — the authoritative completion flag
                        viewModel.markOnboardingComplete()
                        // Navigate with full backstack clear
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkTurquoise
                    )
                ) {
                    Text(
                        text = "Get Started",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

@Composable
fun ResultRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Poppins,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = if (highlight) DarkTurquoise else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
