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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.presentation.components.BottomNavigationBar
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.components.CalorEaseSnackbarHost
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.AestheticWhite
import com.sample.calorease.presentation.theme.DeepTeal
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.OffWhite
import com.sample.calorease.presentation.theme.PaperWhite
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.theme.SubtleGray
import com.sample.calorease.presentation.ui.UiEvent
import com.sample.calorease.presentation.util.SoundPlayer
import com.sample.calorease.presentation.viewmodel.DashboardViewModel

// Gradient brush used for the screen background
private val dashboardGradient = Brush.verticalGradient(
    colors = listOf(AestheticWhite, PaperWhite, OffWhite, SubtleGray)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<DailyEntryEntity?>(null) }
    var showEditDialog by remember { mutableStateOf<DailyEntryEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val soundPlayer = remember { SoundPlayer(context) }

    // Collect one-shot UI events (success/error Snackbars + sounds)
    LaunchedEffect(Unit) {
        viewModel.refreshData()
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSuccess -> {
                    soundPlayer.playSuccess()
                    snackbarHostState.showSnackbar(
                        message  = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is UiEvent.ShowError -> {
                    soundPlayer.playError()
                    snackbarHostState.showSnackbar(
                        message  = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                else -> Unit
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { soundPlayer.release() }
    }
    
    Scaffold(
        topBar    = {},
        containerColor = Color.Transparent,
        snackbarHost = { CalorEaseSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { viewModel.showAddDialog() },
                containerColor = DeepTeal,
                contentColor   = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Food")
            }
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dashboardGradient)
        ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkTurquoise)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh    = { viewModel.refreshDashboard() },
                modifier     = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding      = PaddingValues(top = 48.dp, bottom = 88.dp)
            ) {
                // Welcome header — name only, no redundant 'Home' title
                item {
                    val displayName = state.nickname.takeIf { it.isNotBlank() }?.let { nickname ->
                        nickname.split(" ").joinToString(" ") { word ->
                            word.replaceFirstChar { char ->
                                if (char.isLowerCase()) char.titlecase() else char.toString()
                            }
                        }
                    } ?: "User"

                    Column {
                        Text(
                            text       = "Good day,",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontFamily = Poppins,
                            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                        Text(
                            text       = displayName,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Hero Summary Card with Circular Progress and dynamic background
                item {
                    // Dynamic background color based on calorie consumption
                    val cardBackgroundColor = if (state.consumedCalories > state.goalCalories) {
                        Color(0xFFE57373) // Red when over goal (better contrast than pink)
                    } else {
                        DarkTurquoise // Normal turquoise
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardBackgroundColor  // Dynamic color
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
                            
                            val message = when {
                                state.consumedCalories > state.goalCalories -> "Over your daily target"
                                percentage < 25 -> "Let's get started!"
                                percentage < 75 -> "You're doing great!"
                                percentage < 95 -> "Almost there!"
                                percentage <= 100 -> "Perfect target!"
                                else -> "Over target"
                            }
                            
                            val messageColor = if (state.consumedCalories > state.goalCalories) {
                                Color(0xFFFFCDD2) // Light red
                            } else {
                                Color.White
                            }
                            
                            Text(
                                text = message,
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
                        CalorEaseCard(
                            modifier = Modifier.fillMaxWidth()
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
            }   // end LazyColumn
            }   // end PullToRefreshBox
        }   // end else
        }   // end Box (gradient background)
    }   // end Scaffold content
    
    // BUGFIX Issue 8: Add Calorie Sheet with persistent state from ViewModel
    if (state.showAddDialog) {
        AddCalorieSheet(
            foodName = state.tempFoodName,
            calories = state.tempCalories,
            selectedMealType = state.tempMealType,
            onFoodNameChange = viewModel::updateTempFoodName,
            onCaloriesChange = viewModel::updateTempCalories,
            onMealTypeChange = viewModel::updateTempMealType,
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { foodName, calories, mealType ->
                viewModel.addFoodEntry(foodName, calories, mealType)
                viewModel.clearTempInput()  // Clear after save
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
} // end DashboardScreen

@Composable
fun FoodEntryItem(
    entry: DailyEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    CalorEaseCard(
        modifier = Modifier.fillMaxWidth()
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
