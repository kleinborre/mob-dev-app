package com.sample.calorease.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseOutlinedButton
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.AestheticWhite
import com.sample.calorease.presentation.theme.DeepTeal
import com.sample.calorease.presentation.theme.DeepTealDark
import com.sample.calorease.presentation.theme.Poppins

// ─── Slide data ───────────────────────────────────────────────────────────────

private data class OnboardingSlide(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val accentLight: Color,
    val accentDark: Color
)

private val slides = listOf(
    OnboardingSlide(
        icon        = Icons.Default.LocalFireDepartment,
        iconTint    = Color(0xFF009BAA),
        title       = "Track Calories Easily",
        subtitle    = "Log every meal in seconds and stay on top of your daily intake with smart insights.",
        accentLight = Color(0xFF009BAA),
        accentDark  = Color(0xFF006874)
    ),
    OnboardingSlide(
        icon        = Icons.Default.TrackChanges,
        iconTint    = Color(0xFF43A047),
        title       = "Set Your Goals",
        subtitle    = "Define personalised calorie and weight targets — then let CalorEase guide you there.",
        accentLight = Color(0xFF66BB6A),
        accentDark  = Color(0xFF2E7D32)
    ),
    OnboardingSlide(
        icon        = Icons.Default.ShowChart,
        iconTint    = Color(0xFF7E57C2),
        title       = "Monitor Your Progress",
        subtitle    = "See weekly charts and trends that show exactly how far you have come on your journey.",
        accentLight = Color(0xFFAB47BC),
        accentDark  = Color(0xFF6A1B9A)
    )
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun GettingStartedScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { slides.size })

    // Auto-advance every 5 s
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000L)
            val next = (pagerState.currentPage + 1) % slides.size
            pagerState.animateScrollToPage(next)
        }
    }

    // ── Root layout: AestheticWhite, full-screen column with SpaceBetween ─────
    // SpaceBetween distributes logo / pager / buttons into natural thirds so no
    // element is cramped and nothing floats to an uncomfortable edge.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AestheticWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp)
                .padding(top = 32.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── TOP — Logo block ─────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "calorease",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 28.sp,
                    color      = DeepTeal,
                    textAlign  = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text       = "Your smart calorie companion",
                    fontFamily = Poppins,
                    fontSize   = 13.sp,
                    color      = DeepTeal.copy(alpha = 0.60f),
                    textAlign  = TextAlign.Center
                )
            }

            // ── MIDDLE — Pager grows to fill the space between top and bottom ─
            HorizontalPager(
                state             = pagerState,
                modifier          = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                userScrollEnabled = true
            ) { page ->
                OnboardingSlidePage(slide = slides[page])
            }

            // ── BOTTOM — Dots + Buttons, grouped together ─────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Page indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    repeat(slides.size) { index ->
                        val isSelected = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 28.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) DeepTeal
                                    else DeepTeal.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                CalorEaseButton(
                    text    = "Get Started — Sign Up",
                    onClick = { navController.navigate(Screen.SignUp.route) }
                )

                CalorEaseOutlinedButton(
                    text    = "I already have an account",
                    onClick = { navController.navigate(Screen.Login.route) }
                )
            }
        }
    }
}

// ─── Single slide composable ──────────────────────────────────────────────────

@Composable
private fun OnboardingSlidePage(slide: OnboardingSlide) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Layered radial gradient circles with icon — slightly smaller for breathing room
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            slide.accentLight.copy(alpha = 0.25f),
                            slide.accentDark.copy(alpha = 0.08f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                slide.accentLight.copy(alpha = 0.45f),
                                slide.accentDark.copy(alpha = 0.20f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = slide.icon,
                    contentDescription = slide.title,
                    tint               = slide.iconTint,
                    modifier           = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text       = slide.title,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp,
            color      = DeepTealDark,
            textAlign  = TextAlign.Center,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text       = slide.subtitle,
            fontFamily = Poppins,
            fontSize   = 14.sp,
            color      = Color(0xFF4A5568),
            textAlign  = TextAlign.Center,
            lineHeight = 22.sp,
            modifier   = Modifier.padding(horizontal = 4.dp)
        )
    }
}
