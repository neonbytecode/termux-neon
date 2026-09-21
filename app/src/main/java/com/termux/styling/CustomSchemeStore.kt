package com.termux.styling

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dev.neonbytecode.neon.themeengine.AnsiPalette
import dev.neonbytecode.neon.themeengine.TermuxColorsParser
import java.io.File

data class CustomScheme(
    val name: String,
    val palette: AnsiPalette,
    val file: File,
)

class CustomSchemeStore(private val context: Context) {

    val schemesDir: File
        get() = File(context.filesDir, "custom_schemes").apply { if (!exists()) mkdirs() }

    fun list(): List<CustomScheme> {
        val dir = schemesDir
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.listFiles()
            ?.filter { it.isFile && it.extension.equals("properties", ignoreCase = true) }
            ?.mapNotNull { file ->
                runCatching {
                    file.inputStream().use { input ->
                        val palette = TermuxColorsParser.parseProperties(input, file.name.removeSuffix(".properties"))
                        CustomScheme(name = file.name, palette = palette, file = file)
                    }
                }.getOrNull()
            }
            .orEmpty()
    }

    fun saveScheme(palette: AnsiPalette): Result<CustomScheme> {
        return runCatching {
            val fileName = sanitizeFileName(palette.name)
            require(fileName.removeSuffix(".properties").isNotBlank()) {
                "Scheme name must contain at least one letter or number."
            }
            val file = File(schemesDir, fileName)
            val propertiesContent = TermuxColorsParser.toPropertiesString(palette)
            file.writeText(propertiesContent, Charsets.UTF_8)
            CustomScheme(name = fileName, palette = palette.copy(name = fileName.removeSuffix(".properties")), file = file)
        }
    }

    fun importScheme(uri: Uri): Result<CustomScheme> {
        return runCatching {
            val fileName = resolveFileName(uri) ?: "imported_scheme_${System.currentTimeMillis()}.properties"
            val sanitizedName = sanitizeFileName(fileName)
            val destFile = File(schemesDir, sanitizedName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Cannot read content from selected URI")

            val palette = destFile.inputStream().use { input ->
                TermuxColorsParser.parseProperties(input, sanitizedName.removeSuffix(".properties"))
            }
            CustomScheme(name = sanitizedName, palette = palette, file = destFile)
        }
    }

    fun deleteScheme(fileName: String): Boolean {
        val file = File(schemesDir, fileName)
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
        return if (clean.endsWith(".properties", ignoreCase = true)) {
            clean
        } else {
            "$clean.properties"
        }
    }
}
