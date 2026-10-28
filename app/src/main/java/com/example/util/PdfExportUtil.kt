package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.BusinessProfile
import com.example.data.model.Customer
import com.example.data.model.Job
import com.example.data.model.Payment
import com.example.data.model.PaymentMethod
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import java.io.File
import java.io.FileOutputStream

object PdfExportUtil {

    fun generateQuotePdf(
        context: Context,
        profile: BusinessProfile,
        customer: Customer,
        quote: Quote,
        items: List<QuoteItem>
    ): File? {
        return try {
            val document = PdfDocument()
            val pageWidth = 595 // A4 standard point size
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Paints
            val primaryPaint = Paint().apply {
                color = Color.parseColor("#0F766E")
                isAntiAlias = true
            }
            val accentPaint = Paint().apply {
                color = Color.parseColor("#10B981")
                isAntiAlias = true
            }
            val textDark = Paint().apply {
                color = Color.parseColor("#172033")
                textSize = 12f
                isAntiAlias = true
            }
            val textMuted = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 10f
                isAntiAlias = true
            }
            val textTitle = Paint().apply {
                color = Color.parseColor("#0F766E")
                textSize = 20f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val textHeader = Paint().apply {
                color = Color.WHITE
                textSize = 11f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = Color.parseColor("#E5EAF0")
                strokeWidth = 1f
            }

            // Top Header Banner
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, primaryPaint)

            // Business Logo or Fallback icon
            var logoDrawn = false
            if (!profile.logoPath.isNullOrBlank()) {
                val logoFile = File(profile.logoPath)
                if (logoFile.exists()) {
                    val rawBm = BitmapFactory.decodeFile(logoFile.absolutePath)
                    if (rawBm != null) {
                        val scaledBm = Bitmap.createScaledBitmap(rawBm, 60, 60, true)
                        canvas.drawBitmap(scaledBm, 28f, 15f, null)
                        logoDrawn = true
                    }
                }
            }
            val textOffsetLeft = if (logoDrawn) 100f else 32f

            // Business Name & Title
            val bannerTitle = Paint().apply {
                color = Color.WHITE
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(profile.businessName, textOffsetLeft, 40f, bannerTitle)

            val bannerSub = Paint().apply {
                color = Color.parseColor("#CCFBF1")
                textSize = 11f
                isAntiAlias = true
            }
            canvas.drawText("${profile.category} • ${profile.phone}", textOffsetLeft, 58f, bannerSub)

            // Right side badge: TEKLİF
            val quoteBadgePaint = Paint().apply {
                color = Color.WHITE
                textSize = 16f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("FİYAT TEKLİFİ", pageWidth - 32f, 40f, quoteBadgePaint)

            val quoteNoPaint = Paint().apply {
                color = Color.parseColor("#CCFBF1")
                textSize = 11f
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("No: ${quote.quoteNumber.ifEmpty { "TEK-${quote.id}" }}", pageWidth - 32f, 58f, quoteNoPaint)

            // Customer Info Block & Quote Info Block
            var yPos = 120f
            // Customer Card Box
            val boxPaint = Paint().apply {
                color = Color.parseColor("#F4F8F5")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(32f, yPos, 280f, yPos + 80f), 8f, 8f, boxPaint)
            canvas.drawRoundRect(RectF(315f, yPos, pageWidth - 32f, yPos + 80f), 8f, 8f, boxPaint)

            // Left: Sayın Müşteri
            val sectionHeaderPaint = Paint().apply {
                color = Color.parseColor("#0F766E")
                textSize = 11f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("SAYIN / MÜŞTERİ BİLGİLERİ", 44f, yPos + 20f, sectionHeaderPaint)
            val custNamePaint = Paint().apply {
                color = Color.parseColor("#172033")
                textSize = 13f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(customer.name, 44f, yPos + 38f, custNamePaint)
            canvas.drawText("Tel: ${customer.phone}", 44f, yPos + 54f, textDark)
            if (customer.address.isNotBlank()) {
                val shortAddress = if (customer.address.length > 35) customer.address.take(35) + "..." else customer.address
                canvas.drawText(shortAddress, 44f, yPos + 70f, textMuted)
            }

            // Right: Teklif Detayları
            canvas.drawText("TEKLİF DETAYLARI", 327f, yPos + 20f, sectionHeaderPaint)
            canvas.drawText("Başlık: ${quote.title}", 327f, yPos + 38f, textDark)
            canvas.drawText("Tarih: ${Formatters.formatDate(quote.date)}", 327f, yPos + 54f, textMuted)
            canvas.drawText("Geçerlilik: ${Formatters.formatDate(quote.validUntilDate)}", 327f, yPos + 70f, textMuted)

            yPos += 105f

            // Items Table Header
            val thBg = Paint().apply {
                color = Color.parseColor("#0F766E")
            }
            canvas.drawRoundRect(RectF(32f, yPos, pageWidth - 32f, yPos + 26f), 4f, 4f, thBg)
            canvas.drawText("#", 42f, yPos + 17f, textHeader)
            canvas.drawText("Açıklama / Hizmet Kalemi", 66f, yPos + 17f, textHeader)
            canvas.drawText("Miktar", 330f, yPos + 17f, textHeader)
            canvas.drawText("Birim Fiyat", 410f, yPos + 17f, textHeader)
            canvas.drawText("Tutar", 500f, yPos + 17f, textHeader)

            yPos += 26f

            // Table Rows
            val rowBgEven = Paint().apply { color = Color.WHITE }
            val rowBgOdd = Paint().apply { color = Color.parseColor("#F9FAF8") }

            items.forEachIndexed { index, item ->
                val bg = if (index % 2 == 0) rowBgEven else rowBgOdd
                canvas.drawRect(32f, yPos, pageWidth - 32f, yPos + 24f, bg)
                canvas.drawLine(32f, yPos + 24f, pageWidth - 32f, yPos + 24f, linePaint)

                canvas.drawText("${index + 1}", 42f, yPos + 16f, textMuted)
                val desc = if (item.description.length > 36) item.description.take(36) + "..." else item.description
                canvas.drawText(desc, 66f, yPos + 16f, textDark)
                canvas.drawText("${item.quantity} ${item.unit}", 330f, yPos + 16f, textDark)
                canvas.drawText(Formatters.formatCurrency(item.unitPrice, profile.currencySymbol), 410f, yPos + 16f, textDark)
                canvas.drawText(Formatters.formatCurrency(item.total, profile.currencySymbol), 500f, yPos + 16f, textDark)

                yPos += 24f
            }

            // Totals section
            yPos += 15f
            val totalBoxRight = pageWidth - 32f
            val totalBoxLeft = 350f
            canvas.drawRoundRect(RectF(totalBoxLeft, yPos, totalBoxRight, yPos + 55f), 6f, 6f, boxPaint)

            val totalLabelPaint = Paint().apply {
                color = Color.parseColor("#172033")
                textSize = 11f
                isAntiAlias = true
            }
            val totalValuePaint = Paint().apply {
                color = Color.parseColor("#0F766E")
                textSize = 15f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }

            canvas.drawText("GENEL TOPLAM:", totalBoxLeft + 16f, yPos + 33f, sectionHeaderPaint)
            canvas.drawText(Formatters.formatCurrency(quote.totalAmount, profile.currencySymbol), totalBoxRight - 16f, yPos + 35f, totalValuePaint)

            // Notes Section
            if (quote.notes.isNotBlank()) {
                val notesY = yPos + 75f
                canvas.drawText("Teklif Notları ve Şartlar:", 32f, notesY, sectionHeaderPaint)
                val lines = quote.notes.split("\n")
                var currY = notesY + 16f
                lines.take(4).forEach { line ->
                    canvas.drawText(line, 32f, currY, textMuted)
                    currY += 14f
                }
            }

            // Footer
            val footerY = pageHeight - 40f
            canvas.drawLine(32f, footerY - 10f, pageWidth - 32f, footerY - 10f, linePaint)
            val footerPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 9f
                isAntiAlias = true
            }
            canvas.drawText("Bu teklif Ustaca uygulaması ile hazırlanmıştır.", 32f, footerY, footerPaint)
            val contactFooter = "${profile.businessName} • ${profile.phone} • ${profile.address}"
            canvas.drawText(contactFooter, 32f, footerY + 14f, footerPaint)

            document.finishPage(page)

            // Save PDF file in cache
            val cacheDir = File(context.cacheDir, "quotes")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val pdfFile = File(cacheDir, "Teklif_${quote.quoteNumber.ifEmpty { quote.id.toString() }}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Tahsilat makbuzu (A5 boyutunda, tek sayfa). */
    fun generatePaymentReceiptPdf(
        context: Context,
        profile: BusinessProfile,
        customer: Customer,
        payment: Payment,
        job: Job?
    ): File? {
        return try {
            val document = PdfDocument()
            val w = 420
            val h = 595
            val page = document.startPage(PdfDocument.PageInfo.Builder(w, h, 1).create())
            val c = page.canvas

            val ink = Color.parseColor("#0F766E")
            val banner = Paint().apply { color = ink; isAntiAlias = true }
            val white = Paint().apply { color = Color.WHITE; textSize = 15f; isFakeBoldText = true; isAntiAlias = true }
            val whiteSub = Paint().apply { color = Color.parseColor("#CCFBF1"); textSize = 9.5f; isAntiAlias = true }
            val whiteRight = Paint().apply {
                color = Color.WHITE; textSize = 13f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT; isAntiAlias = true
            }
            val whiteRightSub = Paint().apply {
                color = Color.parseColor("#CCFBF1"); textSize = 9.5f; textAlign = Paint.Align.RIGHT; isAntiAlias = true
            }
            val label = Paint().apply { color = Color.parseColor("#64748B"); textSize = 9.5f; isAntiAlias = true }
            val value = Paint().apply { color = Color.parseColor("#172033"); textSize = 11.5f; isAntiAlias = true }
            val valueBold = Paint().apply { color = Color.parseColor("#172033"); textSize = 11.5f; isFakeBoldText = true; isAntiAlias = true }
            val amountPaint = Paint().apply { color = ink; textSize = 24f; isFakeBoldText = true; isAntiAlias = true }
            val line = Paint().apply { color = Color.parseColor("#E5EAF0"); strokeWidth = 1f }
            val boxPaint = Paint().apply { color = Color.parseColor("#ECFDF5"); isAntiAlias = true }

            c.drawRect(0f, 0f, w.toFloat(), 78f, banner)
            c.drawText(profile.businessName, 24f, 34f, white)
            val sub = listOf(profile.category, profile.phone).filter { it.isNotBlank() }.joinToString(" • ")
            if (sub.isNotBlank()) c.drawText(sub, 24f, 52f, whiteSub)
            c.drawText("TAHSİLAT MAKBUZU", w - 24f, 34f, whiteRight)
            c.drawText("No: TM-${payment.id}", w - 24f, 52f, whiteRightSub)

            var y = 110f
            fun row(l: String, v: String, bold: Boolean = false) {
                c.drawText(l, 24f, y, label)
                y += 15f
                c.drawText(v, 24f, y, if (bold) valueBold else value)
                y += 22f
            }

            row("Tarih", Formatters.formatDateTime(payment.date))
            row("Ödemeyi Yapan", customer.name, bold = true)
            if (customer.phone.isNotBlank()) row("Telefon", customer.phone)
            if (job != null) row("İş", listOf(job.jobCode, job.title).filter { it.isNotBlank() }.joinToString(" - "))
            row("Ödeme Şekli", PaymentMethod.getLabel(payment.paymentMethod))
            if (payment.description.isNotBlank()) row("Açıklama", payment.description)

            y += 4f
            c.drawRoundRect(RectF(24f, y, w - 24f, y + 78f), 10f, 10f, boxPaint)
            c.drawText("Alınan Tutar", 38f, y + 22f, label)
            c.drawText(Formatters.formatCurrency(payment.amount, profile.currencySymbol), 38f, y + 50f, amountPaint)
            val words = Paint().apply { color = Color.parseColor("#0F766E"); textSize = 9f; isAntiAlias = true }
            c.drawText("Yalnız: ${amountToTurkishWords(payment.amount)}", 38f, y + 68f, words)
            y += 110f

            c.drawLine(w - 170f, y + 30f, w - 24f, y + 30f, line)
            c.drawText("Teslim Alan / İmza", w - 170f, y + 44f, label)

            val footerY = h - 34f
            c.drawLine(24f, footerY - 12f, w - 24f, footerY - 12f, line)
            val footer = Paint().apply { color = Color.parseColor("#94A3B8"); textSize = 8f; isAntiAlias = true }
            c.drawText("Bu makbuz Ustaca uygulaması ile hazırlanmıştır. Resmi fatura yerine geçmez.", 24f, footerY, footer)
            val contact = listOf(profile.phone, profile.address).filter { it.isNotBlank() }.joinToString(" • ")
            if (contact.isNotBlank()) c.drawText(contact, 24f, footerY + 12f, footer)

            document.finishPage(page)
            val dir = File(context.cacheDir, "receipts")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "Makbuz_TM-${payment.id}.pdf")
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** 1250.50 -> "Bin İki Yüz Elli Türk Lirası Elli Kuruş" */
    fun amountToTurkishWords(amount: Double): String {
        val totalKurus = Math.round(amount * 100)
        val lira = totalKurus / 100
        val kurus = (totalKurus % 100).toInt()
        val liraText = if (lira == 0L) "Sıfır" else numberToWords(lira)
        return buildString {
            append("$liraText Türk Lirası")
            if (kurus > 0) append(" ${numberToWords(kurus.toLong())} Kuruş")
        }
    }

    private fun numberToWords(n: Long): String {
        val ones = listOf("", "Bir", "İki", "Üç", "Dört", "Beş", "Altı", "Yedi", "Sekiz", "Dokuz")
        val tens = listOf("", "On", "Yirmi", "Otuz", "Kırk", "Elli", "Altmış", "Yetmiş", "Seksen", "Doksan")
        fun hundreds(x: Int): String {
            val h = x / 100
            val t = (x % 100) / 10
            val o = x % 10
            val parts = mutableListOf<String>()
            if (h > 0) parts.add(if (h == 1) "Yüz" else "${ones[h]} Yüz")
            if (t > 0) parts.add(tens[t])
            if (o > 0) parts.add(ones[o])
            return parts.joinToString(" ")
        }
        val scales = listOf("", "Bin", "Milyon", "Milyar", "Trilyon")
        var rest = n
        var i = 0
        val groups = mutableListOf<String>()
        while (rest > 0 && i < scales.size) {
            val g = (rest % 1000).toInt()
            if (g > 0) {
                val text = when {
                    i == 1 && g == 1 -> "Bin"
                    scales[i].isEmpty() -> hundreds(g)
                    else -> "${hundreds(g)} ${scales[i]}"
                }
                groups.add(0, text)
            }
            rest /= 1000
            i++
        }
        return groups.joinToString(" ")
    }

    fun shareFile(context: Context, file: File, mimeType: String = "application/pdf", title: String = "Paylaş") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
