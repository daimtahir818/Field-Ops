package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.FieldOpsRepository

class FieldOpsApplication : Application() {

    lateinit var database: AppDatabase
    lateinit var repository: FieldOpsRepository

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = FieldOpsRepository(database.fieldOpsDao())
    }
}
