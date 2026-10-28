package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object PaymentMethod {
    const val CASH = "NAKIT"
    const val CREDIT_CARD = "KART"
    const val TRANSFER = "HAVALE"
    const val OTHER = "DIGER"

    fun getLabel(method: String): String = when (method) {
        CASH -> "Nakit"
        CREDIT_CARD -> "Kredi Kartı / POS"
        TRANSFER -> "Havale / EFT"
        OTHER -> "Diğer"
        else -> method
    }
}

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Job::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["jobId"])
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val jobId: Long? = null,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = PaymentMethod.CASH,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String = "Malzeme", // Malzeme, Ulaşım, Yakıt, Ekipman, Kira, Fatura, Diğer
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val receiptPhotoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val type: String, // JOB, PAYMENT, QUOTE, CUSTOMER, EXPENSE
    val relatedId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ExpenseCategorySum(
    val category: String,
    val total: Double
)
