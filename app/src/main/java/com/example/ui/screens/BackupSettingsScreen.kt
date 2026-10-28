package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CepteUstaTopBar
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusError
import com.example.util.PdfExportUtil
import java.io.File

@Composable
fun BackupSettingsScreen(
    onExportBackup: ((File) -> Unit) -> Unit,
    onRestoreBackup: (String) -> Unit,
    onLoadSampleData: () -> Unit,
    onResetAllData: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showSampleConfirm by remember { mutableStateOf(false) }
    var showResetStep1 by remember { mutableStateOf(false) }
    var showResetStep2 by remember { mutableStateOf(false) }

    // File picker for restoring JSON
    val jsonFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonContent = inputStream?.bufferedReader()?.use { it.readText() }
                if (!jsonContent.isNullOrBlank()) {
                    onRestoreBackup(jsonContent)
                } else {
                    Toast.makeText(context, "Seçilen dosya boş", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Dosya okunamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CepteUstaTopBar(
                title = "Veri Yedekleme & Geri Yükleme",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Local Persistence Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Verileriniz gizlilik esasıyla cihazınızın yerel güvenli SQLite veritabanında saklanır. Telefon değiştirirken verilerinizi aktarmak için aşağıdaki JSON yedekleme özelliğini kullanabilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export & Restore Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Yedekleme İşlemleri",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Müşterileriniz, iş emirleriniz, teklifleriniz ve tüm gelir-gider kayıtlarınız tek bir güvenli JSON dosyasına aktarılır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onExportBackup { backupFile ->
                                PdfExportUtil.shareFile(context, backupFile, mimeType = "application/json")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("backup_export_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yedek Dosyası Oluştur ve Paylaş", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { jsonFilePickerLauncher.launch("application/json") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("backup_restore_btn")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp), tint = ForestGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yedekten Geri Yükle (.json)", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sample Data Card (Explicit Requirement)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tanıtım ve Örnek Veriler",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Uygulamayı tanımak, rapor ve teklif akışlarını test etmek için 4 müşteri, örnek işler, teklif ve gelir-gider kayıtları yükleyebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showSampleConfirm = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("backup_load_sample_btn")
                    ) {
                        Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(18.dp), tint = ForestGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Örnek Verileri Yükle", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Danger Zone: Reset All Data (2-step verification)
            Card(
                colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tehlikeli Bölge: Verileri Sıfırla",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusError
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tüm müşteriler, iş emirleri, teklifler, fotoğraflar ve finans kayıtları kalıcı olarak silinir. Bu işlem geri alınamaz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showResetStep1 = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("backup_reset_all_btn")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tüm Verileri Sıfırla (2 Aşamalı)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Sample Data Confirmation Dialog
        if (showSampleConfirm) {
            AlertDialog(
                onDismissRequest = { showSampleConfirm = false },
                title = { Text("Örnek Verileri Yükle") },
                text = { Text("Uygulamaya tanıtım amaçlı usta işleri, müşteri ve teklif kayıtları eklenecektir. Onaylıyor musunuz?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showSampleConfirm = false
                            onLoadSampleData()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("Yükle")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSampleConfirm = false }) {
                        Text("Vazgeç")
                    }
                }
            )
        }

        // Reset Step 1 Dialog
        if (showResetStep1) {
            AlertDialog(
                onDismissRequest = { showResetStep1 = false },
                title = { Text("1. Aşama Onayı: Verileri Sıfırla", color = StatusError) },
                text = { Text("Tüm iş, müşteri, teklif ve tahsilat kayıtlarınızı silmek üzeresiniz. Devam etmek istiyor musunuz?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetStep1 = false
                            showResetStep2 = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                    ) {
                        Text("Evet, 2. Aşamaya Geç")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetStep1 = false }) {
                        Text("Vazgeç")
                    }
                }
            )
        }

        // Reset Step 2 Dialog
        if (showResetStep2) {
            AlertDialog(
                onDismissRequest = { showResetStep2 = false },
                title = { Text("Son Onay: Kalıcı Olarak Silinsin mi?", color = StatusError) },
                text = { Text("Bu işlem cihazdaki tüm veritabanını sıfırlayacaktır. Bu işlem geri alınamaz!") },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetStep2 = false
                            onResetAllData()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                    ) {
                        Text("Tümünü Temizle ve Sıfırla")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetStep2 = false }) {
                        Text("İptal Et")
                    }
                }
            )
        }
    }
}
