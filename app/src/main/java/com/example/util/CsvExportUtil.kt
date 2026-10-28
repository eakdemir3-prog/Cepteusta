package com.example.util

import android.content.Context
import androidx.core.content.FileProvider
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.JobWithCustomer
import com.example.data.model.Payment
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object CsvExportUtil {

    fun exportJobsToCsv(context: Context, jobs: List<JobWithCustomer>): File {
        val cacheDir = File(context.cacheDir, "exports")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file = File(cacheDir, "Ustaca_Is_Listesi_${System.currentTimeMillis()}.csv")

        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
            writer.write("\uFEFF") // UTF-8 BOM for Excel Turkish character support
            writer.write("İş Kodu;Müşteri Adı;Telefon;Başlık;Kategori;Durum;Öncelik;Tarih;Saat;Tahmini Ücret;Kesin Ücret\n")
            jobs.forEach { item ->
                val j = item.job
                val c = item.customer
                val dateStr = Formatters.formatDate(j.scheduledDate)
                writer.write("${escape(j.jobCode)};${escape(c.name)};${escape(c.phone)};${escape(j.title)};${escape(j.category)};${escape(j.status)};${escape(j.priority)};$dateStr;${escape(j.scheduledTime)};${j.estimatedPrice};${j.finalPrice}\n")
            }
        }
        return file
    }

    fun exportCustomersToCsv(context: Context, customers: List<Customer>): File {
        val cacheDir = File(context.cacheDir, "exports")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file = File(cacheDir, "Ustaca_Musteri_Listesi_${System.currentTimeMillis()}.csv")

        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
            writer.write("\uFEFF")
            writer.write("Müşteri Adı;Telefon;E-Posta;Adres;Notlar;Kayıt Tarihi\n")
            customers.forEach { c ->
                val dateStr = Formatters.formatDate(c.createdAt)
                writer.write("${escape(c.name)};${escape(c.phone)};${escape(c.email)};${escape(c.address)};${escape(c.notes)};$dateStr\n")
            }
        }
        return file
    }

    fun exportFinancesToCsv(
        context: Context,
        payments: List<Payment>,
        expenses: List<Expense>
    ): File {
        val cacheDir = File(context.cacheDir, "exports")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file = File(cacheDir, "Ustaca_Gelir_Gider_${System.currentTimeMillis()}.csv")

        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
            writer.write("\uFEFF")
            writer.write("Tür;Tarih;Kategori/Yöntem;Açıklama;Tutar (TL)\n")
            payments.forEach { p ->
                val dateStr = Formatters.formatDate(p.date)
                writer.write("Tahsilat;$dateStr;${escape(p.paymentMethod)};${escape(p.description)};+${p.amount}\n")
            }
            expenses.forEach { e ->
                val dateStr = Formatters.formatDate(e.date)
                writer.write("Gider;$dateStr;${escape(e.category)};${escape(e.description)};-${e.amount}\n")
            }
        }
        return file
    }

    private fun escape(text: String): String {
        return text.replace(";", " ").replace("\n", " ").trim()
    }
}
