package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.viewmodel.FieldOpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: FieldOpsViewModel
) {
    val isOnline by viewModel.isOnline.collectAsState()
    val offlineActionsCount by viewModel.offlineActionCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val jobs by viewModel.allJobs.collectAsState()

    var isDarkTheme by remember { mutableStateOf(false) }
    var syncLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSyncing by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Analytics Metrics (Bonus Feature)
    val completedCount = jobs.count { it.status == "Completed" }
    val totalCount = jobs.size
    val averageCompletionRate = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat() * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        "Application Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    } }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Profile summary block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                        Column {
                            Text(text = currentUser?.fullName ?: "Alex Mercer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = currentUser?.email ?: "alex@fieldops.com", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Phone: " + (currentUser?.phone ?: "+1 (555) 100-2000"), style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Preferences section
                Text(text = "Preferences", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                Spacer(modifier = Modifier.height(12.dp))

                // Dark Theme Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Dark Theme", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                            Text("Toggle light or dark visual canvas styling.", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it },
                        modifier = Modifier.testTag("dark_theme_switch")
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))

                // Offline mode Demo Switch (Offline-First Experience Core)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                        Column {
                            Text("Network Operations State", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                            Text(
                                text = if (isOnline) "ONLINE mode. Changes sync immediately to Cloud Firestore."
                                else "OFFLINE mode. Local actions are cached in Room persistence.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { viewModel.toggleNetworkMode() },
                        modifier = Modifier.testTag("network_mode_switch")
                    )
                }

                // Sync action trigger
                if (offlineActionsCount > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Pending Offline Operations ($offlineActionsCount)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You performed $offlineActionsCount work orders updates while disconnected. Click sync to dispatch them.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    isSyncing = true
                                    viewModel.syncOfflineData { logs ->
                                        syncLogs = logs
                                        isSyncing = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSyncing && isOnline
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Synchronize Local Cache ($offlineActionsCount)")
                                }
                            }
                        }
                    }
                }

                // Sync log feedback
                if (syncLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Database Sync Completed Logs:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            syncLogs.forEach { log ->
                                Text("• $log", style = MaterialTheme.typography.bodySmall, color = Color(0xFF047857))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Technician performance stats (Bonus Feature)
                Text(text = "Technician Performance KPI", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Shift Completed Tasks", style = MaterialTheme.typography.bodySmall)
                                Text("$completedCount / $totalCount Requests", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Efficiency Rating", style = MaterialTheme.typography.bodySmall)
                                Text("$averageCompletionRate%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider()

                        Spacer(modifier = Modifier.height(16.dp))

                        // KPI progress representation
                        LinearProgressIndicator(
                            progress = (averageCompletionRate.toFloat() / 100f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "KPI Metric: Your active completion rate qualifies for the Senior Level Operations Bonus (target >80%). Keep up the exceptional field operations!",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, lineHeight = 16.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Logout Button
                Button(
                    onClick = {
                        viewModel.logout {
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                                popUpTo("settings") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout from Device Session", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            // Bottom Navigation
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    } },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("jobs_list") },
                    icon = { Icon(Icons.Default.List, contentDescription = "Jobs") },
                    label = { Text("Jobs") },
                    modifier = Modifier.testTag("nav_jobs")
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("customer_profile") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Customers") },
                    label = { Text("Customers") },
                    modifier = Modifier.testTag("nav_customers")
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("notifications") },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts") },
                    modifier = Modifier.testTag("nav_alerts")
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already here */ },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    }
}
