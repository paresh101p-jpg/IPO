package com.IPO.Tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HypeMeter(hypeLevel: String) {
    val (progress, label, emoji) = when (hypeLevel.lowercase()) {
        "very high" -> Triple(1.0f, "Very High", "🔥")
        "high"      -> Triple(0.75f, "High", "🚀")
        "medium"    -> Triple(0.50f, "Medium", "⚡")
        "low"       -> Triple(0.25f, "Low", "📉")
        else        -> Triple(0.05f, "Unknown", "❓")
    }

    val description = when (hypeLevel.lowercase()) {
        "very high" -> "Massive retail & institutional demand. Huge listing gains expected."
        "high"      -> "Strong market interest. Good listing gains likely."
        "medium"    -> "Average demand. Moderate listing gains expected."
        "low"       -> "Weak demand. Might list near offer price or at a discount."
        else        -> "Hype data not available yet."
    }

    // Gradient colors: Red -> Yellow -> Green
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF3D00), // Red
            Color(0xFFFF9100), // Orange  
            Color(0xFFFFD600), // Yellow
            Color(0xFF76FF03), // Light Green
            Color(0xFF00E676)  // Neon Green
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📈 Hype Meter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$emoji $label",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = when (hypeLevel.lowercase()) {
                        "very high" -> Color(0xFF00E676)
                        "high"      -> Color(0xFF76FF03)
                        "medium"    -> Color(0xFFFFD600)
                        "low"       -> Color(0xFFFF9100)
                        else        -> Color.Gray
                    }
                )
            }

            // Gradient Bar: Empty track + colored fill
            val fillColor = when {
                progress <= 0.25f -> Color(0xFFFF3D00) // Red
                progress <= 0.50f -> Color(0xFFFF9100) // Orange
                progress <= 0.75f -> Color(0xFFFFD600) // Yellow
                else              -> Color(0xFF00E676) // Green
            }
            
            Box(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                // Empty Track
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )
                // Gradient Filled Portion
                if (progress > 0f) {
                    val fillBrush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF3D00), // Red at start
                            fillColor          // current level color at end
                        ),
                        startX = 0f,
                        endX = Float.POSITIVE_INFINITY
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(
                                topStart = 10.dp, bottomStart = 10.dp,
                                topEnd = if (progress >= 0.99f) 10.dp else 4.dp,
                                bottomEnd = if (progress >= 0.99f) 10.dp else 4.dp
                            ))
                            .background(fillBrush)
                    )
                    // White Dot at end of fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterEnd)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Avoid 📉", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF3D00))
                Text("Must Apply 🔥", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E676))
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
