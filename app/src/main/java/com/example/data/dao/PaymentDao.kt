package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC, createdAt DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE jobId = :jobId ORDER BY date DESC")
    fun getPaymentsForJob(jobId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE jobId = :jobId ORDER BY date DESC")
    suspend fun getPaymentsForJobSync(jobId: Long): List<Payment>

    @Query("SELECT * FROM payments WHERE date BETWEEN :startTime AND :endTime ORDER BY date DESC")
    fun getPaymentsInDateRange(startTime: Long, endTime: Long): Flow<List<Payment>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE date BETWEEN :startTime AND :endTime")
    fun getTotalCollectedInDateRange(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE jobId = :jobId")
    fun getTotalPaidForJob(jobId: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deleteById(id: Long)
}
