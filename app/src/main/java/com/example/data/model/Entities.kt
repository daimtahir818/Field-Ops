package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String = "technician",
    val createdAt: String
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val requestId: String,
    val customerName: String,
    val serviceType: String,
    val issueDescription: String,
    var status: String, // "Assigned", "Accepted", "In Progress", "On Hold", "Completed", "Cancelled"
    val assignedTechnician: String,
    val serviceDate: String,
    val location: String,
    val phone: String,
    val createdAt: String,
    var technicianNotes: String = "",
    var latitude: Double = 37.7749,
    var longitude: Double = -122.4194,
    var imageUri: String? = null,
    val verificationCode: String = "QR-OP-${requestId.takeLast(4)}",
    var isSynced: Boolean = true
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val reportId: String,
    val requestId: String,
    val findings: String,
    val actionsTaken: String,
    val completionNotes: String,
    val createdAt: String,
    val createdBy: String,
    var isSynced: Boolean = true
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerId: String,
    val name: String,
    val phone: String,
    val address: String,
    val historyJson: String // Serialized history list
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val activeUserId: String?,
    val activeUserEmail: String?
)

@Entity(tableName = "offline_actions")
data class OfflineActionEntity(
    @PrimaryKey(autoGenerate = true) val actionId: Int = 0,
    val requestId: String,
    val actionType: String, // "UPDATE_STATUS", "UPDATE_NOTES", "CREATE_REPORT"
    val payload: String, // JSON payload representing the change
    val timestamp: Long = System.currentTimeMillis()
)
