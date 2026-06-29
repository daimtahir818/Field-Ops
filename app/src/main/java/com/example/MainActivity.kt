package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FieldOpsViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val navController = rememberNavController()
          val viewModel: FieldOpsViewModel = viewModel()

          NavHost(
            navController = navController,
            startDestination = "splash"
          ) {
            composable("splash") { SplashScreen(navController, viewModel) }
            composable("login") { LoginScreen(navController, viewModel) }
            composable("register") { RegisterScreen(navController, viewModel) }
            composable("dashboard") { DashboardScreen(navController, viewModel) }
            composable("jobs_list") { JobsListScreen(navController, viewModel) }
            composable("job_detail") { JobDetailScreen(navController, viewModel) }
            composable("service_report") { ServiceReportScreen(navController, viewModel) }
            composable("customer_profile") { CustomerProfileScreen(navController, viewModel) }
            composable("notifications") { NotificationsScreen(navController, viewModel) }
            composable("settings") { SettingsScreen(navController, viewModel) }
          }
        }
      }
    }
  }
}
