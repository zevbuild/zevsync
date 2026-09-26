package com.example.data.github

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class GitHubFileItem(
    val name: String,
    val path: String,
    val type: String, // "file" or "dir"
    val size: Long,
    val downloadUrl: String?,
    val htmlUrl: String?
)

data class GitHubReleaseInfo(
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val htmlUrl: String,
    val assets: List<GitHubReleaseAsset>
)

data class GitHubReleaseAsset(
    val name: String,
    val size: Long,
    val downloadUrl: String,
    val contentType: String
)

data class GitHubDownloadResult(
    val success: Boolean,
    val fileName: String,
    val bytesDownloaded: Long,
    val localFile: File?,
    val textContent: String?,
    val errorMessage: String? = null
)

class GitHubSyncService(
    private val cacheDir: File,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "GitHubSyncService"
        private const val USER_AGENT = "SyncBeam-Android-GitHub-Downloader"
    }

    /**
     * Download a file directly from a GitHub URL or raw URL into the local file cache.
     */
    suspend fun downloadDirectFile(
        rawOrGitHubUrl: String,
        customFileName: String? = null,
        onProgress: ((Float, Long, Long) -> Unit)? = null
    ): GitHubDownloadResult = withContext(Dispatchers.IO) {
        try {
            val resolvedUrl = resolveDirectDownloadUrl(rawOrGitHubUrl)
            val request = Request.Builder()
                .url(resolvedUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext GitHubDownloadResult(
                    success = false,
                    fileName = customFileName ?: "download",
                    bytesDownloaded = 0,
                    localFile = null,
                    textContent = null,
                    errorMessage = "HTTP ${response.code}: ${response.message}"
                )
            }

            val body = response.body ?: return@withContext GitHubDownloadResult(
                success = false,
                fileName = customFileName ?: "download",
                bytesDownloaded = 0,
                localFile = null,
                textContent = null,
                errorMessage = "Empty response body from GitHub"
            )

            val contentLength = body.contentLength()
            val finalFileName = customFileName?.ifBlank { null }
                ?: extractFileNameFromUrl(resolvedUrl)

            val targetDir = File(cacheDir, "github_downloads").apply { mkdirs() }
            val outputFile = File(targetDir, finalFileName)

            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(8192)
            var totalBytesRead = 0L
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                totalBytesRead += read
                if (contentLength > 0 && onProgress != null) {
                    val progress = (totalBytesRead.toFloat() / contentLength).coerceIn(0f, 1f)
                    onProgress(progress, totalBytesRead, contentLength)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Try reading as text if reasonable size
            val textContent = if (totalBytesRead < 2 * 1024 * 1024) {
                try {
                    outputFile.readText()
                } catch (e: Exception) {
                    null
                }
            } else null

            Log.d(TAG, "Successfully downloaded $finalFileName ($totalBytesRead bytes)")
            GitHubDownloadResult(
                success = true,
                fileName = finalFileName,
                bytesDownloaded = totalBytesRead,
                localFile = outputFile,
                textContent = textContent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed downloading GitHub file: ${e.message}", e)
            GitHubDownloadResult(
                success = false,
                fileName = customFileName ?: "download",
                bytesDownloaded = 0,
                localFile = null,
                textContent = null,
                errorMessage = e.localizedMessage ?: e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Browse files and directories in a repository via GitHub REST API.
     */
    suspend fun listRepositoryContents(
        owner: String,
        repo: String,
        path: String = "",
        token: String? = null
    ): Result<List<GitHubFileItem>> = withContext(Dispatchers.IO) {
        try {
            val cleanPath = path.trim('/').let { if (it.isNotEmpty()) "/$it" else "" }
            val url = "https://api.github.com/repos/$owner/$repo/contents$cleanPath"

            val reqBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/vnd.github.v3+json")

            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }

            val response = client.newCall(reqBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("GitHub API Error ${response.code}: ${response.message}"))
            }

            val responseBody = response.body?.string() ?: "[]"
            val jsonArray = JSONArray(responseBody)
            val items = mutableListOf<GitHubFileItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    GitHubFileItem(
                        name = obj.optString("name"),
                        path = obj.optString("path"),
                        type = obj.optString("type"),
                        size = obj.optLong("size", 0L),
                        downloadUrl = obj.optString("download_url", "").ifEmpty { null },
                        htmlUrl = obj.optString("html_url", "").ifEmpty { null }
                    )
                )
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch latest releases and downloadable APK assets from a repository.
     */
    suspend fun getLatestRelease(
        owner: String,
        repo: String,
        token: String? = null
    ): Result<GitHubReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val reqBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/vnd.github.v3+json")

            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }

            val response = client.newCall(reqBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("GitHub Release Error ${response.code}: ${response.message}"))
            }

            val json = JSONObject(response.body?.string() ?: "{}")
            val assetsArray = json.optJSONArray("assets") ?: JSONArray()
            val assets = mutableListOf<GitHubReleaseAsset>()

            for (i in 0 until assetsArray.length()) {
                val assetObj = assetsArray.getJSONObject(i)
                assets.add(
                    GitHubReleaseAsset(
                        name = assetObj.optString("name"),
                        size = assetObj.optLong("size", 0L),
                        downloadUrl = assetObj.optString("browser_download_url"),
                        contentType = assetObj.optString("content_type")
                    )
                )
            }

            val release = GitHubReleaseInfo(
                tagName = json.optString("tag_name", "v1.0"),
                name = json.optString("name", "Latest Release"),
                body = json.optString("body", "No release description available."),
                publishedAt = json.optString("published_at", ""),
                htmlUrl = json.optString("html_url", "https://github.com/$owner/$repo/releases"),
                assets = assets
            )

            Result.success(release)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export a local document to a GitHub Gist (public or secret).
     */
    suspend fun exportToGist(
        fileName: String,
        content: String,
        description: String = "Exported from SyncBeam Mesh Vault",
        isPublic: Boolean = false,
        token: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val gistPayload = JSONObject().apply {
                put("description", description)
                put("public", isPublic)
                val filesObj = JSONObject().apply {
                    val fileContentObj = JSONObject().apply {
                        put("content", content)
                    }
                    put(fileName, fileContentObj)
                }
                put("files", filesObj)
            }

            val body = gistPayload.toString().toRequestBody("application/json".toMediaType())
            val reqBuilder = Request.Builder()
                .url("https://api.github.com/gists")
                .post(body)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/vnd.github.v3+json")

            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }

            val response = client.newCall(reqBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gist creation failed (${response.code}): ${response.message}"))
            }

            val respJson = JSONObject(response.body?.string() ?: "{}")
            val htmlUrl = respJson.optString("html_url", "")
            Result.success(htmlUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resolves raw download URL from GitHub URLs.
     * Examples:
     * - https://github.com/user/repo/blob/main/path/to/file.ext -> https://raw.githubusercontent.com/user/repo/main/path/to/file.ext
     * - user/repo/main/path/to/file.ext -> https://raw.githubusercontent.com/user/repo/main/path/to/file.ext
     * - raw.githubusercontent.com URL -> direct
     */
    fun resolveDirectDownloadUrl(input: String): String {
        val trimmed = input.trim()

        if (trimmed.startsWith("https://raw.githubusercontent.com/")) {
            return trimmed
        }

        if (trimmed.startsWith("https://github.com/")) {
            // Check if it is a blob URL: https://github.com/owner/repo/blob/branch/path
            val regexBlob = Regex("""^https://github\.com/([^/]+)/([^/]+)/blob/([^/]+)/(.+)$""")
            val matchBlob = regexBlob.find(trimmed)
            if (matchBlob != null) {
                val (owner, repo, branch, path) = matchBlob.destructured
                return "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
            }

            // Check if it is a raw URL or archive zip: https://github.com/owner/repo/archive/refs/heads/main.zip
            if (trimmed.endsWith(".zip") || trimmed.endsWith(".apk") || trimmed.contains("/releases/download/")) {
                return trimmed
            }

            // Check if it is a gist URL: https://gist.github.com/owner/gist_id -> https://gist.githubusercontent.com/owner/gist_id/raw
            if (trimmed.contains("gist.github.com")) {
                return if (trimmed.endsWith("/raw")) trimmed else "$trimmed/raw"
            }

            // If it's a repo link like https://github.com/owner/repo, download main branch archive zip
            val regexRepo = Regex("""^https://github\.com/([^/]+)/([^/]+)/?$""")
            val matchRepo = regexRepo.find(trimmed)
            if (matchRepo != null) {
                val (owner, repo) = matchRepo.destructured
                return "https://github.com/$owner/$repo/archive/refs/heads/main.zip"
            }
        }

        // Shorthand format: owner/repo/path or owner/repo/branch/path
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://") && trimmed.contains('/')) {
            val parts = trimmed.split('/')
            return if (parts.size == 2) {
                "https://github.com/${parts[0]}/${parts[1]}/archive/refs/heads/main.zip"
            } else if (parts.size >= 3) {
                "https://raw.githubusercontent.com/$trimmed"
            } else {
                "https://$trimmed"
            }
        }

        return trimmed
    }

    private fun extractFileNameFromUrl(url: String): String {
        val lastSegment = url.substringAfterLast('?').substringAfterLast('/')
        return if (lastSegment.isNotBlank() && lastSegment.contains('.')) {
            lastSegment
        } else if (lastSegment.isNotBlank()) {
            "$lastSegment.txt"
        } else {
            "github_file_${System.currentTimeMillis()}.txt"
        }
    }
}
