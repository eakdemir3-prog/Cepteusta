package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.repository.CepteUstaRepository
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AddEditCustomerScreen(
    customerId: Long?,
    repository: CepteUstaRepository,
    onBack: () -> Unit,
    onCustomerSaved: (Long) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var duplicatePhoneWarning by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        if (customerId != null && customerId > 0L) {
            val existing = repository.getCustomerById(customerId)
            if (existing != null) {
                name = existing.name
                phone = existing.phone
                email = existing.email
                address = existing.address
                notes = existing.notes
            }
        }
    }

    LaunchedEffect(phone) {
        if (phone.length >= 7) {
            val duplicate = repository.findCustomerByPhone(phone, customerId ?: 0L)
            duplicatePhoneWarning = (duplicate != null)
        } else {
            duplicatePhoneWarning = false
        }
    }

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = if (customerId != null && customerId > 0L) "Müşteriyi Düzenle" else "Yeni Müşteri Ekle",
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
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Müşteri Adı Soyadı / Firma Adı *") },
                placeholder = { Text("Örn: Mehmet Aydın veya ABC İnşaat") },
                isError = nameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cust_name_input"),
                singleLine = true
            )
            if (nameError) {
                Text(
                    text = "Müşteri adı zorunludur.",
                    color = StatusError,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    if (it.isNotBlank()) phoneError = false
                },
                label = { Text("Telefon Numarası *") },
                placeholder = { Text("Örn: 0532 123 45 67") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cust_phone_input"),
                singleLine = true
            )
            if (phoneError) {
                Text(
                    text = "Telefon numarası zorunludur.",
                    color = StatusError,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            if (duplicatePhoneWarning) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusWarningContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Uyarı: Bu telefon numarasıyla kayıtlı başka bir müşteri bulunmaktadır.",
                        color = StatusWarning,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-Posta Adresi (İsteğe Bağlı)") },
                placeholder = { Text("Örn: musteri@mail.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Müşteri Adresi (İsteğe Bağlı)") },
                placeholder = { Text("İlçe, mahalle, cadde, sokak, bina ve daire no") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Müşteri İle İlgili Özel Notlar") },
                placeholder = { Text("İş tercihleri, fatura talebi veya hatırlatmalar...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (phone.isBlank()) {
                        phoneError = true
                        return@Button
                    }

                    val cust = Customer(
                        id = customerId ?: 0L,
                        name = name.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        address = address.trim(),
                        notes = notes.trim()
                    )

                    coroutineScope.launch {
                        val id = repository.saveCustomer(cust)
                        onCustomerSaved(id)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("cust_save_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (customerId != null && customerId > 0L) "Değişiklikleri Kaydet" else "Müşteriyi Kaydet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
