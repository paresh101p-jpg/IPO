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
import androidx.compose.ui.graphics.Color
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
    var selectedType by remember { mutableStateOf("All") } // "All", "Mainboard", "SME"
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
            // Main Status Tabs
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

            // Sub-filter Chips (Mainboard / SME)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Mainboard", "SME").forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if(isSelected) FilterChipDefaults.filterChipBorder(borderColor = MaterialTheme.colorScheme.primary) else FilterChipDefaults.filterChipBorder(borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
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
                    val filteredIpos = state.ipos.filter { ipo ->
                        val statusMatch = when(selectedTab) {
                            0 -> ipo.status.equals("Open", ignoreCase = true)
                            1 -> ipo.status.equals("Upcoming", ignoreCase = true)
                            else -> ipo.status.equals("Closed", ignoreCase = true) || ipo.status.equals("Listed", ignoreCase = true)
                        }
                        val typeMatch = when(selectedType) {
                            "All" -> true
                            else -> ipo.type.equals(selectedType, ignoreCase = true)
                        }
                        statusMatch && typeMatch
                    }
                    
                    if (filteredIpos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "▲",
                                    fontSize = 100.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                                )
                                Text(
                                    text = "No Data",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
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
            val isDateTba = ipo.openDate.isNullOrBlank() || ipo.openDate.equals("TBA", ignoreCase = true) || ipo.openDate == "-"
            if (isDateTba) {
                "📌 To Be Announced"
            } else {
                val openDate = parseDate(ipo.openDate)
                val diff = if (openDate != null) openDate.time - now else -1L
                val dateRange = "${ipo.openDate} → ${ipo.closeDate}"
                if (diff > 0) {
                    val days = diff / (1000 * 60 * 60 * 24)
                    if (days >= 1) "📌 $dateRange | ⏰ ${days}d mein khulega" else "📌 $dateRange | 🔥 Aaj khul raha hai!"
                } else "📌 $dateRange"
            }
        }
        "open" -> {
            val closeDate = parseDate(ipo.closeDate)
            // Add 23 hours and 59 minutes to treat close date as the end of the day
            val closeTimeEndOfDay = closeDate?.let { it.time + (24 * 60 * 60 * 1000) - 1 } ?: -1L
            val diff = closeTimeEndOfDay - now
            if (diff > 0) {
                val days = diff / (1000 * 60 * 60 * 24)
                val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                if (days >= 1) "⏳ Closes in: ${days}d ${hours}h" else "⚠️ Sirf aaj ${hours}h baki!"
            } else {
                "✅ Subscription Closed"
            }
        }
        else -> "✅ Closed | 🚀 Listing: ${ipo.listingDate ?: "TBD"}"
    }
}

@Composable
fun IpoCard(ipo: IpoData, onClick: () -> Unit) {
    val priceStr = ipo.offerPrice ?: "0"
    val priceVal = priceStr.filter { it.isDigit() }.toIntOrNull() ?: 0
    val lotVal = ipo.lotSize?.filter { it.isDigit() }?.toIntOrNull() ?: 0
    val totalAmount = if (priceVal > 0 && lotVal > 0) "₹${priceVal * lotVal}" else "TBD"
    val countdownText = SmartIpoBadge(ipo = ipo)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Logo or Initial
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ipo.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = ipo.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    // Type Tag (Mainboard / SME)
                    Surface(
                        color = if(ipo.type == "SME") Color(0xFFFF9100).copy(alpha = 0.1f) else Color(0xFF00E676).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = ipo.type ?: "Mainboard",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if(ipo.type == "SME") Color(0xFFFF9100) else Color(0xFF00E676)
                        )
                    }
                }
                Badge(containerColor = if (ipo.status.equals("Open", true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary) {
                    Text(ipo.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Price & Shares", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val isPriceTba = ipo.offerPrice.isNullOrBlank() || ipo.offerPrice.equals("TBA", ignoreCase = true) || ipo.offerPrice == "-"
                    if (isPriceTba) {
                        Text("To Be Announced", style = MaterialTheme.typography.bodyMedium, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("${ipo.offerPrice} x ${ipo.lotSize ?: "?"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(totalAmount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("GMP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (ipo.gmp == "TBA") "To Be Announced" else ipo.gmp,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = if (ipo.gmp == "TBA") 10.sp else MaterialTheme.typography.bodyMedium.fontSize,
                        color = AccentSecondary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = countdownText,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HypeMeter(hypeLevel = ipo.hype_meter ?: "Medium")
        }
    }
}
