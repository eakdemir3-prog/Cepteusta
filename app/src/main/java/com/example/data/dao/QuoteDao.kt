package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun getAllQuotes(): Flow<List<Quote>>

    @Query("SELECT * FROM quotes WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getQuotesForCustomer(customerId: Long): Flow<List<Quote>>

    @Query("SELECT * FROM quotes WHERE id = :id LIMIT 1")
    fun getQuoteByIdFlow(id: Long): Flow<Quote?>

    @Query("SELECT * FROM quotes WHERE id = :id LIMIT 1")
    suspend fun getQuoteById(id: Long): Quote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote): Long

    @Update
    suspend fun updateQuote(quote: Quote)

    @Delete
    suspend fun deleteQuote(quote: Quote)

    @Query("DELETE FROM quotes WHERE id = :id")
    suspend fun deleteQuoteById(id: Long)

    @Query("UPDATE quotes SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE quotes SET status = 'SURESI_DOLDU' WHERE status IN ('TASLAK', 'GONDERILDI') AND validUntilDate > 0 AND validUntilDate < :now")
    suspend fun expireOldQuotes(now: Long): Int

    // Items
    @Query("SELECT * FROM quote_items WHERE quoteId = :quoteId ORDER BY id ASC")
    fun getItemsForQuote(quoteId: Long): Flow<List<QuoteItem>>

    @Query("SELECT * FROM quote_items WHERE quoteId = :quoteId ORDER BY id ASC")
    suspend fun getItemsForQuoteSync(quoteId: Long): List<QuoteItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<QuoteItem>)

    @Query("DELETE FROM quote_items WHERE quoteId = :quoteId")
    suspend fun deleteItemsForQuote(quoteId: Long)

    @Transaction
    suspend fun saveQuoteWithItems(quote: Quote, items: List<QuoteItem>): Long {
        val quoteId = if (quote.id == 0L) {
            insertQuote(quote)
        } else {
            updateQuote(quote)
            quote.id
        }
        deleteItemsForQuote(quoteId)
        val preparedItems = items.map { it.copy(quoteId = quoteId) }
        insertItems(preparedItems)
        return quoteId
    }
}
