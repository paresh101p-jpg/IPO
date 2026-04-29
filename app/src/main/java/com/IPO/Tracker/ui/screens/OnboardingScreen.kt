@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.IPO.Tracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.IPO.Tracker.R
import com.IPO.Tracker.ui.theme.AccentPrimary
import com.IPO.Tracker.ui.theme.AccentSecondary
import com.IPO.Tracker.ui.theme.AccentTertiary
import com.IPO.Tracker.ui.theme.AccentDanger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val accentColor: Color
)

val onboardingPages = listOf(
    OnboardingPage(
        emoji = "📈",
        title = "Live GMP Tracking",
        subtitle = "Grey Market Premium — Updated Daily",
        description = "Track Grey Market Premium (GMP) of every IPO in real time. Know the estimated listing price before the market opens — so you can decide smarter.",
        accentColor = Color(0xFF00E676)
    ),
    OnboardingPage(
        emoji = "⏳",
        title = "Live Countdown Timers",
        subtitle = "Never Miss An IPO Again",
        description = "Every IPO card shows a live countdown — how many days, hours, and minutes are left to apply. Set calendar reminders in one tap!",
        accentColor = Color(0xFFFFD600)
    ),
    OnboardingPage(
        emoji = "🧮",
        title = "Profit Calculator",
        subtitle = "Know Your Profit Before Applying",
        description = "Select your broker (Zerodha, Groww, Angel, ICICI...) and instantly see your exact net profit after all charges — no guesswork!",
        accentColor = Color(0xFFD4AF37)
    ),
    OnboardingPage(
        emoji = "🕵️",
        title = "Anchor Investor Spy",
        subtitle = "See Where the Big Money Goes",
        description = "Discover which big Mutual Funds and Foreign Investors (like HDFC MF, SBI MF, Goldman Sachs) invested in the IPO — a strong signal of quality.",
        accentColor = Color(0xFFFF9100)
    ),
    OnboardingPage(
        emoji = "🔒",
        title = "100% Safe & Private",
        subtitle = "Your Data Stays With You",
        description = "We use Google Firebase for secure sign-in. No financial data is stored on our servers. PAN numbers stay only on your device. Zero risk.",
        accentColor = AccentSecondary
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(page = onboardingPages[page])
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 28.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) AccentPrimary else AccentPrimary.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip
                TextButton(onClick = onFinish) {
                    Text("Skip", color = Color.Gray, fontWeight = FontWeight.Medium)
                }

                // Next / Get Started
                Button(
                    onClick = {
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onFinish()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black),
                    modifier = Modifier.height(48.dp).widthIn(min = 160.dp)
                ) {
                    Text(
                        if (pagerState.currentPage == onboardingPages.size - 1) "Get Started 🚀" else "Next →",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(page) {
        visible = false
        delay(100)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        page.accentColor.copy(alpha = 0.08f),
                        Color(0xFF000000),
                        Color(0xFF000000)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 140.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Big Emoji
                    Text(
                        text = page.emoji,
                        fontSize = 90.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Accent line
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(page.accentColor)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(
                        text = page.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = page.subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = page.accentColor,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Description
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = page.accentColor.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = page.description,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
