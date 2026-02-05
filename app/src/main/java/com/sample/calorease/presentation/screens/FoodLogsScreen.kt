package com.sample.calorease.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.presentation.components.BottomNavigationBar
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogsScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    
    // Date formatting
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()) }
    val today = remember { Calendar.getInstance().timeInMillis }
    val todayFormatted = remember { dateFormatter.format(Date(today)) }
    
    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    
    // Filter entries by selected date (or show all)
    val filteredEntries = remember(state.foodEntries, selectedDate) {
        if (selectedDate != null) {
            state.foodEntries.filter { entry ->
                val entryCalendar = Calendar.getInstance().apply { timeInMillis = entry.date }
                val selectedCalendar = Calendar.getInstance().apply { timeInMillis = selectedDate!! }
                entryCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                entryCalendar.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)
            }
        } else {
            state.foodEntries.sortedByDescending { it.date }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Food History",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = DarkTurquoise  // PART 4: Header color (was black)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkTurquoise
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pick Date",
                            tint = DarkTurquoise
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Today's Date or Selected Date
            Text(
                text = if (selectedDate == null) "Today: $todayFormatted" else "Selected: ${dateFormatter.format(Date(selectedDate!!))}",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                color = DarkTurquoise
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Food Entries List
            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedDate == null) "No food entries yet.\nStart tracking your meals!" else "No entries for this date",
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = Poppins,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEntries) { entry ->
                        FoodHistoryItem(entry)
                    }
                }
            }
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) {
                        Text("OK", fontFamily = Poppins, color = DarkTurquoise)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        selectedDate = null // Clear filter
                        showDatePicker = false
                    }) {
                        Text("Show All", fontFamily = Poppins)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun FoodHistoryItem(entry: DailyEntryEntity) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormatter.format(Date(entry.date)),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = Poppins,
                    color = Color.Gray
                )
                Text(
                    text = entry.mealType,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = Poppins,
                    color = DarkTurquoise
                )
            }
            
            Text(
                text = "${entry.calories} cal",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = DarkTurquoise
            )
        }
    }
}
