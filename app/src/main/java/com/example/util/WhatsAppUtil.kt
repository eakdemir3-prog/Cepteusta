package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** Müşteriye WhatsApp'tan hazır mesaj gönderme yardımcıları (Türkiye numaraları için). */
object WhatsAppUtil {

    /** 0532..., 532..., +90 532..., 90532... biçimlerini 905xxxxxxxxx'e çevirir. */
    fun normalizePhone(raw: String): String? {
        var digits = raw.filter { it.isDigit() }
        if (digits.startsWith("00")) digits = digits.drop(2)
        digits = when {
            digits.startsWith("90") && digits.length == 12 -> digits
            digits.startsWith("0") && digits.length == 11 -> "9$digits"
            digits.length == 10 -> "90$digits"
            else -> digits
        }
        return if (digits.length >= 10) digits else null
    }

    fun open(context: Context, phone: String, message: String) {
        val number = normalizePhone(phone)
        if (number == null) {
            Toast.makeText(context, "Müşterinin telefon numarası geçersiz.", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse("https://wa.me/$number?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp veya tarayıcı bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firstName(name: String): String = name.trim().split(" ").firstOrNull().orEmpty().ifBlank { name }

    private fun signature(businessName: String): String =
        if (businessName.isBlank()) "" else "\n\n$businessName"

    fun appointmentReminder(customerName: String, businessName: String, jobTitle: String, date: String, time: String): String {
        val whenText = listOf(date, time).filter { it.isNotBlank() && it != "-" }.joinToString(" saat ")
        return "Merhaba ${firstName(customerName)}, \"$jobTitle\" işi için randevunuzu hatırlatmak isteriz" +
            (if (whenText.isNotBlank()) ": $whenText." else ".") +
            " Bir değişiklik olursa lütfen bize haber verin." + signature(businessName)
    }

    fun onTheWay(customerName: String, businessName: String): String =
        "Merhaba ${firstName(customerName)}, yola çıktık, kısa süre içinde adresinizde olacağız." + signature(businessName)

    fun jobCompleted(customerName: String, businessName: String, jobTitle: String, total: String, remaining: String?): String {
        val rest = if (remaining != null) " Kalan ödeme tutarı: $remaining." else ""
        return "Merhaba ${firstName(customerName)}, \"$jobTitle\" işiniz tamamlanmıştır. Toplam tutar: $total.$rest" +
            " Bizi tercih ettiğiniz için teşekkür ederiz." + signature(businessName)
    }

    fun paymentReminder(customerName: String, businessName: String, amount: String): String =
        "Merhaba ${firstName(customerName)}, yaptığımız işlerden kalan $amount tutarında ödemeniz bulunmaktadır." +
            " Uygun olduğunuzda ödeme yapabilirseniz seviniriz. Teşekkürler." + signature(businessName)

    fun thankYou(customerName: String, businessName: String): String =
        "Merhaba ${firstName(customerName)}, bizi tercih ettiğiniz için teşekkür ederiz. Memnun kaldıysanız çevrenize önermenizi rica ederiz." +
            signature(businessName)
}
