package com.example.ui.screens

import android.content.Intent
import com.example.util.WhatsAppUtil
import com.example.ui.components.WhatsAppTemplate
import com.example.ui.components.WhatsAppButton
import androidx.compose.runtime.produceState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.JobDetailBundle
import com.example.data.model.JobPhoto
import com.example.data.model.JobPriority
import com.example.data.model.JobStatus
import com.example.data.model.PaymentMethod
import com.example.data.repository.CepteUstaRepository
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.util.Formatters
import java.io.File
import java.io.FileOutputStream

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: Long,
    repository: CepteUstaRepository,
    currencySymbol: String,
    onBack: () -> Unit,
    onEditJob: (Long) -> Unit,
    onAddPaymentForJob: (Long, Long) -> Unit,
    onJobDeleted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var bundle by remember { mutableStateOf<JobDetailBundle?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedPhotoCategory by remember { mutableStateOf("ONCE") } // ONCE, SONRA
    val businessName by produceState(initialValue = "") {
        value = runCatching { repository.getProfile().businessName }.getOrDefault("")
    }

    suspend fun refreshBundle() {
        bundle = repository.getJobBundle(jobId)
    }

    LaunchedEffect(jobId) {
        refreshBundle()
    }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val photosDir = File(context.filesDir, "job_photos")
                if (!photosDir.exists()) photosDir.mkdirs()
                val photoFile = File(photosDir, "job_${jobId}_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(photoFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                coroutineScope.launch {
                    repository.addJobPhoto(
                        JobPhoto(
                            jobId = jobId,
                            filePath = photoFile.absolutePath,
                            photoType = selectedPhotoCategory
                        )
                    )
                    bundle = repository.getJobBundle(jobId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val currentBundle = bundle

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = currentBundle?.job?.jobCode ?: "İş Detayı",
                onBack = onBack,
                actions = {
                    if (currentBundle != null) {
                        IconButton(
                            onClick = { onEditJob(jobId) },
                            modifier = Modifier.testTag("job_detail_edit_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("job_detail_delete_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = StatusError)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (currentBundle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("İş kaydı yükleniyor...")
            }
        } else {
            val job = currentBundle.job
            val customer = currentBundle.customer
            val photos = currentBundle.photos
            val payments = currentBundle.payments

            val filteredPhotos = photos.filter { it.photoType == selectedPhotoCategory }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Title & Status Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = job.status)
                            PriorityBadge(priority = job.priority)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = job.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kategori: ${job.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestGreenPrimary,
                            fontWeight = FontWeight.Medium
                        )

                        if (job.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = job.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (job.scheduledDate > 0L) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Randevu: ${Formatters.formatDate(job.scheduledDate)} ${job.scheduledTime}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Status Changer
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "İş Durumunu Güncelle",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                JobStatus.IN_PROGRESS to "Devam",
                                JobStatus.COMPLETED to "Tamamla",
                                JobStatus.CANCELLED to "İptal"
                            ).forEach { (status, label) ->
                                val isSelected = job.status == status
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.updateJobStatus(jobId, status)
                                            bundle = repository.getJobBundle(jobId)
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) ForestGreenContainer else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ForestGreenOnContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customer Info Card (Call & Map intents)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = ForestGreenOnContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = customer.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Call button
                            IconButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${customer.phone}")
                                    }
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess.copy(alpha = 0.1f))
                                    .testTag("job_detail_call_btn")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Ara", tint = StatusSuccess)
                            }
                        }

                        val addr = job.address.ifBlank { customer.address }
                        if (addr.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(addr)}"))
                                            context.startActivity(mapIntent)
                                        } catch (_: Exception) {}
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = addr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        val waTotal = Formatters.formatCurrency(
                            if (job.finalPrice > 0) job.finalPrice else job.estimatedPrice,
                            currencySymbol
                        )
                        val waRemaining = if (currentBundle.remainingBalance > 0.0) {
                            Formatters.formatCurrency(currentBundle.remainingBalance, currencySymbol)
                        } else null
                        WhatsAppButton(
                            phone = customer.phone,
                            templates = buildList {
                                add(
                                    WhatsAppTemplate(
                                        "Randevu hatırlatma",
                                        WhatsAppUtil.appointmentReminder(
                                            customer.name,
                                            businessName,
                                            job.title,
                                            Formatters.formatDate(job.scheduledDate),
                                            job.scheduledTime
                                        )
                                    )
                                )
                                add(WhatsAppTemplate("Yoldayız", WhatsAppUtil.onTheWay(customer.name, businessName)))
                                add(
                                    WhatsAppTemplate(
                                        "İş tamamlandı",
                                        WhatsAppUtil.jobCompleted(customer.name, businessName, job.title, waTotal, waRemaining)
                                    )
                                )
                                if (waRemaining != null) {
                                    add(
                                        WhatsAppTemplate(
                                            "Ödeme hatırlatma",
                                            WhatsAppUtil.paymentReminder(customer.name, businessName, waRemaining)
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Financial Balance & Payment Action
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ÜCRET VE TAHSİLAT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Toplam Bedel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val price = if (job.finalPrice > 0) job.finalPrice else job.estimatedPrice
                                Text(
                                    text = Formatters.formatCurrency(price, currencySymbol),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column {
                                Text(
                                    text = "Alınan Tahsilat",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatCurrency(currentBundle.totalPaid, currencySymbol),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = StatusSuccess
                                )
                            }
                            Column {
                                Text(
                                    text = "Kalan Bakiye",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatCurrency(currentBundle.remainingBalance, currencySymbol),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (currentBundle.remainingBalance > 0) StatusWarning else StatusSuccess
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onAddPaymentForJob(job.id, customer.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("job_detail_add_payment_btn")
                        ) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tahsilat Ekle", fontWeight = FontWeight.Bold)
                        }

                        if (payments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            payments.forEach { pay ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${Formatters.formatDate(pay.date)} • ${PaymentMethod.getLabel(pay.paymentMethod)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Formatters.formatCurrency(pay.amount, currencySymbol),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = StatusSuccess
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Before / After Photos Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "İş Fotoğrafları (${photos.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.testTag("job_photo_add_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Fotoğraf Ekle",
                                    tint = ForestGreenPrimary
                                )
                            }
                        }

                        // Tab: İş Öncesi / İş Sonrası
                        TabRow(
                            selectedTabIndex = if (selectedPhotoCategory == "ONCE") 0 else 1,
                            containerColor = Color.Transparent,
                            contentColor = ForestGreenPrimary
                        ) {
                            Tab(
                                selected = selectedPhotoCategory == "ONCE",
                                onClick = { selectedPhotoCategory = "ONCE" },
                                text = { Text("İş Öncesi (${photos.count { it.photoType == "ONCE" }})") }
                            )
                            Tab(
                                selected = selectedPhotoCategory == "SONRA",
                                onClick = { selectedPhotoCategory = "SONRA" },
                                text = { Text("İş Sonrası (${photos.count { it.photoType == "SONRA" }})") }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (filteredPhotos.isEmpty()) {
                            Text(
                                text = "Bu aşamada henüz fotoğraf eklenmedi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(filteredPhotos, key = { it.id }) { photo ->
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(File(photo.filePath)),
                                            contentDescription = "İş Fotoğrafı",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Delete icon
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    repository.deleteJobPhoto(photo.id)
                                                    bundle = repository.getJobBundle(jobId)
                                                }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(28.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Sil",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Delete Confirm Dialog
        ConfirmDialog(
            show = showDeleteConfirm,
            title = "İş Kaydını Sil",
            message = "Bu iş kaydını silmek istediğinize emin misiniz? İşe ait tüm fotoğraflar silinecektir. Finansal tutarlılık için tamamlanan işleri silmek yerine 'İptal' durumuna alabilirsiniz.",
            onConfirm = {
                coroutineScope.launch {
                    repository.deleteJob(jobId)
                    onJobDeleted()
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
