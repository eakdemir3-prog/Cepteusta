package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Customer
import com.example.data.model.JobStatus
import com.example.data.model.JobWithCustomer
import com.example.data.model.Payment
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusErrorContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.ui.theme.WhatsAppGreen
import com.example.util.Formatters
import com.example.util.WhatsAppUtil

/** Bir müşterinin açık alacağı. */
data class ReceivableRow(
    val customer: Customer,
    val billed: Double,
    val paid: Double,
    val balance: Double,
    val openJobCount: Int,
    val oldestUnpaidAt: Long
) {
    val waitingDays: Long
        get() = if (oldestUnpaidAt <= 0L) 0L
        else ((System.currentTimeMillis() - oldestUnpaidAt) / (24L * 60 * 60 * 1000)).coerceAtLeast(0L)
}


/** İptal edilmemiş işlerin tutarı - müşterinin tüm tahsilatları. Müşteri detayındaki hesapla aynı. */
fun buildReceivables(jobs: List<JobWithCustomer>, payments: List<Payment>): List<ReceivableRow> {
    val paymentsByCustomer = payments.groupBy { it.customerId }
    val paymentsByJob = payments.filter { it.jobId != null }.groupBy { it.jobId }
    return jobs
        .filter { it.job.status != JobStatus.CANCELLED }
        .groupBy { it.customer.id }
        .mapNotNull { (_, list) ->
            val customer = list.first().customer
            val billed = list.sumOf { if (it.job.finalPrice > 0) it.job.finalPrice else it.job.estimatedPrice }
            val paid = paymentsByCustomer[customer.id].orEmpty().sumOf { it.amount }
            val balance = billed - paid
            if (balance < 0.01) return@mapNotNull null
            val unpaidJobs = list.filter { jc ->
                val price = if (jc.job.finalPrice > 0) jc.job.finalPrice else jc.job.estimatedPrice
                val jobPaid = paymentsByJob[jc.job.id].orEmpty().sumOf { it.amount }
                price - jobPaid > 0.01
            }
            val oldest = unpaidJobs.minOfOrNull { jc ->
                if (jc.job.scheduledDate > 0L) jc.job.scheduledDate else jc.job.createdAt
            } ?: list.minOf { it.job.createdAt }
            ReceivableRow(
                customer = customer,
                billed = billed,
                paid = paid,
                balance = balance,
                openJobCount = unpaidJobs.size.coerceAtLeast(1),
                oldestUnpaidAt = oldest
            )
        }
        .sortedByDescending { it.balance }
}

@Composable
fun ReceivablesScreen(
    jobs: List<JobWithCustomer>,
    payments: List<Payment>,
    currencySymbol: String,
    businessName: String,
    onCustomerClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(0) } // 0: Tümü, 1: 30+ gün, 2: 60+ gün
    val all = remember(jobs, payments) { buildReceivables(jobs, payments) }
    val rows = when (filter) {
        1 -> all.filter { it.waitingDays >= 30 }
        2 -> all.filter { it.waitingDays >= 60 }
        else -> all
    }
    val total = all.sumOf { it.balance }
    val overdueTotal = all.filter { it.waitingDays >= 30 }.sumOf { it.balance }

    Scaffold(
        topBar = { CepteUstaTopBar(title = "Alacaklar (${all.size})", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "TOPLAM AÇIK ALACAK",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            Formatters.formatCurrency(total, currencySymbol),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (total > 0) StatusWarning else MaterialTheme.colorScheme.onSurface
                        )
                        if (overdueTotal > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "30 günü geçen: ${Formatters.formatCurrency(overdueTotal, currencySymbol)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = StatusError
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Tümü", "30+ gün", "60+ gün").forEachIndexed { index, label ->
                        FilterChip(
                            selected = filter == index,
                            onClick = { filter = index },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreenContainer,
                                selectedLabelColor = ForestGreenOnContainer
                            )
                        )
                    }
                }
            }

            if (rows.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.CheckCircle,
                        title = if (all.isEmpty()) "Bekleyen Alacak Yok" else "Bu filtrede alacak yok",
                        description = if (all.isEmpty()) "Tüm müşterilerinizin ödemeleri tamam. Harika!"
                        else "Seçili süreyi geçen bekleyen ödeme bulunmuyor.",
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                items(rows, key = { it.customer.id }) { row ->
                    ReceivableCard(
                        row = row,
                        currencySymbol = currencySymbol,
                        onClick = { onCustomerClick(row.customer.id) },
                        onWhatsApp = {
                            WhatsAppUtil.open(
                                context,
                                row.customer.phone,
                                WhatsAppUtil.paymentReminder(
                                    row.customer.name,
                                    businessName,
                                    Formatters.formatCurrency(row.balance, currencySymbol)
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceivableCard(
    row: ReceivableRow,
    currencySymbol: String,
    onClick: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val late = row.waitingDays >= 30
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (late) StatusErrorContainer else StatusWarningContainer)
            ) {
                Text(
                    row.customer.name.trim().take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (late) StatusError else StatusWarning
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    row.customer.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    Formatters.formatCurrency(row.balance, currencySymbol),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (late) StatusError else StatusWarning
                )
                val waitText = when {
                    row.waitingDays <= 0L -> "Bugün"
                    else -> "${row.waitingDays} gündür bekliyor"
                }
                Text(
                    "${row.openJobCount} açık iş • $waitText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onWhatsApp,
                enabled = row.customer.phone.isNotBlank(),
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen.copy(alpha = 0.12f))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "WhatsApp ile ödeme hatırlat",
                    tint = WhatsAppGreen
                )
            }
        }
    }
}
