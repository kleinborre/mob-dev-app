package com.sample.calorease.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.BottomNavigationBar
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.statsState.collectAsState()
    
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
                    .verticalScroll(rememberScrollState())  // ✅ Phase H: Landscape support
            ) {
                // ✅ Phase E: Header matching Settings page
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                
                // FIX-2c: Reduced from 24dp to minimize top whitespace
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = "Last 7 Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Daily calorie consumption",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Chart Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    CalorieBarChart(
                        data = state.weekData.map { it.dayLabel to it.calories },
                        dailyGoal = state.dailyGoal,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(
                        color = DarkTurquoise,
                        label = "Consumed"
                    )
                    LegendItem(
                        color = MaterialTheme.colorScheme.error,
                        label = "Daily Goal",
                        isDashed = true
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Summary Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(
                        label = "Average Daily Intake",  // ✅ Phase F: More descriptive
                        value = "${state.weekData.map { it.calories }.average().toInt()} kcal",  // ✅ Phase F: Add "kcal" unit
                        description = "Based on last 7 days",  // ✅ Phase F: Add context
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCard(
                        label = "Daily Goal",
                        value = "${state.dailyGoal} kcal",  // ✅ Phase F: Add "kcal" unit
                        description = "Your target intake",  // ✅ Phase F: Add context
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CalorieBarChart(
    data: List<Pair<String, Int>>,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    val barColor = DarkTurquoise
    val goalLineColor = MaterialTheme.colorScheme.error
    
    Column(modifier = modifier) {
        // Chart area with Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                if (data.isEmpty()) return@Canvas
                
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // Chart dimensions
                val chartPadding = 40f
                val chartWidth = canvasWidth - (chartPadding * 2)
                val chartHeight = canvasHeight - chartPadding
                
                // Find max value for scaling
                val maxValue = maxOf(
                    data.maxOfOrNull { it.second } ?: 0,
                    dailyGoal
                ).let { if (it == 0) 100 else (it * 1.2).toInt() }
                
                val yScale = chartHeight / maxValue
                
                // ✅ Phase F: Draw horizontal grid lines for better readability
                val gridLineCount = 4
                val gridColor = Color.Gray.copy(alpha = 0.2f)
                for (i in 0..gridLineCount) {
                    val y = canvasHeight - (chartHeight * i / gridLineCount)
                    drawLine(
                        color = gridColor,
                        start = Offset(chartPadding, y),
                        end = Offset(canvasWidth - chartPadding, y),
                        strokeWidth = 1f
                    )
                }
                
                // Draw Daily Goal Line
                val goalY = canvasHeight - (dailyGoal * yScale)
                drawLine(
                    color = goalLineColor,
                    start = Offset(chartPadding, goalY),
                    end = Offset(canvasWidth - chartPadding, goalY),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                
                // Draw Bars
                val barWidth = chartWidth / (data.size * 2)
                val spacing = barWidth
                
                data.forEachIndexed { index, (_, value) ->
                    val barHeight = value * yScale
                    val x = chartPadding + (index * (barWidth + spacing)) + spacing
                    val y = canvasHeight - barHeight
                    
                    // Bar
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                    
                    // ✅ Phase F: Draw value label on top of bar
                    if (value > 0) {
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                            drawText(
                                value.toString(),
                                x + barWidth / 2,
                                y - 10f,
                                paint
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    isDashed: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isDashed) 24.dp else 16.dp, 3.dp)
                .background(color, if (isDashed) RoundedCornerShape(0.dp) else CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Poppins
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    // FIX-2b: Use CalorEaseCard design
    CalorEaseCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                color = DarkTurquoise
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,  // PHASE 5: Smaller font (was headlineMedium)
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface  // FIX-2b: Black instead of DarkTurquoise
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
