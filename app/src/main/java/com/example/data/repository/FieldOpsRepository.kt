package com.example.data.repository

import com.example.data.database.FieldOpsDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FieldOpsRepository(private val dao: FieldOpsDao) {

    val allJobs: Flow<List<JobEntity>> = dao.getAllJobsFlow()
    val allReports: Flow<List<ReportEntity>> = dao.getAllReportsFlow()
    val allCustomers: Flow<List<CustomerEntity>> = dao.getAllCustomersFlow()
    val activeSession: Flow<SessionEntity?> = dao.getActiveSessionFlow()

    // Mock network status (can be toggled in settings for Offline-First demo)
    private var isOnlineState = true

    fun setOnlineStatus(online: Boolean) {
        isOnlineState = online
    }

    fun isOnline(): Boolean = isOnlineState

    suspend fun seedDatabaseIfEmpty() {
        // Only seed if no jobs exist
        val currentJobs = dao.getAllJobsFlow().firstOrNull() ?: emptyList()
        if (currentJobs.isEmpty()) {
            val defaultUser = UserEntity(
                userId = "tech_123",
                fullName = "Alex Mercer",
                email = "alex@fieldops.com",
                phone = "+1 (555) 100-2000",
                role = "technician",
                createdAt = getCurrentTimestamp()
            )
            dao.insertUser(defaultUser)

            val seedCustomers = listOf(
                CustomerEntity(
                    customerId = "cust_01",
                    name = "John Doe",
                    phone = "+1 (555) 019-2831",
                    address = "123 Silicon Valley Road, San Jose, CA",
                    historyJson = "[{\"date\":\"2026-04-12\",\"type\":\"AC Filter Replacement\",\"notes\":\"Completed routine service.\"}]"
                ),
                CustomerEntity(
                    customerId = "cust_02",
                    name = "Alice Smith",
                    phone = "+1 (555) 024-8592",
                    address = "456 Oakwood Avenue, San Francisco, CA",
                    historyJson = "[]"
                ),
                CustomerEntity(
                    customerId = "cust_03",
                    name = "Bob Martinez",
                    phone = "+1 (555) 031-7744",
                    address = "789 Pine Heights, Berkeley, CA",
                    historyJson = "[{\"date\":\"2025-11-05\",\"type\":\"Fuse Box Repair\",\"notes\":\"Replaced main breakers.\"}]"
                ),
                CustomerEntity(
                    customerId = "cust_04",
                    name = "Sarah Johnson",
                    phone = "+1 (555) 042-1234",
                    address = "101 Grand Blvd, Oakland, CA",
                    historyJson = "[{\"date\":\"2026-01-20\",\"type\":\"Drain Cleaning\",\"notes\":\"Unclogged master bathroom drain.\"}]"
                )
            )
            dao.insertCustomers(seedCustomers)

            val seedJobs = listOf(
                JobEntity(
                    requestId = "REQ-1001",
                    customerName = "John Doe",
                    serviceType = "HVAC Repair",
                    issueDescription = "Central HVAC blowing lukewarm air. Fan spin seems sluggish and unit makes a loud humming noise.",
                    status = "Assigned",
                    assignedTechnician = "tech_123",
                    serviceDate = "2026-06-29",
                    location = "123 Silicon Valley Road, San Jose, CA",
                    phone = "+1 (555) 019-2831",
                    createdAt = getCurrentTimestamp(),
                    latitude = 37.3382,
                    longitude = -121.8863
                ),
                JobEntity(
                    requestId = "REQ-1002",
                    customerName = "Alice Smith",
                    serviceType = "Electrical Troubleshooting",
                    issueDescription = "Master bedroom wall outlets sparking when plugging in appliances. Breaker panel making a buzzing noise.",
                    status = "In Progress",
                    assignedTechnician = "tech_123",
                    serviceDate = "2026-06-29",
                    location = "456 Oakwood Avenue, San Francisco, CA",
                    phone = "+1 (555) 024-8592",
                    createdAt = getCurrentTimestamp(),
                    latitude = 37.7749,
                    longitude = -122.4194
                ),
                JobEntity(
                    requestId = "REQ-1003",
                    customerName = "Bob Martinez",
                    serviceType = "Commercial AC Maintenance",
                    issueDescription = "Regular 6-month preventive system maintenance and filter replacement for warehouse cooling systems.",
                    status = "Completed",
                    assignedTechnician = "tech_123",
                    serviceDate = "2026-06-28",
                    location = "789 Pine Heights, Berkeley, CA",
                    phone = "+1 (555) 031-7744",
                    createdAt = getCurrentTimestamp(),
                    latitude = 37.8715,
                    longitude = -122.2730,
                    technicianNotes = "All air filters replaced, pressure test completed within guidelines. Coils cleaned."
                ),
                JobEntity(
                    requestId = "REQ-1004",
                    customerName = "Sarah Johnson",
                    serviceType = "Plumbing Leakage Repair",
                    issueDescription = "Water dripping rapidly through the ceiling underneath the main hallway bathroom. Main valve shutoff required.",
                    status = "Assigned",
                    assignedTechnician = "tech_123",
                    serviceDate = "2026-06-30",
                    location = "101 Grand Blvd, Oakland, CA",
                    phone = "+1 (555) 042-1234",
                    createdAt = getCurrentTimestamp(),
                    latitude = 37.8044,
                    longitude = -122.2711
                )
            )
            dao.insertJobs(seedJobs)

            // Seed a report for the completed job
            val seedReport = ReportEntity(
                reportId = "REP-001",
                requestId = "REQ-1003",
                findings = "Identified dirty condenser coils and heavily clogged intake filters. Refrigerant pressure levels were nominal.",
                actionsTaken = "Thoroughly chemically cleaned the condenser coils. Replaced 3 commercial-grade pleated filters. Verified system current draw.",
                completionNotes = "System is operating at peak cooling efficiency. Recommended next checkup in 6 months.",
                createdAt = getCurrentTimestamp(),
                createdBy = "Alex Mercer"
            )
            dao.insertReport(seedReport)
        }
    }

    suspend fun getJobById(requestId: String): JobEntity? {
        return dao.getJobById(requestId)
    }

    fun getJobByIdFlow(requestId: String): Flow<JobEntity?> {
        return dao.getJobByIdFlow(requestId)
    }

    fun getReportsForJob(requestId: String): Flow<List<ReportEntity>> {
        return dao.getReportsForJobFlow(requestId)
    }

    suspend fun updateJobStatus(requestId: String, status: String, notes: String = ""): Boolean {
        val job = dao.getJobById(requestId) ?: return false
        job.status = status
        if (notes.isNotEmpty()) {
            job.technicianNotes = notes
        }

        if (isOnlineState) {
            job.isSynced = true
            dao.updateJob(job)
        } else {
            job.isSynced = false
            dao.updateJob(job)
            // Log offline action
            dao.insertOfflineAction(
                OfflineActionEntity(
                    requestId = requestId,
                    actionType = "UPDATE_STATUS",
                    payload = "$status|$notes"
                )
            )
        }
        return true
    }

    suspend fun submitServiceReport(
        requestId: String,
        findings: String,
        actionsTaken: String,
        completionNotes: String,
        technicianName: String
    ): ReportEntity {
        val reportId = "REP-" + UUID.randomUUID().toString().takeLast(6).uppercase()
        val report = ReportEntity(
            reportId = reportId,
            requestId = requestId,
            findings = findings,
            actionsTaken = actionsTaken,
            completionNotes = completionNotes,
            createdAt = getCurrentTimestamp(),
            createdBy = technicianName,
            isSynced = isOnlineState
        )

        // Save report locally
        dao.insertReport(report)

        // Mark corresponding job as Completed
        val job = dao.getJobById(requestId)
        if (job != null) {
            job.status = "Completed"
            job.isSynced = isOnlineState
            dao.updateJob(job)
        }

        if (!isOnlineState) {
            // Queue report submission offline
            dao.insertOfflineAction(
                OfflineActionEntity(
                    requestId = requestId,
                    actionType = "CREATE_REPORT",
                    payload = "$reportId|$findings|$actionsTaken|$completionNotes|$technicianName"
                )
            )
        }

        return report
    }

    suspend fun syncOfflineActions(): List<String> {
        val syncLogs = mutableListOf<String>()
        if (!isOnlineState) return syncLogs

        val actions = dao.getAllOfflineActions()
        for (action in actions) {
            when (action.actionType) {
                "UPDATE_STATUS" -> {
                    val parts = action.payload.split("|")
                    if (parts.size >= 2) {
                        val status = parts[0]
                        val notes = parts[1]
                        val job = dao.getJobById(action.requestId)
                        if (job != null) {
                            job.status = status
                            job.technicianNotes = notes
                            job.isSynced = true
                            dao.updateJob(job)
                            syncLogs.add("Synced Status of ${job.requestId} to '$status'")
                        }
                    }
                }
                "CREATE_REPORT" -> {
                    val parts = action.payload.split("|")
                    if (parts.size >= 5) {
                        val reportId = parts[0]
                        val findings = parts[1]
                        val actionsTaken = parts[2]
                        val completionNotes = parts[3]
                        val techName = parts[4]

                        // Find report and mark synced
                        val job = dao.getJobById(action.requestId)
                        if (job != null) {
                            job.status = "Completed"
                            job.isSynced = true
                            dao.updateJob(job)
                            syncLogs.add("Synced Completion Report for ${job.requestId}")
                        }
                    }
                }
            }
            dao.deleteOfflineAction(action.actionId)
        }

        // Also mark any unsynced jobs/reports as synced
        val allJobsList = dao.getAllJobsFlow().firstOrNull() ?: emptyList()
        for (job in allJobsList) {
            if (!job.isSynced) {
                job.isSynced = true
                dao.updateJob(job)
            }
        }

        return syncLogs
    }

    suspend fun getPendingOfflineActionCount(): Int {
        return dao.getAllOfflineActions().size
    }

    // Authentication mock flows
    suspend fun login(email: String, password: String): Boolean {
        // Simple validator: email must be valid format, check against seed user
        if (email.contains("@") && password.length >= 6) {
            var user = dao.getUserByEmail(email)
            if (user == null) {
                // Auto create for ease of testing if not matches
                val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                user = UserEntity(
                    userId = "user_" + UUID.randomUUID().toString().takeLast(5),
                    fullName = "$name Technician",
                    email = email,
                    phone = "+1 (555) 909-1234",
                    createdAt = getCurrentTimestamp()
                )
                dao.insertUser(user)
            }
            dao.insertSession(SessionEntity(activeUserId = user.userId, activeUserEmail = user.email))
            return true
        }
        return false
    }

    suspend fun register(fullName: String, email: String, phone: String, password: String): Boolean {
        if (fullName.isNotEmpty() && email.contains("@") && phone.isNotEmpty() && password.length >= 6) {
            val user = UserEntity(
                userId = "user_" + UUID.randomUUID().toString().takeLast(5),
                fullName = fullName,
                email = email,
                phone = phone,
                createdAt = getCurrentTimestamp()
            )
            dao.insertUser(user)
            dao.insertSession(SessionEntity(activeUserId = user.userId, activeUserEmail = user.email))
            return true
        }
        return false
    }

    suspend fun resetPassword(email: String): Boolean {
        // Mock successful reset
        return email.contains("@")
    }

    suspend fun logout() {
        dao.clearSession()
    }

    suspend fun getActiveUser(): UserEntity? {
        val session = dao.getActiveSession() ?: return null
        return dao.getUserById(session.activeUserId ?: "")
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
