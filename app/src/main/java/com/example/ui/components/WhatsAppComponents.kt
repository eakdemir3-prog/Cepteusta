package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WhatsAppGreen
import com.example.util.WhatsAppUtil

/** Hazır WhatsApp mesajı: başlık ve gönderilecek metin. */
data class WhatsAppTemplate(val title: String, val message: String)

/**
 * "WhatsApp'tan Yaz" düğmesi. Dokununca hazır mesaj listesi açılır,
 * seçilen mesaj müşterinin numarasıyla WhatsApp'ta hazır olarak açılır.
 */
@Composable
fun WhatsAppButton(
    phone: String,
    templates: List<WhatsAppTemplate>,
    modifier: Modifier = Modifier,
    label: String = "WhatsApp'tan Yaz"
) {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showPicker = true },
        enabled = phone.isNotBlank(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Chat,
            contentDescription = null,
            tint = WhatsAppGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = WhatsAppGreen, fontWeight = FontWeight.Bold)
    }

    if (showPicker) {
        WhatsAppTemplatePicker(
            templates = templates,
            onPick = { template ->
                showPicker = false
                WhatsAppUtil.open(context, phone, template.message)
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
fun WhatsAppTemplatePicker(
    templates: List<WhatsAppTemplate>,
    onPick: (WhatsAppTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hangi mesajı gönderelim?") },
        text = {
            Column {
                templates.forEachIndexed { index, template ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(template) }
                            .padding(vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                template.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                template.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (index < templates.lastIndex) HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
    )
}
