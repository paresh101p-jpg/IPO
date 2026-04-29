package com.IPO.Tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.IPO.Tracker.ui.screens.DashboardScreen
import com.IPO.Tracker.ui.screens.IpoDetailScreen
import com.IPO.Tracker.viewmodel.IpoUiState
import com.IPO.Tracker.viewmodel.IpoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IpoApp()
                }
            }
        }
    }
}

@Composable
fun IpoApp() {
    val navController = rememberNavController()
    val viewModel: IpoViewModel = viewModel()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onIpoClick = { ipo ->
                    navController.navigate("detail/${ipo.id}")
                }
            )
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
