package com.IPO.Tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.IPO.Tracker.model.IpoData
import com.IPO.Tracker.ui.components.HypeMeter
import com.IPO.Tracker.ui.components.RedFlagsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpoDetailScreen(ipo: IpoData, onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ipo.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Divider()
                    DetailRow("GMP", ipo.gmp, Color(0xFF388E3C))
                    DetailRow("Status", ipo.status)
                    DetailRow("Open Date", ipo.openDate ?: "N/A")
                    DetailRow("Close Date", ipo.closeDate ?: "N/A")
                    DetailRow("Price Band", ipo.priceBand ?: "N/A")
                    DetailRow("Subscription", ipo.subscription)
                    DetailRow("Est. Allotment Probability", ipo.allotment_prob, Color(0xFF1976D2))
                }
            }

            // AI/Unique Features Section
            Text("AI Analysis & Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            HypeMeter(hypeLevel = ipo.hype_meter)
            
            RedFlagsCard(redFlags = ipo.red_flags)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
