@file:OptIn(ExperimentalMaterial3Api::class)
package com.IPO.Tracker.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.IPO.Tracker.R
import com.IPO.Tracker.data.DematStore
import com.IPO.Tracker.data.NotificationPreferencesStore
import com.IPO.Tracker.data.PaperTradeStore
import com.IPO.Tracker.ui.theme.AccentSecondary
import com.IPO.Tracker.ui.theme.AccentTertiary
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import android.widget.Toast
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AccountScreen(viewModel: com.IPO.Tracker.viewmodel.IpoViewModel, onPolicyClick: () -> Unit = {}) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val uiState by viewModel.uiState.collectAsState()
    val currentIpos = (uiState as? com.IPO.Tracker.viewmodel.IpoUiState.Success)?.ipos.orEmpty()
    val paperTradeSummary = PaperTradeStore.getSummary(context, currentIpos)
    val paperTradeDetails = PaperTradeStore.getDetails(context, currentIpos)
    var showPaperTradeHistory by remember { mutableStateOf(false) }
    val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    var currentUser by remember { mutableStateOf(auth?.currentUser) }
    var isLoading by remember { mutableStateOf(false) }
    var showAddPanDialog by remember { mutableStateOf(false) }
    var newPanName by remember { mutableStateOf("") }
    var newPanNumber by remember { mutableStateOf("") }
    var newDpId by remember { mutableStateOf("") }
    var newClientId by remember { mutableStateOf("") }
    var newUpiId by remember { mutableStateOf("") }
    var savedAccounts by remember { mutableStateOf(DematStore.getAccounts(context)) }
    var listingAlert by remember { mutableStateOf(NotificationPreferencesStore.isListingAlertEnabled(context)) }
    var allotmentAlert by remember { mutableStateOf(NotificationPreferencesStore.isAllotmentAlertEnabled(context)) }
    var gmpAlert by remember { mutableStateOf(NotificationPreferencesStore.isGmpAlertEnabled(context)) }
    var newsAlert by remember { mutableStateOf(NotificationPreferencesStore.isNewsAlertEnabled(context)) }
    val coroutineScope = rememberCoroutineScope()

    // Google Sign-In Setup
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth?.signInWithCredential(credential)?.addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            currentUser = auth.currentUser
                        }
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Login Error: ${e.message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }
        } else {
            Toast.makeText(context, "Login Cancelled or Failed", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (currentUser != null) {
                            if (currentUser?.photoUrl != null) {
                                AsyncImage(
                                    model = currentUser?.photoUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(80.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (currentUser != null) {
                            Text("${currentUser?.displayName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${currentUser?.email}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth().clickable { showPaperTradeHistory = true },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Paper Trades", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${paperTradeSummary.selectedCount} selected",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = com.IPO.Tracker.ui.theme.AccentSecondary,
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                                )
                                Text(
                                    "${paperTradeSummary.wonCount} Won • ${paperTradeSummary.lossCount} Loss • ${paperTradeSummary.openCount} Open",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    auth?.signOut()
                                    googleSignInClient.signOut()
                                    currentUser = null
                                }, 
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Logout")
                            }
                        } else {
                            Text("Welcome to IPO Tracker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Button(onClick = { 
                                    isLoading = true
                                    launcher.launch(googleSignInClient.signInIntent)
                                }, modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentSecondary)) {
                                    Text("Sign In With Google", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Family Demat / PAN Manager
            if (currentUser != null) {
                item {
                    Text("Family Demat & PAN Manager", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (savedAccounts.isEmpty()) {
                                Text("No saved PAN accounts yet.", fontWeight = FontWeight.Bold)
                                Text(
                                    "Add your family Demat / PAN details to enable auto-check allotment and reminder support.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                savedAccounts.forEach { account ->
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(account.name, fontWeight = FontWeight.Bold)
                                                Text("PAN: ${account.panNumber}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                                Text(
                                                    "DP: ${account.dpId} • Client: ${account.clientId}",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            IconButton(onClick = {
                                                DematStore.removeAccount(context, account.id)
                                                savedAccounts = DematStore.getAccounts(context)
                                                Toast.makeText(context, "Removed ${account.name}", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remove account")
                                            }
                                        }
                                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showAddPanDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Family Demat/PAN")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (savedAccounts.isEmpty()) {
                                        Toast.makeText(context, "Add PAN accounts first to enable auto-check allotment.", Toast.LENGTH_LONG).show()
                                    } else {
                                        val availableLinks = currentIpos.count { !it.allotmentLink.isNullOrBlank() }
                                        Toast.makeText(
                                            context,
                                            if (availableLinks > 0) "Auto-check prepared for ${savedAccounts.size} saved PANs and $availableLinks IPO allotment links. Open IPO details to enable alerts."
                                            else "No IPO allotment links available yet. Please try again when allotment pages are published.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = com.IPO.Tracker.ui.theme.AccentSecondary)
                            ) {
                                Text("Auto-Check Allotment For All Saved PANs", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Notifications Section
            item {
                Text("Notification Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Listing Day Alerts", fontWeight = FontWeight.Medium)
                                Text("Receive a notification when an IPO listing date is updated.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = listingAlert, onCheckedChange = {
                                listingAlert = it
                                NotificationPreferencesStore.setListingAlertEnabled(context, it)
                            })
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allotment Status Alerts", fontWeight = FontWeight.Medium)
                                Text("Get alerts when allotment status changes for your IPOs.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = allotmentAlert, onCheckedChange = {
                                allotmentAlert = it
                                NotificationPreferencesStore.setAllotmentAlertEnabled(context, it)
                            })
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("GMP Change Alerts", fontWeight = FontWeight.Medium)
                                Text("Alert when GMP moves for IPOs you follow.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = gmpAlert, onCheckedChange = {
                                gmpAlert = it
                                NotificationPreferencesStore.setGmpAlertEnabled(context, it)
                            })
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("IPO News Alerts", fontWeight = FontWeight.Medium)
                                Text("Receive news notifications for IPO names you follow.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = newsAlert, onCheckedChange = {
                                newsAlert = it
                                NotificationPreferencesStore.setNewsAlertEnabled(context, it)
                            })
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("For per-IPO alerts, turn on notifications on the IPO detail page.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Legal & Policy Section
            item {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onPolicyClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📜 Legal, Privacy Policy & Disclaimer", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "App Version 1.0.0 | Built in India 🇮🇳",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text("Support Us", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Love using IPO Tracker?", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Help us grow by giving a 5-star rating on the Play Store!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                val manager = ReviewManagerFactory.create(context)
                                val request = manager.requestReviewFlow()
                                request.addOnCompleteListener { task ->
                                    if (task.isSuccessful && activity != null) {
                                        val reviewInfo = task.result
                                        val flow = manager.launchReviewFlow(activity, reviewInfo)
                                        flow.addOnCompleteListener { _ ->
                                            // Review flow complete
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTertiary, contentColor = MaterialTheme.colorScheme.onBackground)
                        ) {
                            Text("⭐ Rate Us on Play Store", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddPanDialog) {
        AlertDialog(
            onDismissRequest = { showAddPanDialog = false },
            title = { Text("Add Demat / PAN") },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newPanName,
                        onValueChange = { newPanName = it },
                        label = { Text("Account name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPanNumber,
                        onValueChange = { newPanNumber = it },
                        label = { Text("PAN number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDpId,
                        onValueChange = { newDpId = it },
                        label = { Text("DP ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newClientId,
                        onValueChange = { newClientId = it },
                        label = { Text("Client ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUpiId,
                        onValueChange = { newUpiId = it },
                        label = { Text("UPI ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPanName.isNotBlank() && newPanNumber.isNotBlank()) {
                        DematStore.addAccount(context, DematStore.createAccount(newPanName, newPanNumber, newDpId, newClientId, newUpiId))
                        savedAccounts = DematStore.getAccounts(context)
                        newPanName = ""
                        newPanNumber = ""
                        newDpId = ""
                        newClientId = ""
                        newUpiId = ""
                        showAddPanDialog = false
                        Toast.makeText(context, "PAN account added.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter account name and PAN number.", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPaperTradeHistory) {
        AlertDialog(
            onDismissRequest = { showPaperTradeHistory = false },
            title = { Text("Paper Trade History") },
            text = {
                if (paperTradeDetails.isEmpty()) {
                    Text("You have not added any IPO to Paper Trades yet.")
                } else {
                    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        paperTradeDetails.forEach { detail ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(detail.name, fontWeight = FontWeight.Bold)
                                Text("Status: ${detail.status} | Result: ${detail.result}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Added: ${java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(java.util.Date(detail.addedAt))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaperTradeHistory = false }) {
                    Text("Close")
                }
            }
        )
    }
}
