package com.sample.calorease.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.Gender
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseOutlinedButton
import com.sample.calorease.presentation.components.CalorEaseProgressBar
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingStatsScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.onboardingState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),  // ✅ Add scroll to fix visibility
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Progress Bar
        CalorEaseProgressBar(
            progress = 0.5f,
            stepText = "Step 2 of 4"
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Tell us about yourself",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = DarkTurquoise
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Gender Selection
        Text(
            text = "Gender",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderButton(
                text = "Male",
                isSelected = state.gender == Gender.MALE,
                onClick = { viewModel.updateGender(Gender.MALE) },
                modifier = Modifier.weight(1f),
                selectedColor = DarkTurquoise  // Turquoise for male
            )
            
            GenderButton(
                text = "Female",
                isSelected = state.gender == Gender.FEMALE,
                onClick = { viewModel.updateGender(Gender.FEMALE) },
                modifier = Modifier.weight(1f),
                selectedColor = com.sample.calorease.presentation.theme.PastelPink  // Pastel pink for female
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Height
        CalorEaseTextField(
            value = state.height,
            onValueChange = viewModel::updateHeight,
            label = "Height (cm)",
            placeholder = "e.g., 170",
            keyboardType = KeyboardType.Number,
            isError = state.heightError != null,
            errorMessage = state.heightError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weight
        CalorEaseTextField(
            value = state.weight,
            onValueChange = viewModel::updateWeight,
            label = "Current Weight (kg)",
            placeholder = "e.g., 70",
            keyboardType = KeyboardType.Number,
            isError = state.weightError != null,
            errorMessage = state.weightError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Birthday (Date Picker)
        BirthdaySelector(
            selectedBirthdayMillis = state.birthday,
            displayAge = state.age,
            onBirthdaySelected = viewModel::updateBirthday,
            isError = state.ageError != null,
            errorMessage = state.ageError ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Activity Level
        Text(
            text = "Activity Level",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ActivityLevelSelector(
            selectedLevel = state.activityLevel,
            onLevelSelected = viewModel::updateActivityLevel
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalorEaseOutlinedButton(
                text = "Back",
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            )
            
            CalorEaseButton(
                text = "Next",
                onClick = {
                    if (viewModel.validateStats()) {
                        viewModel.saveStepTwo()  // ✅ PHASE 2: Save before navigate
                        navController.navigate(Screen.OnboardingGoals.route)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun GenderButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = DarkTurquoise  // Allow custom color
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = text,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActivityLevelSelector(
    selectedLevel: ActivityLevel,
    onLevelSelected: (ActivityLevel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ActivityLevel.entries.forEach { level ->
            val levelText = when (level) {
                ActivityLevel.SEDENTARY -> "Sedentary (little/no exercise)"
                ActivityLevel.LIGHTLY_ACTIVE -> "Lightly Active (1-3 days/week)"
                ActivityLevel.MODERATELY_ACTIVE -> "Moderately Active (3-5 days/week)"
                ActivityLevel.VERY_ACTIVE -> "Very Active (6-7 days/week)"
                ActivityLevel.EXTRA_ACTIVE -> "Extra Active (physical job + training)"
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedLevel == level),
                        onClick = { onLevelSelected(level) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedLevel == level),
                    onClick = { onLevelSelected(level) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = DarkTurquoise
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = levelText,
                    fontFamily = Poppins,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdaySelector(
    selectedBirthdayMillis: Long?,
    displayAge: String,
    onBirthdaySelected: (Long) -> Unit,
    isError: Boolean,
    errorMessage: String
) {
    var showDatePicker by androidx.compose.runtime.remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = "Birthday",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) 
                                 else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedBirthdayMillis != null) {
                        val calendar = java.util.Calendar.getInstance().apply {
                            timeInMillis = selectedBirthdayMillis
                        }
                        val year = calendar.get(java.util.Calendar.YEAR)
                        val month = calendar.get(java.util.Calendar.MONTH) + 1
                        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        "$day/${if (month < 10) "0$month" else month}/$year (Age: $displayAge)"
                    } else {
                        "Select your birth date"
                    },
                    fontFamily = Poppins,
                    color = if (selectedBirthdayMillis != null) 
                            MaterialTheme.colorScheme.onSurface 
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        if (isError && errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = Poppins
            )
        }
    }
    
    if (showDatePicker) {
        // Calculate year range for users at least 13 years old
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val minYear = 1900
        val maxYear = currentYear - 13  // Oldest year that makes user ≥13
        
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedBirthdayMillis ?: System.currentTimeMillis(),
            yearRange = IntRange(minYear, maxYear),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val selectedCal = java.util.Calendar.getInstance().apply {
                        timeInMillis = utcTimeMillis
                    }
                    val today = java.util.Calendar.getInstance()
                    
                    // Disable today and future dates
                    if (selectedCal.get(java.util.Calendar.YEAR) > today.get(java.util.Calendar.YEAR)) {
                        return false
                    }
                    if (selectedCal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                        selectedCal.get(java.util.Calendar.DAY_OF_YEAR) >= today.get(java.util.Calendar.DAY_OF_YEAR)) {
                        return false
                    }
                    
                    // Calculate age
                    var age = today.get(java.util.Calendar.YEAR) - selectedCal.get(java.util.Calendar.YEAR)
                    if (today.get(java.util.Calendar.DAY_OF_YEAR) < selectedCal.get(java.util.Calendar.DAY_OF_YEAR)) {
                        age--
                    }
                    
                    // Disable if age < 13
                    return age >= 13
                }
                
                override fun isSelectableYear(year: Int): Boolean {
                    // Only allow years that make user at least 13
                    return year <= maxYear
                }
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onBirthdaySelected(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", fontFamily = Poppins, color = DarkTurquoise)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select your birth date",
                        fontFamily = Poppins,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = DarkTurquoise
                )
            )
        }
    }
}
