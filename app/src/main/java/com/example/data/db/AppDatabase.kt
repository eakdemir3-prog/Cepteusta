package com.example.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ActivityLogDao
import com.example.data.dao.BusinessProfileDao
import com.example.data.dao.CustomerDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.JobDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.QuoteDao
import com.example.data.model.ActivityLog
import com.example.data.model.BusinessProfile
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Job
import com.example.data.model.JobPhoto
import com.example.data.model.Payment
import com.example.data.model.Quote
import com.example.data.model.QuoteItem

@Database(
    entities = [
        BusinessProfile::class,
        Customer::class,
        Job::class,
        JobPhoto::class,
        Quote::class,
        QuoteItem::class,
        Payment::class,
        Expense::class,
        ActivityLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun customerDao(): CustomerDao
    abstract fun jobDao(): JobDao
    abstract fun quoteDao(): QuoteDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** v2: işlere tamamlanma tarihi eklendi (mevcut tamamlanmış işlere oluşturma tarihi yazılır). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE jobs ADD COLUMN completedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE jobs SET completedAt = createdAt WHERE status = 'TAMAMLANDI'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ustaca.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    // Şema değişirse veriyi silmek yerine Migration eklenmeli (kullanıcı verisi korunur).
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
