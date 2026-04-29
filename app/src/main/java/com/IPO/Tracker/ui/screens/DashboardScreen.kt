@file:OptIn(ExperimentalMaterial3Api::class)
package com.IPO.Tracker.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.IPO.Tracker.model.IpoData
import com.IPO.Tracker.ui.components.HypeMeter
import com.IPO.Tracker.ui.theme.AccentSecondary
import com.IPO.Tracker.ui.theme.AccentTertiary
import com.IPO.Tracker.viewmodel.IpoUiState
import com.IPO.Tracker.viewmodel.IpoViewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: IpoViewModel, onIpoClick: (IpoData) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Open", "Upcoming", "Closed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "▲",
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "IPO Tracker",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize
                            )
                            Text(
                                "India's Investment Companion",
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                title, 
                                fontWeight = if(isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = if(isSelected) MaterialTheme.typography.titleMedium.fontSize else MaterialTheme.typography.titleSmall.fontSize
                            ) 
                        }
                    )
                }
            }
            
            when (val state = uiState) {
                is IpoUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is IpoUiState.Success -> {
                    val filteredIpos = state.ipos.filter { 
                        when(selectedTab) {
                            0 -> it.status.equals("Open", ignoreCase = true)
                            1 -> it.status.equals("Upcoming", ignoreCase = true)
                            else -> it.status.equals("Closed", ignoreCase = true) || it.status.equals("Listed", ignoreCase = true)
                        }
                    }
                    
                    if (filteredIpos.isEmpty()) {
                        // Empty state with watermark
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "▲",
                                    fontSize = 100.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                                )
                                Text(
                                    text = "IPO",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No IPOs in this category right now.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredIpos) { ipo ->
                                IpoCard(ipo, onClick = { onIpoClick(ipo) })
                            }
                            // Watermark footer
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp, bottom = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "▲",
                                            fontSize = 48.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                        )
                                        Text(
                                            text = "IPO Tracker",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 4.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is IpoUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun SmartIpoBadge(ipo: IpoData): String {
    var badge by remember { mutableStateOf("") }

    LaunchedEffect(ipo.id) {
        while (true) {
            badge = computeBadge(ipo)
            delay(1000)
        }
    }
    return badge
}

fun parseDate(dateStr: String?): Date? {
    if (dateStr.isNullOrEmpty()) return null
    val formats = listOf(
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
    )
    for (fmt in formats) {
        try { val d = fmt.parse(dateStr); if (d != null) return d } catch (e: Exception) { }
    }
    return null
}

fun computeBadge(ipo: IpoData): String {
    val now = System.currentTimeMillis()
    return when (ipo.status.lowercase()) {
        "upcoming" -> {
            // Show date range: 22 May - 27 May 2026 | X days baki
            val openDate = parseDate(ipo.openDate)
            val diff = if (openDate != null) openDate.time - now else -1L
            val dateRange = "${ipo.openDate ?: "?"} → ${ipo.closeDate ?: "?"}"
            if (diff > 0) {
                val days = diff / (1000 * 60 * 60 * 24)
                val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                val mins = (diff % (1000 * 60 * 60)) / (1000 * 60)
                when {
                    days >= 3 -> "📌 $dateRange | 🟡 ${days}d mein khulegaa"
                    days >= 1 -> "📌 $dateRange | ⏰ ${days}d ${hours}h mein khulegaa!"
                    hours >= 1 -> "📌 $dateRange | ⚡ Sirf ${hours}h ${mins}m baad!"
                    else -> "📌 $dateRange | 🔥 Aaj khul raha hai!"
                }
            } else {
                "📌 $dateRange"
            }
        }
        "open" -> {
            // Show countdown to close
            val closeDate = parseDate(ipo.closeDate)
            val diff = if (closeDate != null) closeDate.time - now else -1L
            if (diff > 0) {
                val days = diff / (1000 * 60 * 60 * 24)
                val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                val mins = (diff % (1000 * 60 * 60)) / (1000 * 60)
                when {
                    days >= 1 -> "⏳ Closes in: ${days}d ${hours}h ${mins}m"
                    hours >= 1 -> "⚠️ Sirf ${hours}h ${mins}m baki!"
                    else -> "🔴 Aakhri ${mins} minute! Jaldi apply karo!"
                }
            } else {
                "✅ Subscription Closed"
            }
        }
        else -> {
            // Closed - just show listing date
            "✅ Closed | 🚀 Listing: ${ipo.listingDate ?: "TBD"}"
        }
    }
}

@Composable
fun IpoCard(ipo: IpoData, onClick: () -> Unit) {
    // Calculations for the new Card details
    val priceStr = ipo.offerPrice ?: "0"
    val priceVal = priceStr.filter { it.isDigit() }.toIntOrNull() ?: 0
    val lotVal = ipo.lotSize?.filter { it.isDigit() }?.toIntOrNull() ?: 0
    val totalAmount = if (priceVal > 0 && lotVal > 0) "₹${priceVal * lotVal}" else "TBD"
    
    // Smart Badge
    val countdownText = SmartIpoBadge(ipo = ipo)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row (Logo, Name, Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!ipo.logoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ipo.logoUrl,
                        contentDescription = "Logo",
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ipo.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ipo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = AccentTertiary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${ipo.averageRating} (${ipo.totalRatingsCount})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Badge(
                    containerColor = if (ipo.status.equals("Open", true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ) {
                    Text(ipo.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            // Financial Data Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Price & Shares", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${ipo.offerPrice ?: "TBD"} x ${ipo.lotSize ?: "?"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(totalAmount, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("GMP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(ipo.gmp, style = MaterialTheme.typography.bodyMedium, color = AccentSecondary, fontWeight = FontWeight.ExtraBold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time Badge Row
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.Center) {
                    Text(countdownText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HypeMeter(hypeLevel = ipo.hype_meter)
        }
    }
}
