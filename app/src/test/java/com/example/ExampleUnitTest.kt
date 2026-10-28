package com.example

import com.example.data.model.JobPriority
import com.example.data.model.JobStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.QuoteStatus
import com.example.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun currencyFormatting_isAccurate() {
        val formatted = Formatters.formatCurrency(1250.50, "₺")
        assertTrue(formatted.contains("1.250,50") && formatted.contains("₺"))
    }

    @Test
    fun jobStatus_labelsMatchTurkish() {
        assertEquals("Yeni", JobStatus.getLabel(JobStatus.NEW))
        assertEquals("Planlandı", JobStatus.getLabel(JobStatus.SCHEDULED))
        assertEquals("Devam Ediyor", JobStatus.getLabel(JobStatus.IN_PROGRESS))
        assertEquals("Tamamlandı", JobStatus.getLabel(JobStatus.COMPLETED))
        assertEquals("İptal Edildi", JobStatus.getLabel(JobStatus.CANCELLED))
    }

    @Test
    fun jobPriority_labelsMatchTurkish() {
        assertEquals("Normal", JobPriority.getLabel(JobPriority.NORMAL))
        assertEquals("Yüksek", JobPriority.getLabel(JobPriority.HIGH))
        assertEquals("Acil", JobPriority.getLabel(JobPriority.URGENT))
    }

    @Test
    fun paymentMethod_labelsMatchTurkish() {
        assertEquals("Nakit", PaymentMethod.getLabel(PaymentMethod.CASH))
        assertEquals("Kredi Kartı / POS", PaymentMethod.getLabel(PaymentMethod.CREDIT_CARD))
        assertEquals("Havale / EFT", PaymentMethod.getLabel(PaymentMethod.TRANSFER))
    }

    @Test
    fun quoteStatus_labelsMatchTurkish() {
        assertEquals("Taslak", QuoteStatus.getLabel(QuoteStatus.DRAFT))
        assertEquals("Kabul Edildi", QuoteStatus.getLabel(QuoteStatus.ACCEPTED))
    }
}
