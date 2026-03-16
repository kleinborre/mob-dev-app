package com.sample.calorease.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.presentation.components.BottomNavigationBar
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.theme.*
import com.sample.calorease.presentation.viewmodel.FoodHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

private val foodLogsGradient = Brush.verticalGradient(
    colors = listOf(AestheticWhite, PaperWhite, OffWhite, SubtleGray)
)

/**
 * Terminal Final Phase 1.1: Food History Screen
 * - Removed calendar filter entirely
 * - Shows ALL historical entries newest→oldest
 * - Paginated: up to 5 cards per page, "Load More" button appears when > 5 entries exist
 * - Uses dedicated FoodHistoryViewModel (not DashboardViewModel)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogsScreen(
    navController: NavController,
    viewModel: FoodHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Food History",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = DarkTurquoise
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(foodLogsGradient)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkTurquoise)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Header ─────────────────────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Entries",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = DarkTurquoise
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = DarkTurquoise.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${state.totalCount} total",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = DarkTurquoise,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // ── Entry Cards ────────────────────────────────────────
                    if (state.allEntries.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No food entries yet.\nStart tracking your meals!",
                                    fontFamily = Poppins,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    } else {
                        items(
                            items = state.pagedEntries,
                            key = { it.entryId }
                        ) { entry ->
                            FoodHistoryCard(entry = entry)
                        }

                        // ── Load More Button ───────────────────────────────
                        if (state.hasMore) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = viewModel::loadNextPage,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = DarkTurquoise
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, DarkTurquoise
                                    )
                                ) {
                                    val remaining = state.totalCount - state.pagedEntries.size
                                    Text(
                                        text = "Load more  (+${minOf(remaining, 5)} of $remaining remaining)",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else if (state.totalCount > 0) {
                            item {
                                Text(
                                    text = "— All ${state.totalCount} entries shown —",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontFamily = Poppins,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodHistoryCard(entry: DailyEntryEntity) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy  •  hh:mm a", Locale.getDefault()) }
    val dayFormatter   = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

    CalorEaseCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day bubble
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkTurquoise.copy(alpha = 0.10f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = dayFormatter.format(Date(entry.date)).uppercase(),
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = DarkTurquoise
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold
                )
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
