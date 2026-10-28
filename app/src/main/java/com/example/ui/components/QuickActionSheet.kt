package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenOnContainer
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusInfoContainer
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusWarningContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionBottomSheet(
    onDismiss: () -> Unit,
    onNewJob: () -> Unit,
    onNewCustomer: () -> Unit,
    onNewQuote: () -> Unit,
    onAddPayment: () -> Unit,
    onAddExpense: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Hızlı İşlem Yap",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            QuickActionItem(
                icon = Icons.Default.Work,
                iconBg = ForestGreenContainer,
                iconTint = ForestGreenOnContainer,
                title = "Yeni İş Oluştur",
                subtitle = "İş emri, randevu saati ve tahmini ücret belirle",
                tag = "quick_action_new_job",
                onClick = {
                    onDismiss()
                    onNewJob()
                }
            )

            QuickActionItem(
                icon = Icons.Default.PersonAdd,
                iconBg = StatusInfoContainer,
                iconTint = Color(0xFF1D4ED8),
                title = "Yeni Müşteri Ekle",
                subtitle = "İletişim bilgisi ve adres kaydet",
                tag = "quick_action_new_customer",
                onClick = {
                    onDismiss()
                    onNewCustomer()
                }
            )

            QuickActionItem(
                icon = Icons.Default.Description,
                iconBg = StatusWarningContainer,
                iconTint = Color(0xFFB45309),
                title = "Fiyat Teklifi Hazırla",
                subtitle = "Kalem kalem hesapla, PDF olarak paylaş",
                tag = "quick_action_new_quote",
                onClick = {
                    onDismiss()
                    onNewQuote()
                }
            )

            QuickActionItem(
                icon = Icons.Default.AttachMoney,
                iconBg = StatusSuccessContainer,
                iconTint = Color(0xFF15803D),
                title = "Tahsilat Ekle",
                subtitle = "Nakit, POS veya havale ödeme kaydet",
                tag = "quick_action_add_payment",
                onClick = {
                    onDismiss()
                    onAddPayment()
                }
            )

            QuickActionItem(
                icon = Icons.Default.Receipt,
                iconBg = Color(0xFFFEE2E2),
                iconTint = Color(0xFFB91C1C),
                title = "Gider Kaydet",
                subtitle = "Malzeme, yakıt, kira veya fatura harcaması",
                tag = "quick_action_add_expense",
                onClick = {
                    onDismiss()
                    onAddExpense()
                }
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
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
        }
    }
}
