package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val turkishLocale = Locale("tr", "TR")
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", turkishLocale)
    private val timeFormatter = SimpleDateFormat("HH:mm", turkishLocale)
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", turkishLocale)
    private val monthYearFormatter = SimpleDateFormat("MMMM yyyy", turkishLocale)
    private val dayMonthFormatter = SimpleDateFormat("dd MMMM", turkishLocale)

    fun formatCurrency(amount: Double, symbol: String = "₺"): String {
        val bd = BigDecimal(amount).setScale(2, RoundingMode.HALF_UP)
        val format = NumberFormat.getNumberInstance(turkishLocale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "${format.format(bd)} $symbol"
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        return dateFormatter.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        return timeFormatter.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        return dateTimeFormatter.format(Date(timestamp))
    }

    fun formatMonthYear(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        return monthYearFormatter.format(Date(timestamp))
    }

    fun formatDayMonth(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        return dayMonthFormatter.format(Date(timestamp))
    }

    fun getTodayStartMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getTodayEndMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getMonthStartMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
