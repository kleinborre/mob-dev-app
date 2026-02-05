package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sample.calorease.presentation.components.CalorEaseTextField
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCalorieSheet(
    onDismiss: () -> Unit,
    onSave: (String, Int, String) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf<String?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    
    val isFormValid = foodName.isNotBlank() && 
                     calories.toIntOrNull() != null && 
                     (calories.toIntOrNull() ?: 0) > 0 &&
                     selectedMealType != null
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Food Entry",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = DarkTurquoise
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Food Name
            CalorEaseTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = "Food Name",
                placeholder = "e.g., Grilled Chicken",
                keyboardType = KeyboardType.Text
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calories
            CalorEaseTextField(
                value = calories,
                onValueChange = { calories = it.filter { char -> char.isDigit() } },
                label = "Calories",
                placeholder = "e.g., 350",
                keyboardType = KeyboardType.Number
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Meal Type Selection
            Text(
                text = "Meal Type",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Meal Type Chips
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealTypeChip(
                        label = "Breakfast",
                        icon = Icons.Default.BreakfastDining,
                        isSelected = selectedMealType == "Breakfast",
                        onClick = { selectedMealType = "Breakfast" },
                        modifier = Modifier.weight(1f)
                    )
                    MealTypeChip(
                        label = "Lunch",
                        icon = Icons.Default.LunchDining,
                        isSelected = selectedMealType == "Lunch",
                        onClick = { selectedMealType = "Lunch" },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealTypeChip(
                        label = "Dinner",
                        icon = Icons.Default.DinnerDining,
                        isSelected = selectedMealType == "Dinner",
                        onClick = { selectedMealType = "Dinner" },
                        modifier = Modifier.weight(1f)
                    )
                    MealTypeChip(
                        label = "Snack",
                        icon = Icons.Default.Fastfood,
                        isSelected = selectedMealType == "Snack",
                        onClick = { selectedMealType = "Snack" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Add Log Button
            Button(
                onClick = { showConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkTurquoise,
                    disabledContainerColor = DarkTurquoise.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Add Log",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = DarkTurquoise
                )
            },
            title = {
                Text(
                    text = "Confirm Entry",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Add \"$foodName\" with ${calories} calories to $selectedMealType?",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSave(foodName, calories.toInt(), selectedMealType!!)
                        showConfirmation = false
                    }
                ) {
                    Text("Yes, Add", fontFamily = Poppins, color = DarkTurquoise, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
}

@Composable
fun MealTypeChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) DarkTurquoise else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = Poppins,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
