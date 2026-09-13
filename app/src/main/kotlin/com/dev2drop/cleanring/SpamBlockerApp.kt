package com.dev2drop.cleanring

import android.app.Application
import com.dev2drop.cleanring.billing.BillingManager
import com.dev2drop.cleanring.data.AppDatabase
import com.dev2drop.cleanring.data.PrefixRepository
import com.dev2drop.cleanring.data.SpamBlockerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SpamBlockerApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this) }
    val spamRepository by lazy {
        SpamBlockerRepository(
            context = this,
            ruleDao = database.ruleDao(),
            callLogDao = database.callLogDao(),
            prefixDao = database.prefixDao()
        )
    }
    val repository by lazy { PrefixRepository(this, database.prefixDao()) }
    val billingManager by lazy { BillingManager(this, applicationScope) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        applicationScope.launch {
            spamRepository.initializeCache()
            repository.initializeCache()
        }
    }

    companion object {
        lateinit var instance: SpamBlockerApp
            private set
    }
}
