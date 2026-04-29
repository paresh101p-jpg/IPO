package com.IPO.Tracker.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.IPO.Tracker.model.IpoData
import com.IPO.Tracker.ui.components.HypeMeter
import com.IPO.Tracker.ui.theme.AccentSecondary
import com.IPO.Tracker.ui.theme.AccentTertiary
import com.IPO.Tracker.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpoDetailScreen(ipo: IpoData, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    var calendarMessage by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Permission launcher for calendar
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.WRITE_CALENDAR] == true) {
            insertCalendarEventSilently(context, ipo)
            calendarMessage = "✅ IPO dates added to your Google Calendar!"
        } else {
            calendarMessage = "⚠️ Permission denied. Please allow Calendar access."
        }
        showSnackbar = true
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(calendarMessage)
            showSnackbar = false
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(ipo.name, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Button(
                    onClick = { /* Open WebView Link */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentSecondary
                    )
                ) {
                    Text(
                        text = if (ipo.status.equals("Open", true)) "Apply Now" else "Check Allotment",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Offer Price", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                        Text(ipo.offerPrice ?: "TBD", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Grey Market Premium (GMP)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                        Text(ipo.gmp, fontWeight = FontWeight.ExtraBold, color = AccentSecondary, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { /* Show Graph */ },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View GMP Graph", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    }
                }
            }

            // Calendar Sync Button (Top)
            Button(
                onClick = {
                    if (isLoggedIn) {
                        // Check permission and insert silently
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.WRITE_CALENDAR
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            insertCalendarEventSilently(context, ipo)
                            calendarMessage = "✅ IPO dates added to your Google Calendar!"
                            showSnackbar = true
                        } else {
                            permLauncher.launch(arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                            ))
                        }
                    } else {
                        // Not logged in: open calendar app UI
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, "IPO: ${ipo.name}")
                            putExtra(CalendarContract.Events.DESCRIPTION, "Open: ${ipo.openDate} | Close: ${ipo.closeDate} | Listing: ${ipo.listingDate}")
                            putExtra(CalendarContract.Events.ALL_DAY, true)
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, System.currentTimeMillis())
                        }
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isLoggedIn) "Add to Calendar (Auto ✅)" else "Add IPO Dates to Calendar 📅",
                    fontWeight = FontWeight.Bold
                )
            }

            if (!ipo.whaleAlert.isNullOrEmpty()) {
                WhaleAlertBanner(ipo.whaleAlert)
            }

            NetProfitCalculator(gmp = ipo.gmp, lotSizeStr = ipo.lotSize)
            


            AllotmentPredictor(subscriptionStr = ipo.subscriptionText ?: ipo.subscriptionDetails?.totalApplications)
            
            RiskMeter(redFlags = ipo.red_flags)
            
            HypeMeter(hypeLevel = ipo.hype_meter)

            // Rating Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("User Ratings & Sentiment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(ipo.averageRating.toString(), fontSize = MaterialTheme.typography.headlineMedium.fontSize, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < ipo.averageRating.toInt()) AccentTertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Text("Based on ${ipo.totalRatingsCount} votes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { /* Rate IPO */ }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("Rate this IPO")
                    }
                }
            }

            // Details section
            SectionHeader("Subscription Details")
            InfoRow("Retail Portion", ipo.subscriptionDetails?.retailTotal ?: "N/A")
            InfoRow("Total Applications", ipo.subscriptionDetails?.totalApplications ?: ipo.subscriptionText ?: "N/A")
            
            SectionHeader("Company Financials")
            InfoRow("Revenue", ipo.financials?.revenue ?: "N/A")
            InfoRow("Profit After Tax", ipo.financials?.profit ?: "N/A")
            InfoRow("Total Debt", ipo.financials?.debt ?: "N/A")
            
            
            PeerComparisonTable(peers = ipo.peerComparison)
            
            SectionHeader("IPO Timeline")
            InfoRow("Open Date", ipo.openDate ?: "TBD")
            InfoRow("Close Date", ipo.closeDate ?: "TBD")
            InfoRow("Listing Date", ipo.listingDate ?: "TBD")

            // Anchor Investor Spy
            if (!ipo.anchorInvestors.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, AccentTertiary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🕵️‍♂️ Anchor Investor Spy", fontWeight = FontWeight.ExtraBold, color = AccentTertiary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(ipo.anchorInvestors, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            
            RefundTracker()

            // Company Profile Section
            SectionHeader("🏢 Company Profile")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!ipo.aboutCompany.isNullOrEmpty()) {
                        Text("About Company", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(ipo.aboutCompany, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    }
                    if (!ipo.promoters.isNullOrEmpty()) {
                        InfoRow("👤 Promoters", ipo.promoters)
                    }
                    if (!ipo.categoryReservation.isNullOrEmpty()) {
                        InfoRow("🏦 Category Reservation", ipo.categoryReservation)
                    }
                    if (!ipo.issueObjective.isNullOrEmpty()) {
                        Text("🎯 Issue Objective", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(ipo.issueObjective, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Issue Details
            SectionHeader("📊 Issue Details")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow("💰 Issue Size", ipo.issueSize ?: "TBD")
                    InfoRow("📆 Lot Size", "${ipo.lotSize ?: "?"} Shares")
                    InfoRow("🏷️ Price Band", ipo.offerPrice ?: "TBD")
                    InfoRow("💼 Min Investment", ipo.retailLotsAllowed ?: "TBD")
                    if (!ipo.registrarDetails.isNullOrEmpty()) {
                        InfoRow("🏗️ Registrar", ipo.registrarDetails)
                    }
                    if (!ipo.contactDetails.isNullOrEmpty()) {
                        InfoRow("📧 Contact", ipo.contactDetails)
                    }
                }
            }

            // Post Listing Data (if listed)
            if (!ipo.listingPrice.isNullOrEmpty()) {
                SectionHeader("📈 Post Listing Performance")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AccentSecondary.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentSecondary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoRow("🚀 Listing Price", ipo.listingPrice ?: "N/A")
                        InfoRow("⏳ Current Price", ipo.currentPrice ?: "N/A")
                        InfoRow("⬆️ 52-Week High", ipo.fiftyTwoWeekHigh ?: "N/A")
                        InfoRow("⬇️ 52-Week Low", ipo.fiftyTwoWeekLow ?: "N/A")
                        InfoRow("📊 Valuations", ipo.valuations ?: "N/A")
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = MaterialTheme.typography.bodyLarge.fontSize)
        Text(text = value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = MaterialTheme.typography.bodyLarge.fontSize)
    }
}

fun insertCalendarEventSilently(context: android.content.Context, ipo: IpoData) {
    try {
        val cr = context.contentResolver
        val fmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        fmt.timeZone = TimeZone.getDefault()

        // Find the primary Google Calendar account ID
        val calCursor = cr.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME),
            "${CalendarContract.Calendars.IS_PRIMARY} = 1",
            null, null
        )
        var calId = 1L
        calCursor?.use { if (it.moveToFirst()) calId = it.getLong(0) }

        val closeTime = try { fmt.parse(ipo.closeDate ?: "")?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
        val listingTime = try { fmt.parse(ipo.listingDate ?: "")?.time ?: (closeTime + 86400000L * 3) } catch (e: Exception) { closeTime + 86400000L * 3 }

        // Insert Close Date event
        val closeEvent = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.TITLE, "🔴 IPO Closes: ${ipo.name}")
            put(CalendarContract.Events.DESCRIPTION, "Last day to apply for ${ipo.name} IPO.\nPrice: ${ipo.offerPrice}")
            put(CalendarContract.Events.DTSTART, closeTime)
            put(CalendarContract.Events.DTEND, closeTime + 86400000L)
            put(CalendarContract.Events.ALL_DAY, 1)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.HAS_ALARM, 1)
        }
        val closeUri = cr.insert(CalendarContract.Events.CONTENT_URI, closeEvent)

        // Add alarm 1 day before close
        closeUri?.lastPathSegment?.let { eventId ->
            val reminder = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId.toLong())
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                put(CalendarContract.Reminders.MINUTES, 60 * 24) // 1 day before
            }
            cr.insert(CalendarContract.Reminders.CONTENT_URI, reminder)
        }

        // Insert Listing Date event
        val listingEvent = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.TITLE, "🚀 IPO Listing: ${ipo.name}")
            put(CalendarContract.Events.DESCRIPTION, "Listing day for ${ipo.name}. Expected GMP: ${ipo.gmp}")
            put(CalendarContract.Events.DTSTART, listingTime)
            put(CalendarContract.Events.DTEND, listingTime + 86400000L)
            put(CalendarContract.Events.ALL_DAY, 1)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.HAS_ALARM, 1)
        }
        cr.insert(CalendarContract.Events.CONTENT_URI, listingEvent)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
