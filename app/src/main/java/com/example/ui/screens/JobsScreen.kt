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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobPriority
import com.example.data.model.JobStatus
import com.example.data.model.JobWithCustomer
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.util.Formatters

@Composable
fun JobsScreen(
    jobs: List<JobWithCustomer>,
    currencySymbol: String,
    onJobClick: (Long) -> Unit,
    onNewJob: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("TÜMÜ") }

    val statusOptions = listOf(
        "TÜMÜ" to "Tümü (${jobs.size})",
        JobStatus.NEW to "Yeni",
        JobStatus.SCHEDULED to "Planlandı",
        JobStatus.IN_PROGRESS to "Devam Eden",
        JobStatus.COMPLETED to "Tamamlandı",
        JobStatus.CANCELLED to "İptal Edildi"
    )

    val filteredJobs = jobs.filter { item ->
        val job = item.job
        val customer = item.customer

        val matchesSearch = searchQuery.isBlank() ||
                job.title.contains(searchQuery, ignoreCase = true) ||
                job.jobCode.contains(searchQuery, ignoreCase = true) ||
                customer.name.contains(searchQuery, ignoreCase = true) ||
                job.category.contains(searchQuery, ignoreCase = true) ||
                job.address.contains(searchQuery, ignoreCase = true)

        val matchesStatus = selectedStatusFilter == "TÜMÜ" || job.status == selectedStatusFilter

        matchesSearch && matchesStatus
    }

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = "İş Emirleri (${filteredJobs.size})"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewJob,
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("jobs_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni İş Ekle")
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
                placeholder = { Text("İş kodu, müşteri adı, başlık veya kategori ara...") },
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
                    .testTag("jobs_search_input"),
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
                statusOptions.forEach { (statusCode, label) ->
                    val isSelected = selectedStatusFilter == statusCode
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = statusCode },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreenContainer,
                            selectedLabelColor = ForestGreenOnContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Job List or Empty State
            if (filteredJobs.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Work,
                    title = if (searchQuery.isNotBlank() || selectedStatusFilter != "TÜMÜ") "Filtreye Uygun İş Bulunamadı" else "Henüz Kayıtlı İş Yok",
                    description = if (searchQuery.isNotBlank()) "Farklı bir arama terimi deneyebilir veya filtreyi temizleyebilirsiniz." else "Yeni bir iş emri oluşturarak iş detaylarını ve fotoğrafları kaydedin.",
                    actionButtonText = if (searchQuery.isBlank() && selectedStatusFilter == "TÜMÜ") "İlk İşini Ekle" else "Filtreyi Temizle",
                    onActionClick = {
                        if (searchQuery.isBlank() && selectedStatusFilter == "TÜMÜ") {
                            onNewJob()
                        } else {
                            searchQuery = ""
                            selectedStatusFilter = "TÜMÜ"
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
                    items(filteredJobs, key = { it.job.id }) { item ->
                        DashboardJobCard(
                            item = item,
                            currencySymbol = currencySymbol,
                            onClick = { onJobClick(item.job.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }
}
