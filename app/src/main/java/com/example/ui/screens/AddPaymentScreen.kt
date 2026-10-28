package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Payment
import com.example.data.model.PaymentMethod
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusError
import com.example.util.Formatters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    prefillJobId: Long?,
    prefillCustomerId: Long?,
    customers: List<Customer>,
    onSavePayment: (Payment) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedCustomer by remember {
        mutableStateOf(customers.find { it.id == prefillCustomerId } ?: customers.firstOrNull())
    }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var amountStr by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var description by remember { mutableStateOf("") }

    var amountError by remember { mutableStateOf(false) }
    var customerError by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 12, 0, 0)
            dateMillis = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = "Tahsilat Ekle",
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

            // Amount
            OutlinedTextField(
                value = amountStr,
                onValueChange = {
                    amountStr = it.filter { c -> c.isDigit() || c == '.' }
                    if (it.isNotBlank()) amountError = false
                },
                label = { Text("Tahsil Edilen Tutar (₺) *") },
                placeholder = { Text("Örn: 2500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payment_amount_input"),
                singleLine = true
            )
            if (amountError) {
                Text(
                    text = "Geçerli bir tutar giriniz.",
                    color = StatusError,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Chips
            Text(
                text = "Ödeme Yöntemi",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    PaymentMethod.CASH to "Nakit",
                    PaymentMethod.CREDIT_CARD to "Kredi Kartı",
                    PaymentMethod.TRANSFER to "Havale / EFT",
                    PaymentMethod.OTHER to "Diğer"
                ).forEach { (method, label) ->
                    val isSelected = selectedMethod == method
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMethod = method },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreenContainer,
                            selectedLabelColor = ForestGreenOnContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date
            Text(
                text = "Tahsilat Tarihi",
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
                Text(Formatters.formatDate(dateMillis), fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama (İsteğe Bağlı)") },
                placeholder = { Text("Örn: Malzeme avansı veya montaj sonu kalan bakiye") },
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
                    val amount = amountStr.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        amountError = true
                        return@Button
                    }

                    val payment = Payment(
                        customerId = selectedCustomer!!.id,
                        jobId = prefillJobId,
                        amount = amount,
                        date = dateMillis,
                        paymentMethod = selectedMethod,
                        description = description.trim()
                    )
                    onSavePayment(payment)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("payment_save_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Tahsilatı Kaydet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
