package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class JobWithCustomer(
    @Embedded val job: Job,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer
)

data class JobDetailBundle(
    val job: Job,
    val customer: Customer,
    val photos: List<JobPhoto> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0
)

data class CustomerWithDetails(
    val customer: Customer,
    val jobs: List<Job> = emptyList(),
    val quotes: List<Quote> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val totalBilled: Double = 0.0,
    val totalPaid: Double = 0.0,
    val balance: Double = 0.0
)

data class QuoteWithDetails(
    val quote: Quote,
    val customer: Customer,
    val items: List<QuoteItem> = emptyList()
)

data class DashboardStats(
    val todayJobsCount: Int = 0,
    val inProgressJobsCount: Int = 0,
    val completedThisMonthCount: Int = 0,
    val pendingJobsCount: Int = 0,
    val totalCollectedThisMonth: Double = 0.0,
    val totalPendingReceivable: Double = 0.0,
    val totalExpensesThisMonth: Double = 0.0,
    val estimatedNetIncomeThisMonth: Double = 0.0
)
