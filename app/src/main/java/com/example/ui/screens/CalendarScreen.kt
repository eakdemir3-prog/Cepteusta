package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobWithCustomer
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.util.Formatters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    jobs: List<JobWithCustomer>,
    currencySymbol: String,
    onJobClick: (Long) -> Unit,
    onNewJob: () -> Unit
) {
    val turkishLocale = Locale("tr", "TR")
    val cal = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var displayedMonthCal by remember { mutableStateOf(Calendar.getInstance()) }
    var viewMode by remember { mutableStateOf(0) } // 0: Gün / Hafta bandı, 1: Ay

    // Normalizing date to start of day
    fun getNormalizedStartOfDay(millis: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = millis
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    val selectedDayStart = getNormalizedStartOfDay(selectedDateMillis)
    val selectedDayEnd = selectedDayStart + (24 * 60 * 60 * 1000) - 1

    val dayJobs = jobs.filter {
        val jobDay = getNormalizedStartOfDay(it.job.scheduledDate)
        jobDay == selectedDayStart
    }.sortedBy { it.job.scheduledTime }

    // Check for conflicting appointments (same day & same scheduledTime)
    val timeConflicts = dayJobs.groupBy { it.job.scheduledTime }.filter { it.key.isNotBlank() && it.value.size > 1 }

    Scaffold(
        topBar = {
            CepteUstaTopBar(title = "İş Takvimi ve Randevular")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewJob,
                containerColor = ForestGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("calendar_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Randevu Ekle")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Month navigation header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = displayedMonthCal.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    displayedMonthCal = newCal
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Önceki Ay")
                }

                val monthYearFormat = SimpleDateFormat("MMMM yyyy", turkishLocale)
                Text(
                    text = monthYearFormat.format(displayedMonthCal.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = {
                    val newCal = displayedMonthCal.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    displayedMonthCal = newCal
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Sonraki Ay")
                }
            }

            // Horizontal Days Band
            val daysInMonth = displayedMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentYear = displayedMonthCal.get(Calendar.YEAR)
            val currentMonth = displayedMonthCal.get(Calendar.MONTH)

            val daysList = (1..daysInMonth).map { day ->
                val dayCal = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dayCal
            }

            val dayNameFormat = SimpleDateFormat("EEE", turkishLocale)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysList) { dayCal ->
                    val dayStart = dayCal.timeInMillis
                    val isSelected = selectedDayStart == dayStart
                    val jobCountOnDay = jobs.count { getNormalizedStartOfDay(it.job.scheduledDate) == dayStart }

                    Card(
                        modifier = Modifier
                            .clickable { selectedDateMillis = dayStart }
                            .width(52.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayNameFormat.format(dayCal.time).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Dot if has jobs
                            if (jobCountOnDay > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else ForestGreenPrimary)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conflicting appointment warning
            if (timeConflicts.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusWarningContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusWarning,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Dikkat: Bugün aynı saate (${timeConflicts.keys.joinToString(", ")}) planlanmış birden fazla randevu bulunuyor.",
                            color = StatusWarning,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Selected Day Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${Formatters.formatDate(selectedDayStart)} Randevuları (${dayJobs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Day Jobs List
            if (dayJobs.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CalendarMonth,
                    title = "Bugün Randevu Yok",
                    description = "Seçilen tarih için kayıtlı iş veya servis randevusu bulunmuyor.",
                    actionButtonText = "Yeni Randevu Ekle",
                    onActionClick = onNewJob
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dayJobs, key = { it.job.id }) { item ->
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
