package com.IPO.Tracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.IPO.Tracker.ui.screens.*
import com.IPO.Tracker.ui.theme.IPOTrackerTheme
import com.IPO.Tracker.viewmodel.IpoUiState
import com.IPO.Tracker.viewmodel.IpoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPOTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val prefs = getSharedPreferences("ipo_prefs", Context.MODE_PRIVATE)
                    var showSplash by remember { mutableStateOf(true) }
                    var showOnboarding by remember { mutableStateOf(false) }
                    var showApp by remember { mutableStateOf(false) }
                    val isFirstTime = prefs.getBoolean("first_time", true)

                    when {
                        showSplash -> SplashScreen(onSplashDone = {
                            showSplash = false
                            if (isFirstTime) showOnboarding = true else showApp = true
                        })
                        showOnboarding -> OnboardingScreen(onFinish = {
                            prefs.edit().putBoolean("first_time", false).apply()
                            showOnboarding = false
                            showApp = true
                        })
                        showApp -> IpoApp()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpoApp() {
    val navController = rememberNavController()
    val viewModel: IpoViewModel = viewModel()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val bottomNavItems = listOf(
        Pair("dashboard", "IPOs" to Icons.Default.Home),
        Pair("buyback", "Buyback" to Icons.Default.List),
        Pair("news", "News" to Icons.Default.List), // Use proper icon
        Pair("account", "Account" to Icons.Default.AccountCircle)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavItems.map { it.first }) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { (route, info) ->
                        NavigationBarItem(
                            icon = { Icon(info.second, contentDescription = info.first) },
                            label = { Text(info.first) },
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onIpoClick = { ipo -> navController.navigate("detail/${ipo.id}") }
                )
            }
            composable("buyback") {
                BuybackScreen(viewModel = viewModel, onBuybackClick = { id -> navController.navigate("buyback_detail/$id") })
            }
            composable("news") { NewsScreen(viewModel = viewModel) }
            composable("account") { AccountScreen(viewModel = viewModel, onPolicyClick = { navController.navigate("policy") }) }
            
            composable("policy") { PolicyScreen(onBackClick = { navController.popBackStack() }) }

            composable("buyback_detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                val buybacks = viewModel.buybacks.value
                val buyback = buybacks.find { it.id == id }
                if (buyback != null) {
                    BuybackDetailScreen(buyback = buyback, onBackClick = { navController.popBackStack() })
                }
            }
            
            composable("detail/{ipoId}") { backStackEntry ->
                val ipoId = backStackEntry.arguments?.getString("ipoId")
                val uiState = viewModel.uiState.value
                
                if (uiState is IpoUiState.Success) {
                    val ipo = uiState.ipos.find { it.id == ipoId }
                    if (ipo != null) {
                        IpoDetailScreen(ipo = ipo, onBackClick = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
