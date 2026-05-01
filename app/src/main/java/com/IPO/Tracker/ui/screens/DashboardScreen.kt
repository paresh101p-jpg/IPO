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
import com.IPO.Tracker.util.*


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
                        val status = inferIpoStatus(ipo).normalizeString()
                        val type = ipo.type.normalizeString()
                        val statusMatch = when(selectedTab) {
                            0 -> status == "open"
                            1 -> status == "upcoming"
                            else -> status == "closed"
                        }
                        val typeMatch = when(selectedType.normalizeString()) {
                            "all" -> true
                            else -> type == selectedType.normalizeString()
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


@Composable
fun IpoCard(ipo: IpoData, onClick: () -> Unit) {
    val priceStr = ipo.offerPrice ?: "0"
    val totalAmount = formatTotalAmount(priceStr, ipo.lotSize)
    val countdownText = SmartIpoBadge(ipo = ipo)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(
            2.dp,
            when {
                inferIpoStatus(ipo).equals("Open", ignoreCase = true) -> MaterialTheme.colorScheme.primary
                inferIpoStatus(ipo).equals("Upcoming", ignoreCase = true) -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            }
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (!ipo.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ipo.logoUrl,
                        contentDescription = "${ipo.name} logo",
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ipo.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
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
                val effectiveStatus = inferIpoStatus(ipo)
                val isDateTba = ipo.openDate.isNullOrBlank() || ipo.openDate.equals("TBA", ignoreCase = true) || ipo.openDate == "-"
                val displayStatus = if (effectiveStatus.equals("Upcoming", ignoreCase = true) && isDateTba) "To Be Announced" else effectiveStatus

                Badge(containerColor = when {
                    effectiveStatus.equals("Open", true) -> MaterialTheme.colorScheme.primary
                    effectiveStatus.equals("Upcoming", true) -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                }) {
                    Text(displayStatus, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Offer Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(ipo.offerPrice ?: "TBD", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lot Size", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(ipo.lotSize ?: "?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(totalAmount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("GMP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(ipo.gmp.ifBlank { "TBA" }, style = MaterialTheme.typography.bodyMedium, color = AccentSecondary, fontWeight = FontWeight.ExtraBold)
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
