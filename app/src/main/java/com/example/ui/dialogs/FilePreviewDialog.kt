package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.FileCategory
import com.example.data.model.SyncedFile
import com.example.ui.components.FileCategoryIcon
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.MeshCyan80
import com.example.ui.theme.MeshIndigo80
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FilePreviewDialog(
    file: SyncedFile,
    onDismiss: () -> Unit,
    onSaveEdit: (String) -> Unit,
    onTogglePin: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editableContent by remember { mutableStateOf(file.textPreview ?: "") }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FileCategoryIcon(category = file.category, size = 44)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = file.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${file.sizeFormatted} · ${file.mimeType}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = if (file.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Toggle Pin",
                            tint = if (file.isPinned) MeshCyan80 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Metadata Details Strip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Lineage Version", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("v${file.versionNumber} (Lamport: ${file.lamportTimestamp})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MeshIndigo80)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Origin Device", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(file.originDeviceName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Modified", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(dateFormat.format(Date(file.lastModifiedTimestamp)), fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SHA-256 Hash", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(file.contentHash.take(16) + "...", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MeshCyan80)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content View (Image or Text Editor)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (file.category == FileCategory.IMAGE && file.localFilePath != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = File(file.localFilePath),
                                contentDescription = file.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else if (isEditing) {
                        OutlinedTextField(
                            value = editableContent,
                            onValueChange = { editableContent = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("edit_content_field"),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            ),
                            placeholder = { Text("Edit document text...") }
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = file.textPreview?.ifBlank { "(Empty document)" } ?: "(Binary file contents cached in vault)",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bottom Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (file.category == FileCategory.CODE || file.category == FileCategory.DOCUMENT || file.mimeType.startsWith("text/")) {
                        if (isEditing) {
                            Button(
                                onClick = {
                                    onSaveEdit(editableContent)
                                    isEditing = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("save_edit_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save & Sync Version")
                            }
                        } else {
                            Button(
                                onClick = { isEditing = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("start_edit_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Document")
                            }
                        }
                    }
                }
            }
        }
    }
}
