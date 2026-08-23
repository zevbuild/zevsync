package com.example.ui.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.SyncedFile
import com.example.ui.UiState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkDownloadDialog(
    uiState: UiState,
    onDismiss: () -> Unit,
    onExtractApkToVault: (onSuccess: (SyncedFile) -> Unit) -> Unit,
    onOpenGitHubHub: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    var isExtracting by remember { mutableStateOf(false) }
    var extractedFile by remember { mutableStateOf<SyncedFile?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF10B981), Color(0xFF047857))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.InstallMobile,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Download ZevSync APK",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF047857)
                        ) {
                            Text(
                                text = "v1.0.0",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Install directly on any Android device or beam offline",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Direct In-App", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("AI Studio & GitHub", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Install Guide", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // Direct In-App Extraction & Offline Beaming Tab
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WifiTethering, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Offline Self-Distribution Engine", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Extract ZevSync's active APK directly onto this device to save it in your Vault, share it via any chat app, or beam it over Bluetooth to nearby phones with zero internet!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    isExtracting = true
                                    onExtractApkToVault { file ->
                                        isExtracting = false
                                        extractedFile = file
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("extract_apk_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF047857),
                                    contentColor = Color.White
                                ),
                                enabled = !isExtracting
                            ) {
                                Icon(
                                    imageVector = if (isExtracting) Icons.Default.CloudSync else Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isExtracting) "Extracting ZevSync.apk..." else "Extract APK to Local Vault",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            if (extractedFile != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF064E3B).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("ZevSync_v1.0.apk Ready in Vault!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    extractedFile?.localFilePath?.let { path ->
                                                        shareApkFile(context, path)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Share APK", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    onDismiss()
                                                    onShowSnackbar("Open Bluetooth Radar or Vault to beam APK!")
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Beam Mesh", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // APK Details Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Package Information", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            ApkInfoRow("App Name", "ZevSync")
                            ApkInfoRow("Package ID", "com.aistudio.zevsync")
                            ApkInfoRow("Architecture", "Universal (arm64-v8a, armeabi-v7a, x86_64)")
                            ApkInfoRow("Minimum Android", "Android 8.0+ (API 26)")
                            ApkInfoRow("Target Android", "Android 15 (API 35)")
                        }
                    }
                }

                1 -> {
                    // AI Studio & GitHub Download Tab
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Option A: AI Studio Settings Menu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "In the AI Studio web interface:\n" +
                                        "1. Look at the top-right header (near your avatar).\n" +
                                        "2. Click the Settings / Three-Dot Menu (⋮) or Export icon.\n" +
                                        "3. Click 'Download APK' or 'Export as ZIP' to download the ready-to-run file immediately.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF24292E).copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF24292E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Option B: GitHub Releases & Actions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Automated CI/CD generates fresh APK artifacts on every commit. You can download the latest release or inspect raw repo files directly.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onOpenGitHubHub()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E))
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open GitHub Hub", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Installation Guide Tab
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        InstallStepCard(
                            stepNumber = "1",
                            title = "Download APK",
                            description = "Extract the APK inside ZevSync or download 'ZevSync.apk' from AI Studio or GitHub Releases."
                        )
                        InstallStepCard(
                            stepNumber = "2",
                            title = "Allow Unknown Apps",
                            description = "When opening the downloaded APK, tap 'Settings' on the system prompt and enable 'Allow from this source'."
                        )
                        InstallStepCard(
                            stepNumber = "3",
                            title = "Tap Install",
                            description = "Confirm the installation. Android package manager will install ZevSync in seconds."
                        )
                        InstallStepCard(
                            stepNumber = "4",
                            title = "Enable Bluetooth Permissions",
                            description = "Launch ZevSync and grant 'Nearby Devices' & 'Bluetooth' permissions to begin 100% offline mesh synchronizing!"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun ApkInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InstallStepCard(stepNumber: String, title: String, description: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF047857)),
                contentAlignment = Alignment.Center
            ) {
                Text(stepNumber, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
        }
    }
}

private fun shareApkFile(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "APK file not found at $filePath", Toast.LENGTH_SHORT).show()
            return
        }

        val uri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ZevSync Android Application (APK)")
            putExtra(Intent.EXTRA_TEXT, "Here is ZevSync - Offline Bluetooth P2P Synchronizer & Mesh Cache Vault APK.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(intent, "Share ZevSync APK via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing APK: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
