package com.IPO.Tracker.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.IPO.Tracker.R
import com.IPO.Tracker.ui.theme.AccentPrimary
import com.IPO.Tracker.ui.theme.AccentSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashDone: () -> Unit) {
    val logoScale = remember { Animatable(0.3f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val logoOffset = remember { Animatable(80f) }
    val density = LocalDensity.current.density

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, animationSpec = tween(400))
        logoScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        logoOffset.animateTo(0f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        textAlpha.animateTo(1f, animationSpec = tween(600))
        taglineAlpha.animateTo(1f, animationSpec = tween(600))
        delay(1500)
        onSplashDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF000000), Color(0xFF111111), Color(0xFF000000))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Real Logo Image - animated with scale + translate, NO clipping
            Image(
                painter = painterResource(id = R.drawable.ipo_logo),
                contentDescription = "IPO Tracker Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .graphicsLayer { translationY = logoOffset.value * density }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // App Name
            Text(
                text = "IPO Tracker",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "India's Smartest Investment Companion",
                fontSize = 13.sp,
                color = AccentSecondary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Pulsating loading dots
            Row(
                modifier = Modifier.alpha(taglineAlpha.value),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val dotAlpha by rememberInfiniteTransition(label = "dot$index").animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(dotAlpha)
                            .background(AccentPrimary, CircleShape)
                    )
                }
            }
        }
    }
}
