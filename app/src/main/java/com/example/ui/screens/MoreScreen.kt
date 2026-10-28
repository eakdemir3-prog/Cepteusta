package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.model.BusinessProfile
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.theme.StatusNeutralContainer
import com.example.ui.theme.StatusNeutral
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusInfoContainer
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarningContainer
import java.io.File

@Composable
fun MoreScreen(
    profile: BusinessProfile,
    onNavigateCustomers: () -> Unit,
    onNavigateQuotes: () -> Unit,
    onNavigateFinances: () -> Unit,
    onNavigateReports: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateBackup: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            CepteUstaTopBar(title = "Daha Fazla")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Business Card Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateProfile() }
                    .testTag("more_profile_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(ForestGreenContainer),
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
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.businessName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = profile.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestGreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Menu List
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MoreMenuItem(
                        icon = Icons.Default.People,
                        iconBg = ForestGreenContainer,
                        iconTint = ForestGreenOnContainer,
                        title = "Müşteriler",
                        subtitle = "Müşteri listesi, iletişim ve iş geçmişi",
                        tag = "menu_customers",
                        onClick = onNavigateCustomers
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    MoreMenuItem(
                        icon = Icons.Default.Description,
                        iconBg = StatusWarningContainer,
                        iconTint = StatusWarning,
                        title = "Teklifler",
                        subtitle = "Fiyat teklifleri hazırlama ve PDF paylaşımı",
                        tag = "menu_quotes",
                        onClick = onNavigateQuotes
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    MoreMenuItem(
                        icon = Icons.Default.AttachMoney,
                        iconBg = StatusSuccessContainer,
                        iconTint = StatusSuccess,
                        title = "Gelir - Gider & Kasa",
                        subtitle = "Tahsilatlar, malzeme harcamaları ve nakit akışı",
                        tag = "menu_finances",
                        onClick = onNavigateFinances
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    MoreMenuItem(
                        icon = Icons.Default.Assessment,
                        iconBg = StatusInfoContainer,
                        iconTint = StatusInfo,
                        title = "Raporlar & Analiz",
                        subtitle = "Aylık performans, net kazanç ve CSV dışa aktarımı",
                        tag = "menu_reports",
                        onClick = onNavigateReports
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Management Menu
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MoreMenuItem(
                        icon = Icons.Default.Business,
                        iconBg = ForestGreenContainer,
                        iconTint = ForestGreenOnContainer,
                        title = "İşletme Profili & Logo",
                        subtitle = "Firma adı, unvan, telefon ve logo yönetimi",
                        tag = "menu_profile",
                        onClick = onNavigateProfile
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    MoreMenuItem(
                        icon = Icons.Default.Backup,
                        iconBg = Color(0xFF8B5CF6).copy(alpha = 0.14f),
                        iconTint = Color(0xFF8B5CF6),
                        title = "Veri Yedekleme & Geri Yükleme",
                        subtitle = "JSON yedek dosyası, örnek veri yükleme ve sıfırlama",
                        tag = "menu_backup",
                        onClick = onNavigateBackup
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    MoreMenuItem(
                        icon = Icons.Default.Settings,
                        iconBg = StatusNeutralContainer,
                        iconTint = StatusNeutral,
                        title = "Ayarlar",
                        subtitle = "Tema seçimi (Açık/Koyu), para birimi ve hakkında",
                        tag = "menu_settings",
                        onClick = onNavigateSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
