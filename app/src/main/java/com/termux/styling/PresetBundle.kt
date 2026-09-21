package com.termux.styling

/**
 * Portable, versioned representation of saved presets.
 *
 * The format intentionally stays plain JSON so users can back it up, inspect
 * it, and move it between installations without an app-specific database.
 */
object PresetBundle {
    private const val CURRENT_VERSION = 1

    fun encode(presets: List<StylingPreset>): String =
        """{"version":$CURRENT_VERSION,"presets":${StylingPreset.listToJsonArray(presets)}}"""

    fun decode(raw: String): Result<List<StylingPreset>> = runCatching {
        val version = Regex(""""version"\s*:\s*(\d+)""")
            .find(raw)
            ?.groupValues
            ?.get(1)
            ?.toInt()
            ?: throw IllegalArgumentException("Preset bundle is missing its version.")
        require(version == CURRENT_VERSION) { "Unsupported preset bundle version: $version" }

        val presetsJson = Regex(""""presets"\s*:\s*(\[.*])\s*}""", RegexOption.DOT_MATCHES_ALL)
            .find(raw)
            ?.groupValues
            ?.get(1)
            ?: throw IllegalArgumentException("Preset bundle is missing its presets.")
        StylingPreset.listFromJsonArray(presetsJson)
    }
}
