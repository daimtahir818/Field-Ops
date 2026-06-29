package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserEntity
import com.example.data.model.JobEntity
import com.example.data.model.ReportEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.SessionEntity
import com.example.data.model.OfflineActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldOpsDao {

    // Users
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Jobs
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobsFlow(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE requestId = :requestId LIMIT 1")
    suspend fun getJobById(requestId: String): JobEntity?

    @Query("SELECT * FROM jobs WHERE requestId = :requestId LIMIT 1")
    fun getJobByIdFlow(requestId: String): Flow<JobEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Update
    suspend fun updateJob(job: JobEntity)

    // Reports
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE requestId = :requestId")
    fun getReportsForJobFlow(requestId: String): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE customerId = :customerId LIMIT 1")
    suspend fun getCustomerById(customerId: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    // Session
    @Query("SELECT * FROM sessions WHERE id = 1 LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE id = 1 LIMIT 1")
    fun getActiveSessionFlow(): Flow<SessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun clearSession()

    // Offline actions
    @Query("SELECT * FROM offline_actions ORDER BY timestamp ASC")
    suspend fun getAllOfflineActions(): List<OfflineActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineAction(action: OfflineActionEntity)

    @Query("DELETE FROM offline_actions WHERE actionId = :actionId")
    suspend fun deleteOfflineAction(actionId: Int)
}
