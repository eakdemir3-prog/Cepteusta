package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.model.ActivityLog
import com.example.data.model.BusinessProfile
import com.example.data.model.DashboardStats
import com.example.data.model.JobStatus
import com.example.data.model.JobWithCustomer
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusErrorContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.util.Formatters
import java.io.File

@Composable
fun DashboardScreen(
    profile: BusinessProfile,
    stats: DashboardStats,
    jobs: List<JobWithCustomer>,
    recentActivity: List<ActivityLog>,
    onJobClick: (Long) -> Unit,
    onNewJob: () -> Unit,
    onNewCustomer: () -> Unit,
    onNewQuote: () -> Unit,
    onAddPayment: () -> Unit,
    onSeeAllJobs: () -> Unit,
    onProfileClick: () -> Unit,
    onReceivablesClick: () -> Unit = {}
) {
    val todayStart = Formatters.getTodayStartMillis()
    val todayEnd = Formatters.getTodayEndMillis()
    val todayJobs = jobs.filter { it.job.scheduledDate in todayStart..todayEnd }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(ForestGreenContainer)
                            .clickable { onProfileClick() }
                            .testTag("dashboard_profile_logo"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profile.logoPath.isNullOrBlank() && File(profile.logoPath).exists()) {
                            Image(
                                painter = rememberAsyncImagePainter(File(profile.logoPath)),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_brand_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = profile.businessName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = profile.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestGreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.testTag("dashboard_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ayarlar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Financial & Job Stats Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BU AYIN FİNANSAL ÖZETİ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FinanceStatItem(
                            title = "Tahsil Edilen",
                            amount = Formatters.formatCurrency(stats.totalCollectedThisMonth, profile.currencySymbol),
                            tint = StatusSuccess,
                            icon = Icons.Default.TrendingUp
                        )
                        FinanceStatItem(
                            title = "Toplam Gider",
                            amount = Formatters.formatCurrency(stats.totalExpensesThisMonth, profile.currencySymbol),
                            tint = StatusError,
                            icon = Icons.Default.TrendingDown
                        )
                        FinanceStatItem(
                            title = "Kalan Alacak",
                            amount = Formatters.formatCurrency(stats.totalPendingReceivable, profile.currencySymbol),
                            tint = StatusWarning,
                            icon = Icons.Default.Schedule,
                            onClick = onReceivablesClick
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Status Counts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                JobCountCard(
                    modifier = Modifier.weight(1f),
                    title = "Bugünkü İş",
                    count = stats.todayJobsCount.toString(),
                    color = ForestGreenPrimary,
                    containerColor = ForestGreenContainer
                )
                JobCountCard(
                    modifier = Modifier.weight(1f),
                    title = "Devam Eden",
                    count = stats.inProgressJobsCount.toString(),
                    color = StatusWarning,
                    containerColor = StatusWarningContainer
                )
                JobCountCard(
                    modifier = Modifier.weight(1f),
                    title = "Tamamlanan",
                    count = stats.completedThisMonthCount.toString(),
                    color = StatusSuccess,
                    containerColor = StatusSuccessContainer
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Quick Actions Row
        item {
            Text(
                text = "Hızlı İşlemler",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Work,
                    label = "Yeni İş",
                    modifier = Modifier.weight(1f),
                    tag = "dash_quick_job",
                    onClick = onNewJob
                )
                QuickActionButton(
                    icon = Icons.Default.PersonAdd,
                    label = "Müşteri",
                    modifier = Modifier.weight(1f),
                    tag = "dash_quick_cust",
                    onClick = onNewCustomer
                )
                QuickActionButton(
                    icon = Icons.Default.Description,
                    label = "Teklif",
                    modifier = Modifier.weight(1f),
                    tag = "dash_quick_quote",
                    onClick = onNewQuote
                )
                QuickActionButton(
                    icon = Icons.Default.AttachMoney,
                    label = "Tahsilat",
                    modifier = Modifier.weight(1f),
                    tag = "dash_quick_payment",
                    onClick = onAddPayment
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Today's Jobs Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bugünkü Randevular (${todayJobs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (todayJobs.isNotEmpty()) {
                    Text(
                        text = "Tümü",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ForestGreenPrimary,
                        modifier = Modifier.clickable { onSeeAllJobs() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (todayJobs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bugün için planlanmış randevu yok.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        } else {
            items(todayJobs) { item ->
                DashboardJobCard(
                    item = item,
                    currencySymbol = profile.currencySymbol,
                    onClick = { onJobClick(item.job.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // Recent Jobs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Son Eklenen İşler",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tümünü Gör",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ForestGreenPrimary,
                    modifier = Modifier.clickable { onSeeAllJobs() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (jobs.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Work,
                    title = "Henüz İş Kaydı Yok",
                    description = "Ustaca ile ilk iş emrinizi oluşturarak randevularınızı, müşterilerinizi ve ödemelerinizi kolayca yönetin.",
                    actionButtonText = "İlk İşini Ekle",
                    onActionClick = onNewJob
                )
            }
        } else {
            items(jobs.take(5)) { item ->
                DashboardJobCard(
                    item = item,
                    currencySymbol = profile.currencySymbol,
                    onClick = { onJobClick(item.job.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Recent Activity
        if (recentActivity.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Son Hareketler",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            items(recentActivity.take(4)) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ForestGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = log.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = log.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = Formatters.formatTime(log.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
private fun FinanceStatItem(
    title: String,
    amount: String,
    tint: Color,
    icon: ImageVector,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun JobCountCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    color: Color,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                ),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ForestGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = ForestGreenOnContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DashboardJobCard(
    item: JobWithCustomer,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val job = item.job
    val customer = item.customer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("job_item_${job.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = job.jobCode,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ForestGreenPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = job.status)
                }
                PriorityBadge(priority = job.priority)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = job.title,
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
                    text = "${customer.name} • ${job.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val price = if (job.finalPrice > 0) job.finalPrice else job.estimatedPrice
                if (price > 0) {
                    Text(
                        text = Formatters.formatCurrency(price, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ForestGreenPrimary
                    )
                }
            }
            if (job.scheduledDate > 0L) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${Formatters.formatDate(job.scheduledDate)} ${job.scheduledTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
