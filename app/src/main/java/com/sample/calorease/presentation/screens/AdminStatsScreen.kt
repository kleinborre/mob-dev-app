package com.sample.calorease.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.AdminBottomNavigationBar
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.AdminStatsViewModel

/**
 * Admin Statistics Page - Landing page for admin login
 * Shows total users, active/deactivated counts, and signup chart
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen(
    navController: NavController,
    viewModel: AdminStatsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // PHASE 1: Mode saved in AuthViewModel on login, not here
    
    Scaffold(
        bottomBar = { AdminBottomNavigationBar(navController = navController) }
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
                    .verticalScroll(rememberScrollState())
            ) {
                // FIX-1c: Single header with minimal spacing
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "User Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = DarkTurquoise
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stats Cards Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdminStatCard(
                        label = "Total Users",
                        value = state.totalUsers.toString(),
                        color = DarkTurquoise,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Stats Cards Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdminStatCard(
                        label = "Active",
                        value = state.activeUsers.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Deactivated",
                        value = state.deactivatedUsers.toString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Signups Chart
                Text(
                    text = "User Signups (Last 7 Days)",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    UserSignupBarChart(
                        data = state.signupsByDate,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                }
                
                // ✅ Phase A: Removed "Manage Users" button - now on bottom nav
            }
        }
    }
}

@Composable
fun AdminStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Poppins,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UserSignupBarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val barColor = DarkTurquoise
    
    Column(modifier = modifier) {
        // Chart area with Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (data.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        fontFamily = Poppins,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    // Chart dimensions
                    val chartPadding = 40f
                    val chartWidth = canvasWidth - (chartPadding * 2)
                    val chartHeight = canvasHeight - chartPadding
                    
                    // Find max value for scaling
                    val maxValue = (data.maxOfOrNull { it.second } ?: 0).let {
                        if (it == 0) 5 else (it * 1.2).toInt()
                    }
                    
                    val yScale = if (maxValue > 0) chartHeight / maxValue else 0f
                    
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
                            size = Size(barWidth, barHeight.coerceAtLeast(0f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
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
            data.forEach { (label, count) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = Poppins,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = DarkTurquoise,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
