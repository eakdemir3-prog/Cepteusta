package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import com.example.data.model.QuoteStatus
import com.example.data.repository.CepteUstaRepository
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusError
import com.example.util.Formatters
import java.util.Calendar

data class EditableQuoteItem(
    var description: String = "",
    var quantityStr: String = "1",
    var unit: String = "Adet",
    var unitPriceStr: String = ""
) {
    val total: Double
        get() {
            val q = quantityStr.toDoubleOrNull() ?: 1.0
            val p = unitPriceStr.toDoubleOrNull() ?: 0.0
            return q * p
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditQuoteScreen(
    quoteId: Long?,
    initialCustomerId: Long?,
    customers: List<Customer>,
    repository: CepteUstaRepository,
    currencySymbol: String,
    onBack: () -> Unit,
    onQuoteSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCustomer by remember {
        mutableStateOf(customers.find { it.id == initialCustomerId })
    }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var quoteNumber by remember { mutableStateOf("") }
    var validUntilDate by remember {
        mutableStateOf(System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000))
    }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(QuoteStatus.DRAFT) }

    val itemsList = remember {
        mutableStateListOf(EditableQuoteItem(description = "", quantityStr = "1", unit = "Adet", unitPriceStr = ""))
    }

    var titleError by remember { mutableStateOf(false) }
    var customerError by remember { mutableStateOf(false) }

    // Date picker for validity date
    val calendar = Calendar.getInstance().apply { timeInMillis = validUntilDate }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 23, 59, 59)
            validUntilDate = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val grandTotal = itemsList.sumOf { it.total }

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = if (quoteId != null && quoteId > 0) "Teklifi Düzenle" else "Yeni Fiyat Teklifi",
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
                text = "Müşteri *",
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
                        .testTag("quote_customer_dropdown")
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
                                customerError = false
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = false
                },
                label = { Text("Teklif Konusu / Başlığı *") },
                placeholder = { Text("Örn: Daire İçi Komple Elektrik Tesisatı Yenileme") },
                isError = titleError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quote_title_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Validity Date
            Text(
                text = "Teklif Son Geçerlilik Tarihi",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(Formatters.formatDate(validUntilDate), fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Line Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Teklif Kalemleri",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedButton(
                    onClick = {
                        itemsList.add(EditableQuoteItem(description = "", quantityStr = "1", unit = "Adet", unitPriceStr = ""))
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("quote_add_item_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kalem Ekle", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            itemsList.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kalem #${index + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ForestGreenPrimary
                            )
                            if (itemsList.size > 1) {
                                IconButton(
                                    onClick = { itemsList.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Kalemi Sil", tint = StatusError, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        OutlinedTextField(
                            value = item.description,
                            onValueChange = { item.description = it },
                            placeholder = { Text("Hizmet veya malzeme açıklaması") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = item.quantityStr,
                                onValueChange = { item.quantityStr = it },
                                label = { Text("Miktar") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = item.unit,
                                onValueChange = { item.unit = it },
                                label = { Text("Birim") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = item.unitPriceStr,
                                onValueChange = { item.unitPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Birim Fiyat") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.3f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                text = "Tutar: ${Formatters.formatCurrency(item.total, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ForestGreenPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grand Total Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ForestGreenPrimary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Genel Toplam:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = Formatters.formatCurrency(grandTotal, currencySymbol),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = ForestGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Teklif Notları ve Ödeme Şartları") },
                placeholder = { Text("Örn: Malzemeler 1. sınıf olup iş tesliminde %50 bakiye tahsil edilecektir.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
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

                    val quoteItems = itemsList.map {
                        QuoteItem(
                            quoteId = quoteId ?: 0L,
                            description = it.description.ifBlank { "Hizmet Bedeli" },
                            quantity = it.quantityStr.toDoubleOrNull() ?: 1.0,
                            unit = it.unit.ifBlank { "Adet" },
                            unitPrice = it.unitPriceStr.toDoubleOrNull() ?: 0.0,
                            total = it.total
                        )
                    }

                    val quote = Quote(
                        id = quoteId ?: 0L,
                        quoteNumber = quoteNumber,
                        customerId = selectedCustomer!!.id,
                        title = title.trim(),
                        date = System.currentTimeMillis(),
                        validUntilDate = validUntilDate,
                        status = status,
                        notes = notes.trim(),
                        totalAmount = grandTotal
                    )

                    coroutineScope.launch {
                        val savedId = repository.saveQuote(quote, quoteItems)
                        onQuoteSaved(savedId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("quote_save_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (quoteId != null && quoteId > 0) "Değişiklikleri Kaydet" else "Teklifi Kaydet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
