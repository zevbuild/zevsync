package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.github.GitHubFileItem
import com.example.data.github.GitHubReleaseInfo
import com.example.data.model.SyncedFile
import com.example.ui.UiState

enum class GitHubHubTab(val title: String) {
    DIRECT_DOWNLOAD("Direct Download"),
    RELEASES("Releases & APK"),
    REPO_EXPLORER("Repo Files"),
    EXPORT_GIST("Export Gist"),
    GUIDE("GitHub Guide")
}

data class GitHubPreset(
    val title: String,
    val description: String,
    val url: String,
    val suggestedName: String,
    val tag: String
)

@Composable
fun GitHubHubDialog(
    uiState: UiState,
    onDismiss: () -> Unit,
    onDownloadFile: (url: String, customName: String?) -> Unit,
    onFetchRelease: (owner: String, repo: String) -> Unit,
    onFetchContents: (owner: String, repo: String, path: String) -> Unit,
    onExportGist: (file: SyncedFile, isPublic: Boolean, token: String?) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Direct Download State
    var directUrlInput by remember { mutableStateOf("") }
    var directFileNameInput by remember { mutableStateOf("") }

    // Release State
    var repoOwnerInput by remember { mutableStateOf("google") }
    var repoNameInput by remember { mutableStateOf("iosched") }

    // Repo Explorer State
    var exploreOwnerInput by remember { mutableStateOf("android") }
    var exploreRepoInput by remember { mutableStateOf("architecture-samples") }
    var explorePathInput by remember { mutableStateOf("") }

    // Export Gist State
    var selectedFileForGist by remember { mutableStateOf<SyncedFile?>(uiState.files.firstOrNull()) }
    var gistIsPublic by remember { mutableStateOf(false) }
    var gistTokenInput by remember { mutableStateOf("") }

    val presets = remember {
        listOf(
            GitHubPreset(
                title = "Markdown Syntax Cheatsheet",
                description = "Comprehensive markdown formatting guide for offline notes",
                url = "https://raw.githubusercontent.com/adam-p/markdown-here/master/src/common/markdown-here-cheatsheet.md",
                suggestedName = "Markdown_Cheatsheet.md",
                tag = "DOC"
            ),
            GitHubPreset(
                title = "Git Command Reference",
                description = "Complete offline Git workflow cheat sheet",
                url = "https://raw.githubusercontent.com/git/git/master/Documentation/user-manual.txt",
                suggestedName = "Git_Reference_Manual.txt",
                tag = "GUIDE"
            ),
            GitHubPreset(
                title = "Android Kotlin Coroutines Guide",
                description = "Official Kotlin Coroutines core guide & patterns",
                url = "https://raw.githubusercontent.com/Kotlin/kotlinx.coroutines/master/README.md",
                suggestedName = "Kotlin_Coroutines_README.md",
                tag = "CODE"
            ),
            GitHubPreset(
                title = "Sample Architecture Config",
                description = "Clean Architecture sample schema configuration",
                url = "https://raw.githubusercontent.com/android/architecture-samples/main/README.md",
                suggestedName = "Architecture_Samples_Config.md",
                tag = "SAMPLE"
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("github_hub_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "GitHub Direct Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Direct download, releases & repo sync for SyncBeam",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_github_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    GitHubHubTab.values().forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.testTag("github_tab_$index")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Loading / Progress Indicator
                if (uiState.isGitHubLoading) {
                    LinearProgressIndicator(
                        progress = { uiState.gitHubDownloadProgress.coerceIn(0.1f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }

                // Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    when (selectedTab) {
                        0 -> DirectDownloadTab(
                            urlInput = directUrlInput,
                            onUrlChange = { directUrlInput = it },
                            fileNameInput = directFileNameInput,
                            onFileNameChange = { directFileNameInput = it },
                            presets = presets,
                            isLoading = uiState.isGitHubLoading,
                            onSelectPreset = { preset ->
                                directUrlInput = preset.url
                                directFileNameInput = preset.suggestedName
                            },
                            onDownload = {
                                onDownloadFile(directUrlInput, directFileNameInput)
                            }
                        )

                        1 -> ReleasesTab(
                            owner = repoOwnerInput,
                            onOwnerChange = { repoOwnerInput = it },
                            repo = repoNameInput,
                            onRepoChange = { repoNameInput = it },
                            releaseInfo = uiState.gitHubLatestRelease,
                            isLoading = uiState.isGitHubLoading,
                            onFetch = { onFetchRelease(repoOwnerInput, repoNameInput) },
                            onDownloadAsset = { assetUrl, name ->
                                onDownloadFile(assetUrl, name)
                            }
                        )

                        2 -> RepoExplorerTab(
                            owner = exploreOwnerInput,
                            onOwnerChange = { exploreOwnerInput = it },
                            repo = exploreRepoInput,
                            onRepoChange = { exploreRepoInput = it },
                            path = explorePathInput,
                            onPathChange = { explorePathInput = it },
                            repoFiles = uiState.gitHubRepoFiles,
                            isLoading = uiState.isGitHubLoading,
                            onFetch = { onFetchContents(exploreOwnerInput, exploreRepoInput, explorePathInput) },
                            onDownloadFile = { url, name ->
                                onDownloadFile(url, name)
                            }
                        )

                        3 -> ExportGistTab(
                            files = uiState.files,
                            selectedFile = selectedFileForGist,
                            onSelectFile = { selectedFileForGist = it },
                            isPublic = gistIsPublic,
                            onTogglePublic = { gistIsPublic = it },
                            token = gistTokenInput,
                            onTokenChange = { gistTokenInput = it },
                            gistUrl = uiState.gitHubGistUrl,
                            isLoading = uiState.isGitHubLoading,
                            onExport = {
                                selectedFileForGist?.let { file ->
                                    onExportGist(file, gistIsPublic, gistTokenInput.takeIf { it.isNotBlank() })
                                }
                            },
                            onCopyUrl = { url ->
                                clipboardManager.setText(AnnotatedString(url))
                                onShowSnackbar("Gist URL copied to clipboard!")
                            },
                            onOpenUrl = { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        )

                        4 -> GuideTab(
                            onCopyCommand = { cmd ->
                                clipboardManager.setText(AnnotatedString(cmd))
                                onShowSnackbar("Command copied to clipboard!")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectDownloadTab(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    fileNameInput: String,
    onFileNameChange: (String) -> Unit,
    presets: List<GitHubPreset>,
    isLoading: Boolean,
    onSelectPreset: (GitHubPreset) -> Unit,
    onDownload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "📥 Direct GitHub File & Asset Downloader",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Paste any GitHub raw URL, blob link, Gist link, or 'owner/repo/filepath' shorthand. Files are stored directly into your offline cache vault and synced to nearby Bluetooth nodes automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        OutlinedTextField(
            value = urlInput,
            onValueChange = onUrlChange,
            label = { Text("GitHub URL or shorthand (e.g. owner/repo/path)") },
            placeholder = { Text("https://github.com/user/repo/blob/main/file.md") },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            trailingIcon = {
                if (urlInput.isNotBlank()) {
                    IconButton(onClick = { onUrlChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("github_url_input")
        )

        OutlinedTextField(
            value = fileNameInput,
            onValueChange = onFileNameChange,
            label = { Text("Custom File Name (Optional)") },
            placeholder = { Text("e.g. documentation.md") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("github_filename_input")
        )

        Button(
            onClick = onDownload,
            enabled = urlInput.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("github_download_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Downloading from GitHub...")
            } else {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download Directly to Offline Vault")
            }
        }

        // Quick Presets
        Text(
            text = "⚡ Quick Presets & Templates",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        presets.forEach { preset ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPreset(preset) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = preset.tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = preset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onSelectPreset(preset) }) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = "Use Preset",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleasesTab(
    owner: String,
    onOwnerChange: (String) -> Unit,
    repo: String,
    onRepoChange: (String) -> Unit,
    releaseInfo: GitHubReleaseInfo?,
    isLoading: Boolean,
    onFetch: () -> Unit,
    onDownloadAsset: (assetUrl: String, name: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "📦 GitHub Releases & APK Direct Installer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Fetch pre-built APK binaries, release packages, or source code archives directly from GitHub releases for any repository.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = owner,
                onValueChange = onOwnerChange,
                label = { Text("Owner / Org") },
                placeholder = { Text("google") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("github_owner_input")
            )

            OutlinedTextField(
                value = repo,
                onValueChange = onRepoChange,
                label = { Text("Repository") },
                placeholder = { Text("iosched") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("github_repo_input")
            )
        }

        Button(
            onClick = onFetch,
            enabled = owner.isNotBlank() && repo.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fetch_releases_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fetch Latest Release")
        }

        // Release Results
        if (releaseInfo != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = releaseInfo.name.ifBlank { releaseInfo.tagName },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tag: ${releaseInfo.tagName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "LATEST",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (releaseInfo.body.isNotBlank()) {
                        Text(
                            text = releaseInfo.body.take(300),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Downloadable Release Assets (${releaseInfo.assets.size}):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (releaseInfo.assets.isEmpty()) {
                        // Offer source archive
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Storage, contentDescription = null)
                                    Text(
                                        text = "${releaseInfo.tagName}-source.zip",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Button(
                                    onClick = {
                                        val zipUrl = "https://github.com/$owner/$repo/archive/refs/tags/${releaseInfo.tagName}.zip"
                                        onDownloadAsset(zipUrl, "${repo}_${releaseInfo.tagName}.zip")
                                    },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download Zip", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        releaseInfo.assets.forEach { asset ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = asset.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Size: ${(asset.size / 1024 / 1024.0).let { "%.1f MB".format(it) }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = { onDownloadAsset(asset.downloadUrl, asset.name) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Download", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoExplorerTab(
    owner: String,
    onOwnerChange: (String) -> Unit,
    repo: String,
    onRepoChange: (String) -> Unit,
    path: String,
    onPathChange: (String) -> Unit,
    repoFiles: List<GitHubFileItem>,
    isLoading: Boolean,
    onFetch: () -> Unit,
    onDownloadFile: (url: String, name: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = owner,
                onValueChange = onOwnerChange,
                label = { Text("Owner") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = repo,
                onValueChange = onRepoChange,
                label = { Text("Repo") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = path,
            onValueChange = onPathChange,
            label = { Text("Sub-directory / Path (optional)") },
            placeholder = { Text("e.g. app/src/main") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onFetch,
            enabled = owner.isNotBlank() && repo.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_repo_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Folder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Repository Files")
        }

        if (repoFiles.isNotEmpty()) {
            Text(
                text = "Repository Contents (${repoFiles.size} items):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(repoFiles) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.type == "dir") Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = if (item.type == "dir") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                                Column {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.size > 0) {
                                        Text(
                                            text = "${item.size} bytes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (item.type == "file" && item.downloadUrl != null) {
                                IconButton(
                                    onClick = { onDownloadFile(item.downloadUrl, item.name) }
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = "Download File",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else if (item.type == "dir") {
                                OutlinedButton(
                                    onClick = {
                                        onPathChange(item.path)
                                        onFetch()
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Open", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportGistTab(
    files: List<SyncedFile>,
    selectedFile: SyncedFile?,
    onSelectFile: (SyncedFile) -> Unit,
    isPublic: Boolean,
    onTogglePublic: (Boolean) -> Unit,
    token: String,
    onTokenChange: (String) -> Unit,
    gistUrl: String?,
    isLoading: Boolean,
    onExport: () -> Unit,
    onCopyUrl: (String) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "🌐 Export Vault File to GitHub Gist",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Upload any offline markdown document, code file, or log to GitHub as a Gist for sharing across the web.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Text(
            text = "Select Document from Vault:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            files.forEach { file ->
                val isSelected = selectedFile?.id == file.id
                Card(
                    modifier = Modifier
                        .clickable { onSelectFile(file) }
                        .width(180.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = file.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = file.sizeFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            label = { Text("Personal Access Token (optional for anonymous/secret)") },
            placeholder = { Text("ghp_...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTogglePublic(!isPublic) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPublic) "Public Gist" else "Secret / Unlisted Gist",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isPublic) "Discoverable on GitHub public feed" else "Accessible only via direct link",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            androidx.compose.material3.Switch(
                checked = isPublic,
                onCheckedChange = onTogglePublic
            )
        }

        Button(
            onClick = onExport,
            enabled = selectedFile != null && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("export_gist_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Creating Gist on GitHub...")
            } else {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export '${selectedFile?.name ?: "File"}' to GitHub")
            }
        }

        if (gistUrl != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🎉 Gist Published Successfully!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = gistUrl,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onCopyUrl(gistUrl) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Link")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onOpenUrl(gistUrl) }) {
                            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open in Browser")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideTab(onCopyCommand: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🚀 How to Download & Build SyncBeam via GitHub",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SyncBeam is completely open source and modular. Follow these steps to clone, build, or download APK releases directly from GitHub.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Step 1
        InstructionStep(
            stepNumber = "1",
            title = "Export to GitHub from AI Studio",
            description = "Click on the project settings or GitHub icon in the AI Studio header to push this repository directly to your personal GitHub account.",
            command = null,
            onCopy = {}
        )

        // Step 2
        InstructionStep(
            stepNumber = "2",
            title = "Clone the repository locally",
            description = "Open your terminal and run the git clone command on your machine:",
            command = "git clone https://github.com/rajuzevenor/SyncBeam.git",
            onCopy = { onCopyCommand("git clone https://github.com/rajuzevenor/SyncBeam.git") }
        )

        // Step 3
        InstructionStep(
            stepNumber = "3",
            title = "Build the Debug or Release APK",
            description = "Use Gradle to compile and produce the stand-alone installable APK:",
            command = "gradle assembleDebug",
            onCopy = { onCopyCommand("gradle assembleDebug") }
        )

        // Step 4
        InstructionStep(
            stepNumber = "4",
            title = "Direct APK location",
            description = "Once compiled, find your installable APK at:",
            command = "app/build/outputs/apk/debug/app-debug.apk",
            onCopy = { onCopyCommand("app/build/outputs/apk/debug/app-debug.apk") }
        )
    }
}

@Composable
private fun InstructionStep(
    stepNumber: String,
    title: String,
    description: String,
    command: String?,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (command != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = command,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Command",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
