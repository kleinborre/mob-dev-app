package com.sample.calorease.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.sample.calorease.presentation.components.BottomNavigationBar
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.theme.AestheticWhite
import com.sample.calorease.presentation.theme.DeepTeal
import com.sample.calorease.presentation.theme.DeepTealLight
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.StatisticsViewModel
import com.sample.calorease.util.DateUtils
import kotlin.math.absoluteValue

@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.statisticsState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshData() }

    // Screen gradient background
    val gradient = Brush.verticalGradient(
        listOf(AestheticWhite, Color(0xFFECF7F8), Color(0xFFDDF1F3))
    )

    Scaffold(
        bottomBar      = { BottomNavigationBar(navController = navController) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(gradient)) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkTurquoise)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text       = "Weekly Overview",
                        style      = MaterialTheme.typography.titleLarge,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color      = DeepTeal
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text       = "Your calorie activity at a glance",
                        fontFamily = Poppins,
                        fontSize   = 13.sp,
                        color      = DeepTeal.copy(alpha = 0.60f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── 7-day bar chart ─────────────────────────────────────────
                    CalorEaseCard(innerPadding = 16.dp) {
                        Text(
                            text       = "Calories — Last 7 Days",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            style      = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (state.chartData.isNotEmpty()) {
                            val chartModel = entryModelOf(
                                *state.chartData.map { it.calories }.toTypedArray()
                            )
                            val dayLabels = state.chartData.map { DateUtils.getDayOfWeek(it.date) }
                            val xFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { v, _ ->
                                dayLabels.getOrNull(v.toInt()) ?: ""
                            }

                            Chart(
                                chart      = columnChart(
                                    columns = listOf(
                                        lineComponent(color = DarkTurquoise)
                                    )
                                ),
                                model      = chartModel,
                                startAxis  = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(valueFormatter = xFormatter),
                                modifier   = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mini summary row under chart
                            val total = state.chartData.sumOf { it.calories.toDouble() }
                            val avg   = if (state.chartData.isNotEmpty()) total / state.chartData.size else 0.0
                            Row(
                                modifier       = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MiniStat("Total", "${total.toInt()} cal")
                                MiniStat("Avg/Day", "${avg.toInt()} cal")
                                MiniStat("Days Tracked", "${state.chartData.count { it.calories > 0 }}")
                            }
                        } else {
                            Box(
                                modifier         = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = "Start logging meals to see your chart",
                                    fontFamily = Poppins,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Today's goal progress ───────────────────────────────────
                    CalorEaseCard(innerPadding = 16.dp) {
                        val progress      = if (state.goalCalories > 0)
                            (state.todayCalories / state.goalCalories).coerceIn(0f, 1f)
                        else 0f
                        val isOver        = state.todayCalories > state.goalCalories
                        val remaining     = (state.goalCalories - state.todayCalories).absoluteValue.toInt()
                        val progressColor = if (isOver) Color(0xFFE57373) else DeepTeal
                        val progressLight = if (isOver) Color(0xFFFFCDD2) else DeepTealLight.copy(alpha = 0.25f)

                        Row(
                            modifier       = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text       = "Today's Goal",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                style      = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text       = "${state.todayCalories.toInt()} / ${state.goalCalories.toInt()} cal",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 13.sp,
                                color      = progressColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(progressLight)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(progressColor.copy(alpha = 0.7f), progressColor)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text       = if (isOver) "+$remaining cal over goal" else "$remaining cal remaining",
                            fontFamily = Poppins,
                            fontSize   = 12.sp,
                            color      = progressColor.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Weight vs target ─────────────────────────────────────────
                    if (state.currentWeight > 0f) {
                        CalorEaseCard(innerPadding = 16.dp) {
                            Text(
                                text       = "Weight Progress",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                style      = MaterialTheme.typography.titleSmall
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier       = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                WeightStat(
                                    label = "Current",
                                    value = "${"%.1f".format(state.currentWeight)} kg",
                                    color = DeepTeal
                                )
                                // Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(56.dp)
                                        .background(DeepTeal.copy(alpha = 0.15f))
                                )
                                val diff = state.currentWeight - state.targetWeight
                                WeightStat(
                                    label = "Target",
                                    value = "${"%.1f".format(state.targetWeight)} kg",
                                    color = if (diff <= 0f) Color(0xFF43A047) else Color(0xFFE57373)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(56.dp)
                                        .background(DeepTeal.copy(alpha = 0.15f))
                                )
                                val absDiff = diff.absoluteValue
                                WeightStat(
                                    label = if (diff <= 0f) "Lost" else "To Lose",
                                    value = "${"%.1f".format(absDiff)} kg",
                                    color = if (diff <= 0f) Color(0xFF43A047) else Color(0xFFE57373)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── Mini stat block for chart footer ────────────────────────────────────────

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize   = 14.sp,
            color      = DeepTeal
        )
        Text(
            text       = label,
            fontFamily = Poppins,
            fontSize   = 11.sp,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Weight stat column ───────────────────────────────────────────────────────

@Composable
private fun WeightStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp,
            color      = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text       = label,
            fontFamily = Poppins,
            fontSize   = 12.sp,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Stat row (kept for compat) ───────────────────────────────────────────────

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier       = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontFamily = Poppins, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, fontFamily = Poppins, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium)
    }
}
