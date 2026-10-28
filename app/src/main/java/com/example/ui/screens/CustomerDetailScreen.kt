package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import com.example.util.WhatsAppUtil
import com.example.ui.components.WhatsAppTemplate
import com.example.ui.components.WhatsAppButton
import androidx.compose.runtime.produceState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomerWithDetails
import com.example.data.model.PaymentMethod
import com.example.data.repository.CepteUstaRepository
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.util.Formatters

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CustomerDetailScreen(
    customerId: Long,
    repository: CepteUstaRepository,
    currencySymbol: String,
    onBack: () -> Unit,
    onEditCustomer: (Long) -> Unit,
    onNewJobForCustomer: (Long) -> Unit,
    onJobClick: (Long) -> Unit,
    onQuoteClick: (Long) -> Unit,
    onCustomerDeleted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val customerBundle by repository.getCustomerWithDetails(customerId).collectAsStateWithLifecycle(initialValue = null)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: İşler, 1: Teklifler, 2: Tahsilatlar
    val businessName by produceState(initialValue = "") {
        value = runCatching { repository.getProfile().businessName }.getOrDefault("")
    }

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = customerBundle?.customer?.name ?: "Müşteri Detayı",
                onBack = onBack,
                actions = {
                    if (customerBundle != null) {
                        IconButton(
                            onClick = { onEditCustomer(customerId) },
                            modifier = Modifier.testTag("cust_detail_edit_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("cust_detail_delete_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = StatusError)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val bundle = customerBundle
        if (bundle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Müşteri bilgisi yükleniyor...")
            }
        } else {
            val customer = bundle.customer

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Profile Header Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customer.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ForestGreenOnContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Kayıt: ${Formatters.formatDate(customer.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Phone Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                    context.startActivity(dial)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ForestGreenPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        WhatsAppButton(
                            phone = customer.phone,
                            templates = buildList {
                                if (bundle.balance > 0.0) {
                                    add(
                                        WhatsAppTemplate(
                                            "Ödeme hatırlatma",
                                            WhatsAppUtil.paymentReminder(
                                                customer.name,
                                                businessName,
                                                Formatters.formatCurrency(bundle.balance, currencySymbol)
                                            )
                                        )
                                    )
                                }
                                add(WhatsAppTemplate("Yoldayız", WhatsAppUtil.onTheWay(customer.name, businessName)))
                                add(WhatsAppTemplate("Teşekkür / tavsiye", WhatsAppUtil.thankYou(customer.name, businessName)))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (customer.email.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = customer.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (customer.address.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val map = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(customer.address)}"))
                                            context.startActivity(map)
                                        } catch (_: Exception) {}
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = customer.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (customer.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = customer.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customer Financial Summary
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Toplam İş Tutarı", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                Formatters.formatCurrency(bundle.totalBilled, currencySymbol),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            Text("Tahsil Edilen", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                Formatters.formatCurrency(bundle.totalPaid, currencySymbol),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatusSuccess
                            )
                        }
                        Column {
                            Text("Kalan Bakiye", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                Formatters.formatCurrency(bundle.balance, currencySymbol),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (bundle.balance > 0) StatusWarning else StatusSuccess
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: New Job For Customer
                Button(
                    onClick = { onNewJobForCustomer(customerId) },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bu Müşteriye Yeni İş Ekle", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs: İşler, Teklifler, Tahsilatlar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = ForestGreenPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("İşler (${bundle.jobs.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Teklifler (${bundle.quotes.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Tahsilat (${bundle.payments.size})") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        if (bundle.jobs.isEmpty()) {
                            Text("Bu müşteriye ait kayıtlı iş bulunmuyor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                        } else {
                            bundle.jobs.forEach { job ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onJobClick(job.id) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(job.jobCode, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ForestGreenPrimary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                StatusBadge(status = job.status)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(job.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        val price = if (job.finalPrice > 0) job.finalPrice else job.estimatedPrice
                                        Text(Formatters.formatCurrency(price, currencySymbol), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ForestGreenPrimary)
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        if (bundle.quotes.isEmpty()) {
                            Text("Bu müşteriye ait teklif bulunmuyor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                        } else {
                            bundle.quotes.forEach { quote ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onQuoteClick(quote.id) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(quote.quoteNumber, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ForestGreenPrimary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                StatusBadge(status = quote.status)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(quote.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Text(Formatters.formatCurrency(quote.totalAmount, currencySymbol), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ForestGreenPrimary)
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        if (bundle.payments.isEmpty()) {
                            Text("Bu müşteriden henüz tahsilat alınmadı.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                        } else {
                            bundle.payments.forEach { pay ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(Formatters.formatDate(pay.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(PaymentMethod.getLabel(pay.paymentMethod), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                                            if (pay.description.isNotBlank()) {
                                                Text(pay.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Text(Formatters.formatCurrency(pay.amount, currencySymbol), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = StatusSuccess)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        ConfirmDialog(
            show = showDeleteConfirm,
            title = "Müşteriyi Sil",
            message = "Bu müşteriyi silmek istediğinize emin misiniz? Müşteriye ait iş veya ödeme kayıtları bulunuyorsa silme işlemi engellenebilir.",
            onConfirm = {
                coroutineScope.launch {
                    repository.deleteCustomer(customerId)
                    onCustomerDeleted()
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
