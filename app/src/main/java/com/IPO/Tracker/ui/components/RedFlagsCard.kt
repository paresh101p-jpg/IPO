package com.IPO.Tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RedFlagsCard(redFlags: List<String>) {
    val hasRedFlags = redFlags.isNotEmpty() && !redFlags.contains("None")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🚩 DRHP Red Flags", 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.titleMedium,
                color = if (hasRedFlags) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
            )
            
            if (hasRedFlags) {
                redFlags.forEach { flag ->
                    Text(text = "• $flag", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text(text = "✅ No major red flags found in DRHP.", color = Color(0xFF388E3C))
            }
        }
    }
}
