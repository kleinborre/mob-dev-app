package com.sample.calorease.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.presentation.components.BottomNavigationBar
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    var showAddCalorieSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<DailyEntryEntity?>(null) }
    var showEditDialog by remember { mutableStateOf<DailyEntryEntity?>(null) }
    
    // PHASE 1: Mode now saved in AuthViewModel on login
    
    // ✅ Refresh on composition (works with state restoration disabled)
    // Only runs once per composition, no flickering
    LaunchedEffect(Unit) {
        android.util.Log.d("DashboardScreen", "🔄 Screen composed - refreshing...")
        viewModel.refreshData()
    }
    
    Scaffold(
        topBar = {
            // No TopAppBar - using custom header in content for better alignment
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCalorieSheet = true },
                containerColor = DarkTurquoise,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Food"
                )
            }
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkTurquoise)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ✅ Phase E: Home header matching Settings page
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = DarkTurquoise
                    )
                }
                
                item { Spacer(modifier = Modifier.height(4.dp)) }  // PART 4: Minimal gap (was 12dp)
                
                // ✅ Welcome Header with capitalized nickname
                item {
                    // ✅ Capitalize each word in nickname
                    val displayName = state.nickname.takeIf { it.isNotBlank() }?.let { nickname ->
                        nickname.split(" ").joinToString(" ") { word ->
                            word.replaceFirstChar { char -> 
                                if (char.isLowerCase()) char.titlecase() else char.toString()
                            }
                        }
                    } ?: "User"
                    
                    Column {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = Poppins,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,  // PHASE 2: Same size as "Home"
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface  // FIX-2a: Black instead of DarkTurquoise
                        )
                    }
                }
                
                // Hero Summary Card with Circular Progress and dynamic background
                item {
                    // ✅ Dynamic background color based on calorie consumption
                    val cardBackgroundColor = if (state.consumedCalories > state.goalCalories) {
                        Color(0xFFE57373) // Red when over goal (better contrast than pink)
                    } else {
                        DarkTurquoise // Normal turquoise
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBackgroundColor  // ✅ Dynamic color
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Calories Remaining",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Circular Progress Indicator
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { state.progress },
                                    modifier = Modifier.size(160.dp),
                                    color = Color.White,
                                    strokeWidth = 12.dp,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${state.remainingCalories}",
                                        style = MaterialTheme.typography.displayMedium,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "left",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = Poppins,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Dynamic Motivational Message
                            val percentage = if (state.goalCalories > 0) {
                                (state.consumedCalories.toFloat() / state.goalCalories * 100).toInt()
                            } else 0
                            
                            val (message, emoji) = when {
                                state.consumedCalories > state.goalCalories -> "Over your daily target" to "⚠️"
                                percentage < 25 -> "Let's get started!" to "💪"
                                percentage < 75 -> "You're doing great!" to "🎯"
                                percentage < 95 -> "Almost there!" to "🔥"
                                percentage <= 100 -> "Perfect target!" to "✅"
                                else -> "Over target" to "⚠️"
                            }
                            
                            val messageColor = if (state.consumedCalories > state.goalCalories) {
                                Color(0xFFFFCDD2) // Light red
                            } else {
                                Color.White
                            }
                            
                            Text(
                                text = "$emoji $message",
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                color = messageColor
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Subtext: Goal and Eaten with dynamic color
                            val consumedColor = if (state.consumedCalories > state.goalCalories) {
                                Color(0xFFFFCDD2) // Light red when over
                            } else {
                                Color.White.copy(alpha = 0.9f)
                            }
                            
                            Text(
                                text = "Goal: ${state.goalCalories} • Eaten: ${state.consumedCalories}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = Poppins,
                                color = consumedColor
                            )
                        }
                    }
                }
                
                // Food History Section Header with View History Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Food",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "${state.foodEntries.size} items",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = Poppins,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        // View History Button
                        TextButton(
                            onClick = { navController.navigate("food_logs") }
                        ) {
                            Text(
                                text = "View History",
                                fontFamily = Poppins,
                                color = DarkTurquoise,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = DarkTurquoise,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // Food Entry List
                if (state.foodEntries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestaurantMenu,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No food logged yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = Poppins,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Tap + to add your first meal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = Poppins,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                } else {
                    items(state.foodEntries) { entry ->
                        FoodEntryItem(
                            entry = entry,
                            onEdit = { showEditDialog = entry },
                            onDelete = { showDeleteDialog = entry }
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }
    
    // Add Calorie Bottom Sheet
    if (showAddCalorieSheet) {
        AddCalorieSheet(
            onDismiss = { showAddCalorieSheet = false },
            onSave = { foodName, calories, mealType ->
                viewModel.addFoodEntry(foodName, calories, mealType)
                showAddCalorieSheet = false
            }
        )
    }
    
    // Delete Confirmation Dialog
    showDeleteDialog?.let { entry ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Remove this item?",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${entry.foodName}\" (${entry.calories} cal)?",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFoodEntry(entry.entryId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Yes, Remove", fontFamily = Poppins, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
    
    // Edit Food Dialog
    showEditDialog?.let { entry ->
        var editedName by remember { mutableStateOf(entry.foodName) }
        var editedCalories by remember { mutableStateOf(entry.calories.toString()) }
        var editedMealType by remember { mutableStateOf(entry.mealType) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = {
                Text(
                    text = "Edit Food Entry",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Food Name", fontFamily = Poppins) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = editedCalories,
                        onValueChange = { if (it.all { char -> char.isDigit() }) editedCalories = it },
                        label = { Text("Calories", fontFamily = Poppins) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Meal Type Dropdown (simplified for now - could use DropdownMenu)
                    Text("Meal Type: $editedMealType", fontFamily = Poppins, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedEntry = entry.copy(
                            foodName = editedName,
                            calories = editedCalories.toIntOrNull() ?: entry.calories,
                            mealType = editedMealType
                        )
                        viewModel.updateFoodEntry(updatedEntry)
                        showEditDialog = null
                    }
                ) {
                    Text("Save", fontFamily = Poppins, color = DarkTurquoise, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("Cancel", fontFamily = Poppins)
                }
            }
        )
    }
}

@Composable
fun FoodEntryItem(
    entry: DailyEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Meal Type Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkTurquoise.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (entry.mealType) {
                            "Breakfast" -> Icons.Default.BreakfastDining
                            "Lunch" -> Icons.Default.LunchDining
                            "Dinner" -> Icons.Default.DinnerDining
                            else -> Icons.Default.Fastfood
                        },
                        contentDescription = entry.mealType,
                        tint = DarkTurquoise,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = entry.foodName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = entry.mealType,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = Poppins,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entry.calories}",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                Text(
                    text = " cal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = DarkTurquoise,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
