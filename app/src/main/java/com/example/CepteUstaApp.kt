package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.CepteUstaRepository

class CepteUstaApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: CepteUstaRepository by lazy { CepteUstaRepository(database) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CepteUstaApp
            private set
    }
}
