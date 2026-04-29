package com.IPO.Tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HypeMeter(hypeLevel: String) {
    val (color, progress) = when (hypeLevel.lowercase()) {
        "very high" -> Color(0xFFE91E63) to 0.9f
        "high" -> Color(0xFFFF5722) to 0.7f
        "medium" -> Color(0xFFFFC107) to 0.5f
        "low" -> Color(0xFF4CAF50) to 0.2f
        else -> Color.Gray to 0.0f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔥 Hype Meter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = hypeLevel, color = color, fontWeight = FontWeight.Bold)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(color)
                )
            }
        }
    }
}
