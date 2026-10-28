package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Job
import com.example.data.model.JobPriority
import com.example.data.model.JobStatus
import com.example.data.repository.CepteUstaRepository
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusError
import com.example.util.Formatters
import java.util.Calendar

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditJobScreen(
    jobId: Long?,
    initialCustomerId: Long?,
    customers: List<Customer>,
    repository: CepteUstaRepository,
    onBack: () -> Unit,
    onJobSaved: (Long) -> Unit,
    onAddNewCustomer: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCustomer by remember {
        mutableStateOf(customers.find { it.id == initialCustomerId })
    }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Genel Tamirat") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var scheduledDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var scheduledTime by remember { mutableStateOf("10:00") }
    var priority by remember { mutableStateOf(JobPriority.NORMAL) }
    var status by remember { mutableStateOf(JobStatus.NEW) }
    var estimatedPriceStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var jobCode by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf(false) }
    var customerError by remember { mutableStateOf(false) }

    // Load existing job if editing
    LaunchedEffect(jobId) {
        if (jobId != null && jobId > 0L) {
            val existing = repository.getJobBundle(jobId)?.job
            if (existing != null) {
                jobCode = existing.jobCode
                title = existing.title
                description = existing.description
                selectedCategory = existing.category
                address = existing.address
                scheduledDate = existing.scheduledDate
                scheduledTime = existing.scheduledTime
                priority = existing.priority
                status = existing.status
                if (existing.estimatedPrice > 0.0) {
                    estimatedPriceStr = existing.estimatedPrice.toString()
                }
                notes = existing.notes
                selectedCustomer = customers.find { it.id == existing.customerId }
            }
        }
    }

    // Date Picker Dialog
    val calendar = Calendar.getInstance().apply { timeInMillis = scheduledDate }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 0, 0, 0)
            scheduledDate = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Time Picker Dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formatted = String.format("%02d:%02d", hourOfDay, minute)
            scheduledTime = formatted
        },
        10, 0, true
    )

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = if (jobId != null && jobId > 0) "İşi Düzenle" else "Yeni İş Oluştur",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Customer Selector
            Text(
                text = "Müşteri Seçimi *",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = customerDropdownExpanded,
                onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.let { "${it.name} (${it.phone})" } ?: "Müşteri Seçiniz",
                    onValueChange = {},
                    readOnly = true,
                    isError = customerError,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("job_customer_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false }
                ) {
                    customers.forEach { cust ->
                        DropdownMenuItem(
                            text = { Text("${cust.name} - ${cust.phone}") },
                            onClick = {
                                selectedCustomer = cust
                                if (address.isBlank() && cust.address.isNotBlank()) {
                                    address = cust.address
                                }
                                customerError = false
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (customerError) {
                Text(
                    text = "Lütfen bir müşteri seçin veya yeni müşteri ekleyin.",
                    color = StatusError,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onAddNewCustomer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("job_add_new_cust_btn")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yeni Müşteri Ekle", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Job Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = false
                },
                label = { Text("İş Başlığı *") },
                placeholder = { Text("Örn: Kombi arızası veya banyo batarya montajı") },
                isError = titleError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("job_title_input"),
                singleLine = true
            )
            if (titleError) {
                Text(
                    text = "İş başlığı zorunludur.",
                    color = StatusError,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selector
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hizmet Kategorisi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    TRADE_CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Yapılacak İş Açıklaması") },
                placeholder = { Text("Detaylar, kullanılacak malzemeler ve arıza tanımı...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("İşin Yapılacağı Adres") },
                placeholder = { Text("İlçe, mahalle, sokak, daire no") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date & Time Pickers
            Text(
                text = "Planlanan Randevu Tarihi ve Saati",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Formatters.formatDate(scheduledDate), fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(scheduledTime.ifBlank { "Saat Seç" }, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority
            Text(
                text = "Öncelik Derecesi",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    JobPriority.NORMAL to "Normal",
                    JobPriority.HIGH to "Yüksek",
                    JobPriority.URGENT to "Acil"
                ).forEach { (prio, label) ->
                    val isSelected = priority == prio
                    FilterChip(
                        selected = isSelected,
                        onClick = { priority = prio },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreenContainer,
                            selectedLabelColor = ForestGreenOnContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Estimated Price
            OutlinedTextField(
                value = estimatedPriceStr,
                onValueChange = { estimatedPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Tahmini Ücret (₺)") },
                placeholder = { Text("Örn: 1500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("job_price_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Usta Özel Notları") },
                placeholder = { Text("Müşteri talepleri veya özel hatırlatmalar...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = {
                    if (selectedCustomer == null) {
                        customerError = true
                        return@Button
                    }
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }

                    val price = estimatedPriceStr.toDoubleOrNull() ?: 0.0
                    val jobToSave = Job(
                        id = jobId ?: 0L,
                        jobCode = jobCode,
                        customerId = selectedCustomer!!.id,
                        title = title.trim(),
                        description = description.trim(),
                        category = selectedCategory,
                        address = address.trim(),
                        scheduledDate = scheduledDate,
                        scheduledTime = scheduledTime,
                        priority = priority,
                        status = status,
                        estimatedPrice = price,
                        finalPrice = if (jobId != null && jobId > 0L) price else 0.0,
                        notes = notes.trim()
                    )

                    coroutineScope.launch {
                        val savedId = repository.saveJob(jobToSave)
                        onJobSaved(savedId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("job_save_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (jobId != null && jobId > 0) "Değişiklikleri Kaydet" else "İş Emrini Oluştur",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
