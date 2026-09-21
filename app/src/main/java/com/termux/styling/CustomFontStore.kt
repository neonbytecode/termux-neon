package com.termux.styling

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

data class CustomFont(
    val name: String,
    val file: File,
)

class CustomFontStore(private val context: Context) {

    val fontsDir: File
        get() = File(context.filesDir, "custom_fonts").apply { if (!exists()) mkdirs() }

    fun list(): List<CustomFont> {
        val dir = fontsDir
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.listFiles()
            ?.filter { it.isFile && (it.extension.equals("ttf", ignoreCase = true) || it.extension.equals("otf", ignoreCase = true)) }
            ?.map { CustomFont(name = it.name, file = it) }
            .orEmpty()
    }

    fun importFont(uri: Uri): Result<CustomFont> {
        return runCatching {
            val fileName = resolveFileName(uri) ?: "imported_font_${System.currentTimeMillis()}.ttf"
            val sanitizedName = sanitizeFileName(fileName)
            val destFile = File(fontsDir, sanitizedName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Cannot read content from selected URI")

            CustomFont(name = sanitizedName, file = destFile)
        }
    }

    fun deleteFont(fileName: String): Boolean {
        val file = File(fontsDir, fileName)
        return if (file.isFile) file.delete() else false
    }

    private fun resolveFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        return cursor.getString(index)
                    }
                }
            }
        }
        return uri.lastPathSegment
    }

    private fun sanitizeFileName(original: String): String {
        val clean = original.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        require(clean.removeSuffix(".ttf").removeSuffix(".otf").isNotBlank()) {
            "Font name must contain at least one letter or number."
        }
        return if (clean.endsWith(".ttf", ignoreCase = true) || clean.endsWith(".otf", ignoreCase = true)) {
            clean
        } else {
            "$clean.ttf"
        }
    }
}
