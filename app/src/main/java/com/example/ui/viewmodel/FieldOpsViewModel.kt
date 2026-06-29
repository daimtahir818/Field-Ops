package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FieldOpsApplication
import com.example.data.model.*
import com.example.data.repository.FieldOpsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SystemNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    var isRead: Boolean = false
)

class FieldOpsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FieldOpsRepository = (application as FieldOpsApplication).repository

    val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<SessionEntity?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isOnline = MutableStateFlow(repository.isOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _offlineActionCount = MutableStateFlow(0)
    val offlineActionCount: StateFlow<Int> = _offlineActionCount.asStateFlow()

    private val _notifications = MutableStateFlow<List<SystemNotification>>(emptyList())
    val notifications: StateFlow<List<SystemNotification>> = _notifications.asStateFlow()

    // Temporary screen/selection state
    private val _selectedJobId = MutableStateFlow<String?>(null)
    val selectedJob: StateFlow<JobEntity?> = _selectedJobId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getJobByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedCustomerId = MutableStateFlow<String?>(null)
    val selectedCustomer = _selectedCustomerId
        .map { id ->
            if (id == null) null else repository.allCustomers.firstOrNull()?.find { it.customerId == id }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            updateCurrentUser()
            updateOfflineCount()

            // Seed initial notifications
            _notifications.value = listOf(
                SystemNotification(
                    title = "System Ready",
                    message = "FieldOps initial database synchronized. Ready for field work.",
                    isRead = true
                ),
                SystemNotification(
                    title = "New Job Assigned",
                    message = "You have been assigned to 'HVAC Repair' at 123 Silicon Valley Road.",
                    isRead = false
                )
            )
        }
    }

    fun selectJob(requestId: String?) {
        _selectedJobId.value = requestId
    }

    fun selectCustomer(customerId: String?) {
        _selectedCustomerId.value = customerId
    }

    private suspend fun updateCurrentUser() {
        _currentUser.value = repository.getActiveUser()
    }

    private suspend fun updateOfflineCount() {
        _offlineActionCount.value = repository.getPendingOfflineActionCount()
    }

    fun toggleNetworkMode() {
        val nextMode = !_isOnline.value
        _isOnline.value = nextMode
        repository.setOnlineStatus(nextMode)
        if (nextMode) {
            triggerNotification("Online Mode Enabled", "You are now online. Auto-synchronizing changes...")
            syncOfflineData()
        } else {
            triggerNotification("Offline Mode Enabled", "Working offline. Changes will be queued and synchronized automatically.")
        }
    }

    fun syncOfflineData(onSyncComplete: ((List<String>) -> Unit)? = null) {
        viewModelScope.launch {
            if (_isOnline.value) {
                val logs = repository.syncOfflineActions()
                updateOfflineCount()
                if (logs.isNotEmpty()) {
                    triggerNotification("Sync Complete", "Successfully synchronized ${logs.size} offline actions.")
                }
                onSyncComplete?.invoke(logs)
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.login(email, password)
            if (success) {
                updateCurrentUser()
                triggerNotification("Login Successful", "Welcome back to FieldOps, ${email.substringBefore("@")}.")
            }
            onResult(success)
        }
    }

    fun register(fullName: String, email: String, phone: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.register(fullName, email, phone, password)
            if (success) {
                updateCurrentUser()
                triggerNotification("Welcome to FieldOps", "Account created successfully for $fullName.")
            }
            onResult(success)
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            _currentUser.value = null
            onComplete()
        }
    }

    fun forgotPassword(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.resetPassword(email)
            onResult(success)
        }
    }

    fun updateJobStatus(requestId: String, status: String, notes: String = "", onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.updateJobStatus(requestId, status, notes)
            updateOfflineCount()
            if (success) {
                val statusMsg = if (_isOnline.value) "Status updated to $status." else "Status updated (offline queued)."
                triggerNotification("Job Update", "Job $requestId updated: $status")
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun submitServiceReport(
        requestId: String,
        findings: String,
        actionsTaken: String,
        completionNotes: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val techName = _currentUser.value?.fullName ?: "Alex Mercer"
            repository.submitServiceReport(
                requestId = requestId,
                findings = findings,
                actionsTaken = actionsTaken,
                completionNotes = completionNotes,
                technicianName = techName
            )
            updateOfflineCount()
            triggerNotification("Service Report Submitted", "Completion report for job $requestId filed successfully.")
            onResult(true)
        }
    }

    fun triggerNotification(title: String, message: String) {
        val list = _notifications.value.toMutableList()
        list.add(0, SystemNotification(title = title, message = message, isRead = false))
        _notifications.value = list
    }

    fun markNotificationRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }
}
