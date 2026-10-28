package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object JobStatus {
    const val NEW = "YENI"
    const val SCHEDULED = "PLANLANDI"
    const val IN_PROGRESS = "DEVAM_EDIYOR"
    const val COMPLETED = "TAMAMLANDI"
    const val CANCELLED = "IPTAL"

    fun getLabel(status: String): String = when (status) {
        NEW -> "Yeni"
        SCHEDULED -> "Planlandı"
        IN_PROGRESS -> "Devam Ediyor"
        COMPLETED -> "Tamamlandı"
        CANCELLED -> "İptal Edildi"
        else -> status
    }
}

object JobPriority {
    const val NORMAL = "NORMAL"
    const val HIGH = "YUKSEK"
    const val URGENT = "ACIL"

    fun getLabel(priority: String): String = when (priority) {
        NORMAL -> "Normal"
        HIGH -> "Yüksek"
        URGENT -> "Acil"
        else -> priority
    }
}

@Entity(
    tableName = "jobs",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobCode: String = "",
    val customerId: Long,
    val title: String,
    val description: String = "",
    val category: String = "Genel Tamirat",
    val address: String = "",
    val scheduledDate: Long = 0L,
    val scheduledTime: String = "",
    val status: String = JobStatus.NEW,
    val priority: String = JobPriority.NORMAL,
    val estimatedPrice: Double = 0.0,
    val finalPrice: Double = 0.0,
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "job_photos",
    foreignKeys = [
        ForeignKey(
            entity = Job::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["jobId"])]
)
data class JobPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val filePath: String,
    val photoType: String = "ONCE", // ONCE, SONRA, BELGE
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
