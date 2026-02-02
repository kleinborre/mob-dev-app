package com.sample.calorease.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.sample.calorease.presentation.components.BottomNavigationBar
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.StatisticsViewModel
import com.sample.calorease.util.DateUtils

@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.statisticsState.collectAsState()
    
    Scaffold(
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Weekly Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                CalorEaseCard {
                    Text(
                        text = "Calories Consumed (Last 7 Days)",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (state.chartData.isNotEmpty()) {
                        val chartEntryModel = entryModelOf(
                            *state.chartData.map { it.calories }.toTypedArray()
                        )
                        
                        val dayLabels = state.chartData.map { DateUtils.getDayOfWeek(it.date) }
                        
                        val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                            dayLabels.getOrNull(value.toInt()) ?: ""
                        }
                        
                        Chart(
                            chart = columnChart(
                                columns = listOf(
                                    lineComponent(
                                        color = DarkTurquoise
                                    )
                                )
                            ),
                            model = chartEntryModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = bottomAxisValueFormatter
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No data available",
                                fontFamily = Poppins,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Statistics Summary
                CalorEaseCard {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val totalCalories = state.chartData.sumOf { it.calories.toDouble() }
                    val avgCalories = if (state.chartData.isNotEmpty()) {
                        totalCalories / state.chartData.size
                    } else 0.0
                    
                    StatRow("Total Calories", "${totalCalories.toInt()} cal")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    StatRow("Average per Day", "${avgCalories.toInt()} cal")
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
