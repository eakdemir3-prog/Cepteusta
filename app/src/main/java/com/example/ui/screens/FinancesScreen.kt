package com.example.ui.screens

import com.example.data.model.JobWithCustomer
import com.example.data.model.BusinessProfile
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Payment
import com.example.data.model.PaymentMethod
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.util.CsvExportUtil
import com.example.util.Formatters
import com.example.util.PdfExportUtil

@Composable
fun FinancesScreen(
    payments: List<Payment>,
    expenses: List<Expense>,
    customers: List<Customer>,
    currencySymbol: String,
    onAddPayment: () -> Unit,
    onAddExpense: () -> Unit,
    onDeletePayment: (Long) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onBack: () -> Unit,
    profile: BusinessProfile? = null,
    jobs: List<JobWithCustomer> = emptyList()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Tahsilatlar, 1: Giderler
    val customerMap = remember(customers) { customers.associateBy { it.id } }
    val jobMap = remember(jobs) { jobs.associate { it.job.id to it.job } }

    val totalCollected = payments.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val netBalance = totalCollected - totalExpense

    var itemToDeleteType by remember { mutableStateOf<String?>(null) } // "PAYMENT" or "EXPENSE"
    var itemToDeleteId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = "Gelir & Gider Takibi",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            val csvFile = CsvExportUtil.exportFinancesToCsv(context, payments, expenses)
                            PdfExportUtil.shareFile(context, csvFile, mimeType = "text/csv")
                        },
                        modifier = Modifier.testTag("finance_export_csv_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "CSV Dışa Aktar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Net Balance Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NET KASA DURUMU",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatCurrency(netBalance, currencySymbol),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (netBalance >= 0) ForestGreenPrimary else StatusError
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Tahsilat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    Formatters.formatCurrency(totalCollected, currencySymbol),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = StatusSuccess
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = StatusError, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Toplam Gider", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    Formatters.formatCurrency(totalExpense, currencySymbol),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = StatusError
                                )
                            }
                        }
                    }
                }
            }

            // Quick Action Buttons (Tahsilat Ekle, Gider Ekle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("finance_add_payment_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tahsilat Ekle", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onAddExpense,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("finance_add_expense_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = StatusError)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gider Ekle", color = StatusError, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = ForestGreenPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Tahsilatlar (${payments.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Giderler (${expenses.size})") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                if (payments.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.AttachMoney,
                        title = "Henüz Tahsilat Yok",
                        description = "Müşterilerinizden veya işlerden aldığınız ödemeleri kaydedin.",
                        actionButtonText = "Tahsilat Ekle",
                        onActionClick = onAddPayment
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(payments, key = { it.id }) { pay ->
                            val customer = customerMap[pay.customerId]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = customer?.name ?: "Müşteri Tahsilatı",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${Formatters.formatDate(pay.date)} • ${PaymentMethod.getLabel(pay.paymentMethod)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (pay.description.isNotBlank()) {
                                            Text(
                                                text = pay.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "+${Formatters.formatCurrency(pay.amount, currencySymbol)}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = StatusSuccess
                                        )
                                        if (profile != null && customer != null) {
                                            IconButton(onClick = {
                                                val file = PdfExportUtil.generatePaymentReceiptPdf(
                                                    context, profile, customer, pay, pay.jobId?.let { jobMap[it] }
                                                )
                                                if (file != null) PdfExportUtil.shareFile(context, file, title = "Makbuzu Paylaş")
                                            }) {
                                                Icon(Icons.Default.Receipt, contentDescription = "Makbuz", tint = ForestGreenPrimary, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        IconButton(onClick = {
                                            itemToDeleteType = "PAYMENT"
                                            itemToDeleteId = pay.id
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            } else {
                if (expenses.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Receipt,
                        title = "Henüz Gider Kaydı Yok",
                        description = "Malzeme, yakıt, fatura veya ekipman harcamalarınızı kaydedin.",
                        actionButtonText = "Gider Ekle",
                        onActionClick = onAddExpense
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(expenses, key = { it.id }) { exp ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exp.category,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = Formatters.formatDate(exp.date),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (exp.description.isNotBlank()) {
                                            Text(
                                                text = exp.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "-${Formatters.formatCurrency(exp.amount, currencySymbol)}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = StatusError
                                        )
                                        IconButton(onClick = {
                                            itemToDeleteType = "EXPENSE"
                                            itemToDeleteId = exp.id
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }

        ConfirmDialog(
            show = itemToDeleteId != null,
            title = if (itemToDeleteType == "PAYMENT") "Tahsilatı Sil" else "Gider Kaydını Sil",
            message = "Bu finans hareketini silmek istediğinize emin misiniz?",
            onConfirm = {
                val id = itemToDeleteId
                if (id != null) {
                    if (itemToDeleteType == "PAYMENT") onDeletePayment(id)
                    else onDeleteExpense(id)
                }
                itemToDeleteId = null
                itemToDeleteType = null
            },
            onDismiss = {
                itemToDeleteId = null
                itemToDeleteType = null
            }
        )
    }
}
