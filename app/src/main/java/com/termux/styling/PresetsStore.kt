package com.termux.styling

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

data class StylingPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val schemeName: String?,
    val fontName: String?,
    val textColorOverride: Int? = null,
) {
    fun toJson(): String {
        val sScheme = schemeName?.let { "\"${escape(it)}\"" } ?: "null"
        val sFont = fontName?.let { "\"${escape(it)}\"" } ?: "null"
        val sColor = textColorOverride?.toString() ?: "null"
        return """{"id":"${escape(id)}","name":"${escape(name)}","schemeName":$sScheme,"fontName":$sFont,"textColorOverride":$sColor}"""
    }

    companion object {
        private fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")

        fun fromJson(jsonStr: String): StylingPreset? = runCatching {
            val id = extractValue(jsonStr, "id") ?: return null
            val name = extractValue(jsonStr, "name") ?: "Preset"
            val schemeName = extractValue(jsonStr, "schemeName")
            val fontName = extractValue(jsonStr, "fontName")
            val textColorOverride = extractValue(jsonStr, "textColorOverride")?.toIntOrNull()
            StylingPreset(id, name, schemeName, fontName, textColorOverride)
        }.getOrNull()

        private fun extractValue(json: String, key: String): String? {
            val pattern = """"$key"\s*:\s*("(.*?)"|null|(-?\d+))""".toRegex()
            val match = pattern.find(json) ?: return null
            val raw = match.groupValues[1]
            if (raw == "null") return null
            if (raw.startsWith("\"")) {
                return match.groupValues[2].replace("\\\"", "\"").replace("\\\\", "\\")
            }
            return raw
        }

        fun listFromJsonArray(rawArray: String): List<StylingPreset> {
            if (rawArray.isBlank()) return emptyList()
            val objectPattern = """\{[^{}]*\}""".toRegex()
            return objectPattern.findAll(rawArray).mapNotNull { fromJson(it.value) }.toList()
        }

        fun listToJsonArray(presets: List<StylingPreset>): String {
            return presets.joinToString(separator = ",", prefix = "[", postfix = "]") { it.toJson() }
        }
    }
}

class PresetsStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun list(): List<StylingPreset> {
        val raw = prefs.getString(KEY_PRESETS, null) ?: return emptyList()
        return StylingPreset.listFromJsonArray(raw)
    }

    fun save(preset: StylingPreset): List<StylingPreset> {
        val current = list().toMutableList()
        current.removeAll { it.id == preset.id }
        current.add(0, preset)
        persist(current)
        return current
    }

    fun delete(id: String): List<StylingPreset> {
        val current = list().toMutableList()
        current.removeAll { it.id == id }
        persist(current)
        return current
    }

    fun export(): String = PresetBundle.encode(list())

    fun import(raw: String): Result<List<StylingPreset>> = PresetBundle.decode(raw).map { incoming ->
        val merged = (incoming + list())
            .distinctBy { it.id }
            .take(MAX_PRESETS)
        persist(merged)
        merged
    }

    private fun persist(presets: List<StylingPreset>) {
        val json = StylingPreset.listToJsonArray(presets)
        prefs.edit { putString(KEY_PRESETS, json) }
    }

    companion object {
        private const val PREFS_NAME = "neon_presets"
        private const val KEY_PRESETS = "presets_json"
        private const val MAX_PRESETS = 100
    }
}
