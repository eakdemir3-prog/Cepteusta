package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Quote
import com.example.data.model.QuoteStatus
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.util.Formatters

@Composable
fun QuotesScreen(
    quotes: List<Quote>,
    customers: List<Customer>,
    currencySymbol: String,
    onQuoteClick: (Long) -> Unit,
    onNewQuote: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("TÜMÜ") }

    val customerMap = remember(customers) { customers.associateBy { it.id } }

    val statusOptions = listOf(
        "TÜMÜ" to "Tümü (${quotes.size})",
        QuoteStatus.DRAFT to "Taslak",
        QuoteStatus.SENT to "Gönderildi",
        QuoteStatus.ACCEPTED to "Kabul Edildi",
        QuoteStatus.REJECTED to "Reddedildi",
        QuoteStatus.EXPIRED to "Süresi Doldu"
    )

    val filteredQuotes = quotes.filter { q ->
        val custName = customerMap[q.customerId]?.name ?: ""
        val matchesSearch = searchQuery.isBlank() ||
                q.quoteNumber.contains(searchQuery, ignoreCase = true) ||
                q.title.contains(searchQuery, ignoreCase = true) ||
                custName.contains(searchQuery, ignoreCase = true)

        val matchesStatus = selectedStatus == "TÜMÜ" || q.status == selectedStatus

        matchesSearch && matchesStatus
    }

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = "Fiyat Teklifleri (${filteredQuotes.size})",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewQuote,
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("quotes_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Teklif Hazırla")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Teklif no, başlık veya müşteri ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("quotes_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusOptions.forEach { (status, label) ->
                    val isSelected = selectedStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatus = status },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreenContainer,
                            selectedLabelColor = ForestGreenOnContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredQuotes.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Description,
                    title = if (searchQuery.isNotBlank() || selectedStatus != "TÜMÜ") "Teklif Bulunamadı" else "Henüz Teklif Hazırlanmadı",
                    description = if (searchQuery.isNotBlank()) "Arama kriterlerinizi değiştirerek tekrar deneyebilirsiniz." else "Müşterilerinize kalem kalem fiyat teklifi oluşturup tek tıkla PDF olarak paylaşabilirsiniz.",
                    actionButtonText = if (searchQuery.isBlank() && selectedStatus == "TÜMÜ") "İlk Teklifini Oluştur" else "Filtreyi Temizle",
                    onActionClick = {
                        if (searchQuery.isBlank() && selectedStatus == "TÜMÜ") onNewQuote() else {
                            searchQuery = ""
                            selectedStatus = "TÜMÜ"
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredQuotes, key = { it.id }) { quote ->
                        val customer = customerMap[quote.customerId]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onQuoteClick(quote.id) }
                                .testTag("quote_card_${quote.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = quote.quoteNumber,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ForestGreenPrimary
                                    )
                                    StatusBadge(status = quote.status)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = quote.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${customer?.name ?: "Müşteri"} • Geçerlilik: ${Formatters.formatDate(quote.validUntilDate)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = Formatters.formatCurrency(quote.totalAmount, currencySymbol),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = ForestGreenPrimary
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }
}
