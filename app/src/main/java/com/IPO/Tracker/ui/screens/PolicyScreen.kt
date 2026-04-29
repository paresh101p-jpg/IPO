@file:OptIn(ExperimentalMaterial3Api::class)
package com.IPO.Tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.IPO.Tracker.ui.theme.AccentDanger
import com.IPO.Tracker.ui.theme.AccentPrimary
import com.IPO.Tracker.ui.theme.AccentSecondary
import com.IPO.Tracker.ui.theme.AccentTertiary

@Composable
fun PolicyScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Legal & Policies", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ───── IMPORTANT DISCLAIMER ─────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AccentDanger.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️ Important Disclaimer", fontWeight = FontWeight.ExtraBold, color = AccentDanger, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        """IPO Tracker is a FREE informational and educational tool ONLY.

• This app does NOT provide any financial, investment, trading, or legal advice.
• Nothing in this app should be considered as a recommendation to BUY, SELL, or HOLD any security.
• Grey Market Premium (GMP), Hype Meter, and Allotment Predictor are ESTIMATES based on publicly available unofficial data and community sentiment. They are NOT guaranteed predictions.
• Past GMP performance does NOT guarantee future listing gains or profits.
• All investment decisions are solely the USER's responsibility.
• Investments in IPOs are subject to market risks. Please read all DRHP / offer documents carefully before investing.

Always consult a SEBI-registered financial advisor before making any investment decision.""",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }

            // ───── PRIVACY POLICY ─────
            PolicySection(
                icon = "🔒",
                title = "Privacy Policy",
                color = AccentPrimary,
                content = """Last Updated: April 2026

1. INFORMATION WE COLLECT
   We collect only what is necessary to provide core features:
   • Google Account (Name, Email, Profile Photo) — only when you voluntarily sign in with Google.
   • Saved PAN Numbers — stored ONLY on your local device. We do NOT store PAN numbers on any server.
   • Calendar Events — added to your phone's Google Calendar with your permission.
   • Device Crash Logs — via Firebase Analytics (anonymous, no personal data).

2. INFORMATION WE DO NOT COLLECT
   • We do NOT collect your Demat account details, bank account numbers, or trading history.
   • We do NOT collect your AADHAAR, financial statements, or any sensitive financial documents.
   • We do NOT sell, rent, or share your personal data with any third party for marketing purposes.

3. HOW WE USE YOUR INFORMATION
   • To display your Google profile in the app.
   • To improve app stability via anonymous crash reports.
   • To add IPO reminders to your calendar (only with your explicit permission).

4. DATA SECURITY
   • Google Authentication is handled entirely by Google Firebase — one of the world's most secure authentication platforms.
   • PAN numbers are stored on your device's local storage only.
   • We use HTTPS for all network communication.

5. THIRD-PARTY SERVICES
   • Google Firebase (Authentication & Analytics) — subject to Google's Privacy Policy.
   • GitHub (for serving IPO data JSON) — public data only.

6. CHILDREN'S PRIVACY
   This app is not intended for users under 18 years of age.

7. CONTACT US
   For any privacy concerns: ipotrackerin@gmail.com"""
            )

            // ───── DATA SOURCES ─────
            PolicySection(
                icon = "📡",
                title = "Data Sources & Accuracy",
                color = AccentTertiary,
                content = """Our app displays IPO data from the following sources:

• Subscription Data: BSE India (bseindia.com) and NSE India (nseindia.com) — Official public data.
• Grey Market Premium (GMP): Community forums and public investor discussion groups. GMP is an UNOFFICIAL, UNREGULATED estimate and is NOT verified by any regulatory authority.
• Company Financials: Sourced from DRHP documents filed with SEBI — publicly available.
• Allotment Links: Official Registrar websites (Link Intime, KFintech, Bigshare).
• News & Analysis: Publicly available news sources and broker reports.

ACCURACY DISCLAIMER:
While we strive to keep data accurate and up-to-date, we cannot guarantee 100% accuracy or real-time updates. Always verify critical information from official SEBI / BSE / NSE / Company websites before making any investment decision."""
            )

            // ───── TERMS OF USE ─────
            PolicySection(
                icon = "📜",
                title = "Terms of Use",
                color = AccentSecondary,
                content = """By using IPO Tracker, you agree to the following terms:

1. INFORMATIONAL PURPOSE ONLY
   This app provides general information about IPOs for educational purposes only. It is not a brokerage, advisory, or trading platform.

2. NO FINANCIAL ADVICE
   The app's features (GMP, Hype Meter, Predictor, Profit Calculator) are analytical tools based on community data. They are NOT financial advice.

3. USER RESPONSIBILITY
   Users are solely responsible for their own investment decisions. The app developers are NOT liable for any financial loss arising from use of this app.

4. INTELLECTUAL PROPERTY
   All app design, code, and content is the intellectual property of IPO Tracker. Unauthorized copying or redistribution is prohibited.

5. CHANGES TO TERMS
   We reserve the right to update these terms at any time. Continued use of the app constitutes acceptance of the revised terms.

6. GOVERNING LAW
   These terms shall be governed by the laws of India. Any disputes shall be subject to the jurisdiction of courts in India."""
            )

            // ───── DEVELOPER CONTACT ─────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("👨‍💻 Developer Contact", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("App: IPO Tracker — India's Smartest Investment Companion", style = MaterialTheme.typography.bodyMedium)
                    Text("Email: ipotrackerin@gmail.com", style = MaterialTheme.typography.bodyMedium)
                    Text("Version: 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Built in India 🇮🇳 | © 2026 IPO Tracker", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PolicySection(icon: String, title: String, color: androidx.compose.ui.graphics.Color, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$icon $title", fontWeight = FontWeight.ExtraBold, color = color, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}
