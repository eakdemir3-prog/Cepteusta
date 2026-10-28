package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Hatırlatma zamanı gelince bildirimi gösterir; cihaz yeniden başlayınca hatırlatmaları yeniden kurar. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SHOW -> {
                val jobId = intent.getLongExtra(ReminderScheduler.EXTRA_JOB_ID, 0L)
                val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE).orEmpty()
                val text = intent.getStringExtra(ReminderScheduler.EXTRA_TEXT).orEmpty()
                if (jobId > 0L && title.isNotBlank()) {
                    ReminderScheduler.show(context, jobId, title, text)
                }
            }
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val pending = goAsync()
                val appContext = context.applicationContext
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val jobs = AppDatabase.getDatabase(appContext).jobDao().getAllJobsWithCustomerFlow().first()
                        ReminderScheduler.syncAll(appContext, jobs)
                    } catch (_: Exception) {
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_SHOW = "com.example.reminder.SHOW"
    }
}
