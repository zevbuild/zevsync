package com.example.data.cache

import android.content.Context
import android.net.Uri
import com.example.data.model.FileCategory
import com.example.data.model.StorageBreakdown
import com.example.data.model.SyncedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale

class CacheVaultManager(private val context: Context) {

    val vaultDir: File by lazy {
        val dir = File(context.filesDir, "vault_cache")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    val conflictDir: File by lazy {
        val dir = File(context.filesDir, "vault_conflicts")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    suspend fun saveUriToVault(uri: Uri, originalName: String): Triple<File, String, Long> =
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Cannot open stream for URI: $uri")

            val tempFile = File.createTempFile("beam_import_", ".tmp", context.cacheDir)
            val digest = MessageDigest.getInstance("SHA-256")

            tempFile.outputStream().use { outStream ->
                val buffer = ByteArray(64 * 1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                    outStream.write(buffer, 0, bytesRead)
                }
            }
            inputStream.close()

            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            val finalFile = File(vaultDir, "$hash-${sanitizeFileName(originalName)}")
            if (finalFile.exists()) {
                tempFile.delete()
            } else {
                tempFile.renameTo(finalFile)
            }
            Triple(finalFile, hash, finalFile.length())
        }

    suspend fun saveTextDocument(name: String, content: String): Triple<File, String, Long> =
        withContext(Dispatchers.IO) {
            val bytes = content.toByteArray(Charsets.UTF_8)
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(bytes).joinToString("") { "%02x".format(it) }
            val sanitized = sanitizeFileName(if (name.contains('.')) name else "$name.md")
            val targetFile = File(vaultDir, "$hash-$sanitized")
            targetFile.writeBytes(bytes)
            Triple(targetFile, hash, bytes.size.toLong())
        }

    suspend fun createChunkStream(file: File, chunkSize: Int = 64 * 1024): List<ByteArray> =
        withContext(Dispatchers.IO) {
            if (!file.exists()) return@withContext emptyList()
            val chunks = mutableListOf<ByteArray>()
            FileInputStream(file).use { input ->
                val buffer = ByteArray(chunkSize)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    chunks.add(buffer.copyOf(bytesRead))
                }
            }
            chunks
        }

    suspend fun writeChunkToFile(
        targetFile: File,
        chunkIndex: Int,
        chunkData: ByteArray,
        totalChunks: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            FileOutputStream(targetFile, chunkIndex > 0).use { output ->
                output.write(chunkData)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun calculateSha256(file: File): String = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext ""
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(64 * 1024)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun determineCategory(fileName: String, mimeType: String): FileCategory {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return when {
            mimeType.startsWith("image/") || ext in listOf("jpg", "jpeg", "png", "webp", "gif", "svg", "bmp", "avif") -> FileCategory.IMAGE
            mimeType.startsWith("video/") || ext in listOf("mp4", "mkv", "mov", "webm", "avi", "3gp") -> FileCategory.VIDEO
            mimeType.startsWith("audio/") || ext in listOf("mp3", "wav", "flac", "ogg", "m4a", "aac") -> FileCategory.AUDIO
            ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "csv") -> FileCategory.DOCUMENT
            ext in listOf("kt", "java", "py", "js", "ts", "html", "css", "json", "xml", "md", "c", "cpp", "rs", "go", "sql", "sh", "yaml", "yml") -> FileCategory.CODE
            ext in listOf("zip", "rar", "7z", "tar", "gz", "bz2", "apk") -> FileCategory.ARCHIVE
            else -> FileCategory.OTHER
        }
    }

    suspend fun extractTextPreview(file: File, maxChars: Int = 1000): String? =
        withContext(Dispatchers.IO) {
            if (!file.exists()) return@withContext null
            val ext = file.name.substringAfterLast('.', "").lowercase(Locale.ROOT)
            val isTextLike = ext in listOf("txt", "md", "json", "xml", "kt", "java", "py", "js", "ts", "html", "css", "csv", "sql", "sh", "yaml", "yml", "log")
            if (!isTextLike && file.length() > 500 * 1024) return@withContext null

            try {
                val bytes = ByteArray(maxChars.coerceAtMost(file.length().toInt()))
                FileInputStream(file).use { it.read(bytes) }
                String(bytes, Charsets.UTF_8).take(maxChars)
            } catch (e: Exception) {
                null
            }
        }

    fun calculateStorageBreakdown(files: List<SyncedFile>, quotaBytes: Long): StorageBreakdown {
        var total = 0L
        var img = 0L
        var docs = 0L
        var audio = 0L
        var video = 0L
        var code = 0L
        var archive = 0L
        var other = 0L

        for (file in files) {
            val size = file.sizeBytes
            total += size
            when (file.category) {
                FileCategory.IMAGE -> img += size
                FileCategory.DOCUMENT -> docs += size
                FileCategory.AUDIO -> audio += size
                FileCategory.VIDEO -> video += size
                FileCategory.CODE -> code += size
                FileCategory.ARCHIVE -> archive += size
                FileCategory.OTHER, FileCategory.ALL -> other += size
            }
        }

        return StorageBreakdown(
            totalVaultBytes = total,
            quotaBytes = quotaBytes,
            imagesBytes = img,
            docsBytes = docs,
            audioBytes = audio,
            videoBytes = video,
            codeBytes = code,
            archiveBytes = archive,
            otherBytes = other,
            fileCount = files.size
        )
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}
