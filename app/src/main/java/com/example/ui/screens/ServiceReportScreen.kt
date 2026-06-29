package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.ui.viewmodel.FieldOpsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceReportScreen(
    navController: NavController,
    viewModel: FieldOpsViewModel
) {
    val job by viewModel.selectedJob.collectAsState()
    val scrollState = rememberScrollState()

    var findings by remember { mutableStateOf("") }
    var actionsTaken by remember { mutableStateOf("") }
    var completionNotes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPdfPreview by remember { mutableStateOf(false) }

    if (job == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active job selected.")
        }
        return
    }

    val currentJob = job!!

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("service_report_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        "Submit Service Report",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                // Job Summary Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WORK ORDER SUMMARY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${currentJob.requestId} • ${currentJob.serviceType}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "Customer: ${currentJob.customerName}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Report Information",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Technical Findings Field
                OutlinedTextField(
                    value = findings,
                    onValueChange = {
                        findings = it
                        errorMessage = null
                    },
                    label = { Text("Technical Findings & Diagnostics") },
                    placeholder = { Text("What did you identify as the root cause of the issue?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("report_findings_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Actions Taken Field
                OutlinedTextField(
                    value = actionsTaken,
                    onValueChange = {
                        actionsTaken = it
                        errorMessage = null
                    },
                    label = { Text("Actions Performed & Repairs") },
                    placeholder = { Text("What steps and parts did you replace to resolve the issue?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("report_actions_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Completion Notes
                OutlinedTextField(
                    value = completionNotes,
                    onValueChange = { completionNotes = it },
                    label = { Text("Completion Remarks & Client Notes") },
                    placeholder = { Text("Notes for the customer or supervisor regarding post-service specs...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("report_notes_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Export Preview Option
                Button(
                    onClick = {
                        if (findings.isEmpty() || actionsTaken.isEmpty()) {
                            errorMessage = "Please enter technical findings and actions taken before previewing."
                            return@Button
                        }
                        showPdfPreview = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "PDF Preview")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Preview PDF Report", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Complete Job Button
                Button(
                    onClick = {
                        if (findings.isEmpty() || actionsTaken.isEmpty()) {
                            errorMessage = "Please enter technical findings and actions taken before completing work order."
                            return@Button
                        }
                        viewModel.submitServiceReport(
                            requestId = currentJob.requestId,
                            findings = findings,
                            actionsTaken = actionsTaken,
                            completionNotes = completionNotes
                        ) {
                            navController.navigate("dashboard") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_report_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Submit", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Work Report", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }

    // PDF Preview Dialog (Bonus Feature)
    if (showPdfPreview) {
        Dialog(onDismissRequest = { showPdfPreview = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PDF REPORT PREVIEW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        IconButton(onClick = { showPdfPreview = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("FieldOps System Report Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Date generated: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()), style = MaterialTheme.typography.labelSmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Work Order Details:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Order ID: ${currentJob.requestId}", style = MaterialTheme.typography.bodySmall)
                    Text("Customer: ${currentJob.customerName}", style = MaterialTheme.typography.bodySmall)
                    Text("Location: ${currentJob.location}", style = MaterialTheme.typography.bodySmall)
                    Text("Service Type: ${currentJob.serviceType}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Technical Findings:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Text(findings, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Actions Taken:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Text(actionsTaken, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Completion Remarks:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Text(completionNotes.ifEmpty { "None" }, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                showPdfPreview = false
                                viewModel.triggerNotification("PDF Exported", "Report PDF exported and saved to /Documents/FieldOps/REP-${currentJob.requestId}.pdf (Mock)")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save PDF")
                        }
                        OutlinedButton(
                            onClick = { showPdfPreview = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
