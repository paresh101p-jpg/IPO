package com.IPO.Tracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.IPO.Tracker.model.IpoData
import com.IPO.Tracker.viewmodel.IpoUiState
import com.IPO.Tracker.viewmodel.IpoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: IpoViewModel = viewModel(),
    onIpoClick: (IpoData) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Next-Gen IPO Tracker", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (uiState) {
                is IpoUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is IpoUiState.Success -> {
                    val ipos = (uiState as IpoUiState.Success).ipos
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ipos) { ipo ->
                            IpoCard(ipo = ipo, onClick = { onIpoClick(ipo) })
                        }
                    }
                }
                is IpoUiState.Error -> {
                    val message = (uiState as IpoUiState.Error).message
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: $message", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchIpos() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IpoCard(ipo: IpoData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ipo.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (ipo.status == "Upcoming") Color(0xFFE3F2FD) else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = ipo.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (ipo.status == "Upcoming") Color(0xFF1976D2) else Color(0xFF388E3C)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("GMP", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(ipo.gmp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Est. Allotment", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(ipo.allotment_prob, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
