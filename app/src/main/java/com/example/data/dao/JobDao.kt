package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Job
import com.example.data.model.JobPhoto
import com.example.data.model.JobWithCustomer
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Transaction
    @Query("SELECT * FROM jobs WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllJobsWithCustomerFlow(): Flow<List<JobWithCustomer>>

    @Transaction
    @Query("SELECT * FROM jobs WHERE scheduledDate BETWEEN :startTime AND :endTime AND isArchived = 0 ORDER BY scheduledDate ASC, scheduledTime ASC")
    fun getJobsInDateRange(startTime: Long, endTime: Long): Flow<List<JobWithCustomer>>

    @Transaction
    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    fun getJobWithCustomerByIdFlow(id: Long): Flow<JobWithCustomer?>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Long): Job?

    @Query("SELECT * FROM jobs WHERE customerId = :customerId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getJobsForCustomer(customerId: Long): Flow<List<Job>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: Job): Long

    @Update
    suspend fun update(job: Job)

    @Delete
    suspend fun delete(job: Job)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE jobs SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String)

    @Query("UPDATE jobs SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    // Photos
    @Query("SELECT * FROM job_photos WHERE jobId = :jobId ORDER BY createdAt ASC")
    fun getPhotosForJob(jobId: Long): Flow<List<JobPhoto>>

    @Query("SELECT * FROM job_photos WHERE jobId = :jobId ORDER BY createdAt ASC")
    suspend fun getPhotosForJobSync(jobId: Long): List<JobPhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: JobPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: JobPhoto)

    @Query("DELETE FROM job_photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: Long)

    // Counts
    @Query("SELECT COUNT(*) FROM jobs WHERE status = :status AND isArchived = 0")
    fun getCountByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM jobs WHERE scheduledDate BETWEEN :todayStart AND :todayEnd AND isArchived = 0")
    fun getTodayJobsCount(todayStart: Long, todayEnd: Long): Flow<Int>
}
