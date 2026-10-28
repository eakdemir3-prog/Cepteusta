package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.BusinessProfile
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import java.io.File
import java.io.FileOutputStream

val TRADE_CATEGORIES = listOf(
    "Elektrik & Elektronik",
    "Sıhhi Tesisat & Su",
    "Teknik Servis & Tamir",
    "Klima & Isıtma (Kombi)",
    "Boya & Badana & Alçı",
    "Marangoz & Mobilya Montaj",
    "Temizlik & İlaçlama",
    "Oto Servis & Bakım",
    "Diğer Hizmetler"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    initialProfile: BusinessProfile,
    onComplete: (BusinessProfile) -> Unit
) {
    val context = LocalContext.current
    var businessName by remember { mutableStateOf(initialProfile.businessName) }
    var selectedCategory by remember { mutableStateOf(initialProfile.category) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf(initialProfile.phone) }
    var address by remember { mutableStateOf(initialProfile.address) }
    var logoPath by remember { mutableStateOf(initialProfile.logoPath) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val logoDir = File(context.filesDir, "logos")
                if (!logoDir.exists()) logoDir.mkdirs()
                val destFile = File(logoDir, "business_logo_${System.currentTimeMillis()}.png")
                val outputStream = FileOutputStream(destFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                logoPath = destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header
            Text(
                text = "Cepte Usta'ya Hoş Geldiniz",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = ForestGreenPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "İşletmenizin profilini oluşturun. Müşterilerinize sunacağınız tekliflerde ve fişlerde bu bilgiler yer alacaktır.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Logo Picker
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, ForestGreenPrimary.copy(alpha = 0.5f), CircleShape)
                            .clickable { photoPickerLauncher.launch("image/*") }
                            .testTag("onboarding_logo_picker"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!logoPath.isNullOrBlank() && File(logoPath!!).exists()) {
                            Image(
                                painter = rememberAsyncImagePainter(File(logoPath!!)),
                                contentDescription = "İşletme Logosu",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Logo Ekle",
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (logoPath.isNullOrBlank()) "İşletme Logosu Ekle (İsteğe Bağlı)" else "Logoyu Değiştir",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = ForestGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Business Name
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("İşletme / Usta Adı *") },
                placeholder = { Text("Örn: Usta Elektrik & Aydınlatma") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_business_name"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Trade Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Meslek / Faaliyet Alanı") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    TRADE_CATEGORIES.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("İletişim Numarası") },
                placeholder = { Text("Örn: 0532 123 45 67") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_phone"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("İş Yeri / Bölge Adresi") },
                placeholder = { Text("Örn: Kadıköy, İstanbul") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Button(
                onClick = {
                    val finalName = businessName.ifBlank { "Cepte Usta" }
                    val updated = initialProfile.copy(
                        businessName = finalName,
                        category = selectedCategory,
                        phone = phone,
                        address = address,
                        logoPath = logoPath,
                        isFirstSetupDone = true
                    )
                    onComplete(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("onboarding_save_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Kurulumu Tamamla ve Başla",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    onComplete(initialProfile.copy(isFirstSetupDone = true))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("onboarding_skip_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Şimdilik Atla (Sonradan Düzenle)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
