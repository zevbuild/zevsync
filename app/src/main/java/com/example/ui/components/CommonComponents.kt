package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.StorageBreakdown
import com.example.data.model.SyncStatus
import com.example.ui.theme.MeshCyan80
import com.example.ui.theme.MeshDanger
import com.example.ui.theme.MeshIndigo80
import com.example.ui.theme.MeshInfo
import com.example.ui.theme.MeshSuccess
import com.example.ui.theme.MeshWarning

@Composable
fun SyncStatusBadge(status: SyncStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text, icon) = when (status) {
        SyncStatus.SYNCED -> Quadruple(
            Color(0xFF064E3B),
            MeshSuccess,
            "Synced",
            Icons.Default.CheckCircle
        )
        SyncStatus.SYNCING -> Quadruple(
            Color(0xFF0C4A6E),
            MeshInfo,
            "Syncing",
            Icons.Default.Sync
        )
        SyncStatus.CONFLICT -> Quadruple(
            Color(0xFF78350F),
            MeshWarning,
            "Conflict",
            Icons.Default.SyncProblem
        )
        SyncStatus.LOCAL_ONLY -> Quadruple(
            Color(0xFF312E81),
            MeshIndigo80,
            "Local Only",
            Icons.Default.InsertDriveFile
        )
        SyncStatus.REMOTE_ONLY -> Quadruple(
            Color(0xFF374151),
            Color(0xFF9CA3AF),
            "Remote",
            Icons.Default.Folder
        )
        SyncStatus.QUEUED -> Quadruple(
            Color(0xFF1E293B),
            Color(0xFF94A3B8),
            "Queued",
            Icons.Default.Sync
        )
        SyncStatus.ERROR -> Quadruple(
            Color(0xFF7F1D1D),
            MeshDanger,
            "Error",
            Icons.Default.SyncProblem
        )
    }

    Surface(
        color = bgColor.copy(alpha = 0.85f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun FileCategoryIcon(
    category: FileCategory,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    val (icon, bgGradient) = when (category) {
        FileCategory.IMAGE -> Icons.Default.Image to listOf(Color(0xFFEC4899), Color(0xFFBE185D))
        FileCategory.DOCUMENT -> Icons.Default.Description to listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
        FileCategory.AUDIO -> Icons.Default.Audiotrack to listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
        FileCategory.VIDEO -> Icons.Default.Movie to listOf(Color(0xFFF97316), Color(0xFFC2410C))
        FileCategory.CODE -> Icons.Default.Code to listOf(Color(0xFF10B981), Color(0xFF047857))
        FileCategory.ARCHIVE -> Icons.Default.FolderZip to listOf(Color(0xFFEAB308), Color(0xFFA16207))
        FileCategory.OTHER, FileCategory.ALL -> Icons.Default.Folder to listOf(Color(0xFF64748B), Color(0xFF334155))
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(bgGradient)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.label,
            tint = Color.White,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun BluetoothRadarAnimation(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true,
    connectedCount: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val center = this.center
            val maxRadius = size.minDimension / 2

            // Static background grid circles
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                radius = maxRadius * 0.35f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                radius = maxRadius * 0.7f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                radius = maxRadius,
                style = Stroke(width = 1.dp.toPx())
            )

            if (isScanning) {
                // Expanding wave 1
                drawCircle(
                    color = Color(0xFF00F5D4).copy(alpha = (1f - pulse1) * 0.5f),
                    radius = maxRadius * pulse1,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Expanding wave 2
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = (1f - pulse2) * 0.5f),
                    radius = maxRadius * pulse2,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Center device node
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF00D2BA), Color(0xFF4F46E5)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiTethering,
                contentDescription = "Bluetooth Center",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun StorageProgressBar(
    breakdown: StorageBreakdown,
    modifier: Modifier = Modifier
) {
    val total = breakdown.quotaBytes.toFloat().coerceAtLeast(1f)
    val imgFrac = (breakdown.imagesBytes / total).coerceIn(0f, 1f)
    val docFrac = (breakdown.docsBytes / total).coerceIn(0f, 1f)
    val codeFrac = (breakdown.codeBytes / total).coerceIn(0f, 1f)
    val mediaFrac = ((breakdown.audioBytes + breakdown.videoBytes) / total).coerceIn(0f, 1f)
    val otherFrac = ((breakdown.archiveBytes + breakdown.otherBytes) / total).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF1E293B))
        ) {
            if (imgFrac > 0.01f) {
                Box(
                    modifier = Modifier
                        .weight(imgFrac)
                        .height(10.dp)
                        .background(Color(0xFFEC4899))
                )
            }
            if (docFrac > 0.01f) {
                Box(
                    modifier = Modifier
                        .weight(docFrac)
                        .height(10.dp)
                        .background(Color(0xFF3B82F6))
                )
            }
            if (codeFrac > 0.01f) {
                Box(
                    modifier = Modifier
                        .weight(codeFrac)
                        .height(10.dp)
                        .background(Color(0xFF10B981))
                )
            }
            if (mediaFrac > 0.01f) {
                Box(
                    modifier = Modifier
                        .weight(mediaFrac)
                        .height(10.dp)
                        .background(Color(0xFF8B5CF6))
                )
            }
            if (otherFrac > 0.01f) {
                Box(
                    modifier = Modifier
                        .weight(otherFrac)
                        .height(10.dp)
                        .background(Color(0xFFEAB308))
                )
            }
            val remaining = (1f - breakdown.usedPercentage).coerceIn(0.001f, 1f)
            Box(
                modifier = Modifier
                    .weight(remaining)
                    .height(10.dp)
                    .background(Color(0xFF334155))
            )
        }
    }
}
