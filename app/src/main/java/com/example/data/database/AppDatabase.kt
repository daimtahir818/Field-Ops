package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.UserEntity
import com.example.data.model.JobEntity
import com.example.data.model.ReportEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.SessionEntity
import com.example.data.model.OfflineActionEntity

@Database(
    entities = [
        UserEntity::class,
        JobEntity::class,
        ReportEntity::class,
        CustomerEntity::class,
        SessionEntity::class,
        OfflineActionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fieldOpsDao(): FieldOpsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fieldops_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
