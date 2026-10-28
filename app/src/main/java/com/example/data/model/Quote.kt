package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object QuoteStatus {
    const val DRAFT = "TASLAK"
    const val SENT = "GONDERILDI"
    const val ACCEPTED = "KABUL_EDILDI"
    const val REJECTED = "REDDEDILDI"
    const val EXPIRED = "SURESI_DOLDU"

    fun getLabel(status: String): String = when (status) {
        DRAFT -> "Taslak"
        SENT -> "Gönderildi"
        ACCEPTED -> "Kabul Edildi"
        REJECTED -> "Reddedildi"
        EXPIRED -> "Süresi Doldu"
        else -> status
    }
}

@Entity(
    tableName = "quotes",
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
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteNumber: String = "",
    val customerId: Long,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val validUntilDate: Long = System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000), // 15 days
    val status: String = QuoteStatus.DRAFT,
    val notes: String = "",
    val discountPercent: Double = 0.0,
    val taxPercent: Double = 0.0,
    val totalAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quote_items",
    foreignKeys = [
        ForeignKey(
            entity = Quote::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quoteId"])]
)
data class QuoteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteId: Long,
    val description: String,
    val quantity: Double = 1.0,
    val unit: String = "Adet", // Adet, Saat, Gün, Metre, Paket
    val unitPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val total: Double = 0.0
)
