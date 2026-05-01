package com.IPO.Tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.IPO.Tracker.model.PeerData
import com.IPO.Tracker.ui.theme.AccentDanger
import com.IPO.Tracker.ui.theme.AccentSecondary
import com.IPO.Tracker.util.*

@Composable
fun WhaleAlertBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AccentSecondary.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = "Alert", tint = AccentSecondary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("The Whale Watcher 🐳", fontWeight = FontWeight.ExtraBold, color = AccentSecondary)
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetProfitCalculator(gmp: String, lotSizeStr: String?) {
    // Better extraction using Utils
    val gmpValue = parseFirstFloat(gmp)
    val lotSize = extractNumbers(lotSizeStr).firstOrNull() ?: 0
    
    val grossProfit = (gmpValue * lotSize).toInt()
    
    // Broker List
    val brokers = listOf(
        Pair("Zerodha / Groww / Angel", 20),
        Pair("Kotak / Shoonya (Free)", 0),
        Pair("HDFC Sky / Upstox", 30),
        Pair("ICICI Direct / SBI", 50)
    )
    
    var expanded by remember { mutableStateOf(false) }
    var selectedBroker by remember { mutableStateOf(brokers[0]) }
    
    val brokerage = selectedBroker.second
    val tax = (grossProfit * 0.15).toInt() // 15% STCG
    val netProfit = grossProfit - brokerage - tax

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Real Net Profit Calculator 💵", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Dropdown for Broker
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedBroker.first,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Your Broker") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    brokers.forEach { broker ->
                        DropdownMenuItem(
                            text = { Text(broker.first) },
                            onClick = {
                                selectedBroker = broker
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Expected Gross Profit ($gmpValue x $lotSize)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹$grossProfit", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Brokerage & Sell DP Cost", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if(brokerage == 0) "FREE" else "-₹$brokerage", color = if(brokerage == 0) AccentSecondary else AccentDanger, fontWeight = if(brokerage == 0) FontWeight.Bold else FontWeight.Normal)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("STCG Tax (15%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("-₹$tax", color = AccentDanger)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Net Profit in Hand", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("₹${if(netProfit > 0) netProfit else 0}", fontWeight = FontWeight.ExtraBold, color = AccentSecondary, fontSize = MaterialTheme.typography.titleLarge.fontSize)
            }
        }
    }
}

@Composable
fun GmpTrendChart(gmpTrend: List<Float>) {
    if (gmpTrend.size < 2) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("GMP Trend (Last 5 Days) 📈", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            val isGoingDown = gmpTrend.last() < gmpTrend.first()
            val lineColor = if (isGoingDown) AccentDanger else AccentSecondary

            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp).padding(8.dp)) {
                val maxVal = gmpTrend.maxOrNull() ?: 1f
                val minVal = gmpTrend.minOrNull() ?: 0f
                val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal
                val xStep = size.width / (gmpTrend.size - 1)
                
                val path = Path()
                gmpTrend.forEachIndexed { index, value ->
                    val x = index * xStep
                    val y = size.height - ((value - minVal) / range * size.height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

@Composable
fun AllotmentPredictor(subscriptionStr: String?) {
    val sub = subscriptionStr?.replace("x", "")?.toFloatOrNull() ?: 0f
    val probability = if (sub <= 0f) 100f else (1f / sub) * 100f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Smart Allotment Predictor 🧮", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Based on current retail subscription ($subscriptionStr), your chances of getting 1 lot are:", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            
            val probColor = if (probability > 50) AccentSecondary else if (probability > 10) MaterialTheme.colorScheme.tertiary else AccentDanger
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = (probability / 100f).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = probColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(String.format("%.1f%%", probability.coerceAtMost(100f)), fontWeight = FontWeight.ExtraBold, color = probColor, fontSize = MaterialTheme.typography.titleLarge.fontSize)
            }
        }
    }
}

@Composable
fun RiskMeter(redFlags: List<String>) {
    val riskLevel = when {
        redFlags.isEmpty() -> "Safe ✅"
        redFlags.size == 1 -> "Moderate ⚠️"
        else -> "High Risk 🚨"
    }
    val riskColor = when {
        redFlags.isEmpty() -> AccentSecondary
        redFlags.size == 1 -> MaterialTheme.colorScheme.tertiary
        else -> AccentDanger
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("SME Risk Meter", fontWeight = FontWeight.Bold, color = riskColor)
                if (redFlags.isNotEmpty()) {
                    Text(redFlags.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Text(riskLevel, fontWeight = FontWeight.ExtraBold, color = riskColor, fontSize = MaterialTheme.typography.titleMedium.fontSize)
        }
    }
}

@Composable
fun PeerComparisonTable(peers: List<PeerData>) {
    if (peers.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Peer Comparison ⚖️", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Company", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("P/E", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("M.Cap", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            peers.forEach { peer ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(peer.companyName, modifier = Modifier.weight(2f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(peer.peRatio, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(peer.marketCap, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RefundTracker() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Peace of Mind Refund Tracker 🏦", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Don't worry! 90% banks automatically unblock ASBA funds within 24 hours of allotment date.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = 0.9f,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = AccentSecondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
