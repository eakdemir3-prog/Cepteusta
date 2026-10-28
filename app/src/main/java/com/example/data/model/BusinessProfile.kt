package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "Ustaca",
    val category: String = "Elektrik & Elektronik",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val logoPath: String? = null,
    val currencySymbol: String = "₺",
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val isFirstSetupDone: Boolean = false,
    val taxNumber: String = "",
    val notes: String = ""
)
