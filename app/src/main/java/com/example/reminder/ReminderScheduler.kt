package com.example.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Job
import com.example.data.model.JobStatus
import com.example.data.model.JobWithCustomer
import java.util.Calendar

/**
 * Randevu hatırlatmaları: her planlı iş için, randevudan seçilen süre kadar önce bildirim kurar.
 * Kesin alarm izni gerektirmez (setAndAllowWhileIdle); birkaç dakikalık sapma olabilir.
 */
object ReminderScheduler {

    const val CHANNEL_ID = "randevu_hatirlatma"
    const val EXTRA_JOB_ID = "job_id"
    const val EXTRA_TITLE = "title"
    const val EXTRA_TEXT = "text"

    private const val PREFS = "ustaca_prefs"
    private const val KEY_MINUTES = "reminder_minutes_before"
    const val DEFAULT_MINUTES = 60

    /** Ayarlarda sunulan seçenekler: dakika -> etiket. 0 = kapalı. */
    val OPTIONS: List<Pair<Int, String>> = listOf(
        0 to "Kapalı",
        30 to "30 dk önce",
        60 to "1 saat önce",
        120 to "2 saat önce",
        1440 to "1 gün önce"
    )

    fun getMinutesBefore(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_MINUTES, DEFAULT_MINUTES)

    fun setMinutesBefore(context: Context, minutes: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_MINUTES, minutes).apply()
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Randevu hatırlatmaları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Yaklaşan iş randevularını hatırlatır" }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    /** İşin randevu zamanı: tarih + "HH:mm" saat (saat yoksa 09:00). */
    fun appointmentTime(job: Job): Long? {
        if (job.scheduledDate <= 0L) return null
        val cal = Calendar.getInstance().apply { timeInMillis = job.scheduledDate }
        val parts = job.scheduledTime.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.takeIf { it in 0..23 } ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull()?.takeIf { it in 0..59 } ?: 0
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun isActive(job: Job): Boolean =
        !job.isArchived && (job.status == JobStatus.NEW || job.status == JobStatus.SCHEDULED || job.status == JobStatus.IN_PROGRESS)

    private fun pendingIntent(context: Context, jobId: Long, title: String, text: String, create: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SHOW
            putExtra(EXTRA_JOB_ID, jobId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or
            (if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE)
        return PendingIntent.getBroadcast(context, jobId.toInt(), intent, flags)
    }

    /** İş kaydedildiğinde / durumu değiştiğinde çağrılır: gerekiyorsa kurar, değilse iptal eder. */
    fun sync(context: Context, job: Job, customerName: String, address: String) {
        cancel(context, job.id)
        val minutes = getMinutesBefore(context)
        if (minutes <= 0 || !isActive(job)) return
        val at = appointmentTime(job) ?: return
        val trigger = at - minutes * 60_000L
        if (trigger <= System.currentTimeMillis()) return

        val lead = OPTIONS.firstOrNull { it.first == minutes }?.second?.removeSuffix(" önce") ?: "$minutes dk"
        val title = "Randevu yaklaşıyor ($lead sonra): ${job.title}"
        val text = buildString {
            append(customerName)
            if (job.scheduledTime.isNotBlank()) append(" • saat ${job.scheduledTime}")
            if (address.isNotBlank()) append(" • $address")
        }
        val pi = pendingIntent(context, job.id, title, text, create = true) ?: return
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        try {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        } catch (_: SecurityException) {
            alarm.set(AlarmManager.RTC_WAKEUP, trigger, pi)
        }
    }

    fun cancel(context: Context, jobId: Long) {
        val pi = pendingIntent(context, jobId, "", "", create = false) ?: return
        context.getSystemService(AlarmManager::class.java)?.cancel(pi)
        pi.cancel()
    }

    /** Tüm işleri yeniden kurar (açılışta, cihaz yeniden başlayınca, ayar değişince). */
    fun syncAll(context: Context, jobs: List<JobWithCustomer>) {
        jobs.forEach { jc ->
            sync(context, jc.job, jc.customer.name, jc.job.address.ifBlank { jc.customer.address })
        }
    }

    fun show(context: Context, jobId: Long, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        createChannel(context)
        val openApp = PendingIntent.getActivity(
            context,
            jobId.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF0F766E.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(jobId.toInt(), notification)
        } catch (_: SecurityException) {
        }
    }
}
