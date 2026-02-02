package com.sample.calorease.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sample.calorease.R
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.CalorEaseOutlinedButton
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins

@Composable
fun GettingStartedScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000L)
            val nextPage = (pagerState.currentPage + 1) % 3
            pagerState.animateScrollToPage(nextPage)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        //  Carousel
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(page)
        }
        
        // Page Indicators
        Row(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .then(
                            if (index == pagerState.currentPage)
                                Modifier.clip(CircleShape).size(12.dp, 8.dp)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 32.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .then(
                                Modifier.clip(
                                    if (index == pagerState.currentPage)
                                        androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                    else CircleShape
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawCircle(
                                color = if (index == pagerState.currentPage)
                                    Color(0xFF0097B2)
                                else
                                    Color(0xFF0097B2).copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign Up Button
        CalorEaseButton(
            text = "Sign Up",
            onClick = { navController.navigate(Screen.SignUp.route) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Login Button
        CalorEaseOutlinedButton(
            text = "Login",
            onClick = { navController.navigate(Screen.Login.route) }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun OnboardingPage(page: Int) {
    val imageRes = when (page) {
        0 -> R.drawable.intro_slide_1
        1 -> R.drawable.intro_slide_2
        else -> R.drawable.intro_slide_3
    }
    
    val title = when (page) {
        0 -> "Welcome to CalorEase"
        1 -> "Track Your Goals"
        else -> "Achieve Success"
    }
    
    val description = when (page) {
        0 -> "Your personal calorie tracking companion for a healthier lifestyle"
        1 -> "Set and monitor your fitness goals with precision"
        else -> "Watch your progress and celebrate achievements"
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Full-screen background image
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Semi-transparent overlay for better text visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )
        
        // Centered text overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = DarkTurquoise,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = Poppins,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
