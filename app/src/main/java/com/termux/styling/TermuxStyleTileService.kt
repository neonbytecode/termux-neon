package com.termux.styling

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dev.neonbytecode.neon.termux.AppliedStyleReader
import dev.neonbytecode.neon.termux.BundledStyleCatalog
import dev.neonbytecode.neon.termux.TermuxEnvironment
import dev.neonbytecode.neon.termux.TermuxStyleWriter

class TermuxStyleTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        cycleNextStyle()
    }

    fun updateTileState() {
        val tile = qsTile ?: return
        val env = TermuxEnvironment.evaluate(this)

        tile.icon = Icon.createWithResource(this, R.drawable.ic_launcher)

        if (!env.accessible) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "Offline"
            tile.contentDescription = "Termux is unavailable"
            tile.updateTile()
            return
        }

        val catalog = BundledStyleCatalog(assets)
        val currentStyle = AppliedStyleReader(catalog).read(env)
        val presets = PresetsStore(this).list()
        val label = resolveTileLabel(currentStyle.schemeName, presets)

        tile.state = Tile.STATE_ACTIVE
        tile.label = label
        tile.contentDescription = "Current Termux theme: $label"
        tile.updateTile()
    }

    fun cycleNextStyle() {
        val env = TermuxEnvironment.evaluate(this)
        if (!env.accessible) return

        val catalog = BundledStyleCatalog(assets)
        val writer = TermuxStyleWriter(this, catalog)
        val presets = PresetsStore(this).list()

        if (presets.isNotEmpty()) {
            val currentStyle = AppliedStyleReader(catalog).read(env)
            val currentIndex = presets.indexOfFirst { it.schemeName == currentStyle.schemeName }
            val nextPreset = presets[computeNextIndex(currentIndex, presets.size)]
            writer.applyAll(
                state = env,
                schemeName = nextPreset.schemeName,
                fontName = nextPreset.fontName,
                foregroundHex = nextPreset.textColorOverride?.let { color ->
                    String.format("#%06X", color and 0xFFFFFF)
                },
            )
        } else {
            val schemes = catalog.schemeNames()
            if (schemes.isEmpty()) {
                updateTileState()
                return
            }

            val currentStyle = AppliedStyleReader(catalog).read(env)
            val currentIndex = schemes.indexOf(currentStyle.schemeName ?: "")
            val nextScheme = schemes[computeNextIndex(currentIndex, schemes.size)]
            writer.apply(state = env, isColors = true, bundleName = nextScheme)
        }

        updateTileState()
    }

    companion object {
        fun computeNextIndex(currentIndex: Int, totalSize: Int): Int {
            if (totalSize <= 0) return 0
            val safeIndex = if (currentIndex < 0) -1 else currentIndex
            return (safeIndex + 1) % totalSize
        }

        fun resolveTileLabel(currentSchemeName: String?, presets: List<StylingPreset>): String {
            val presetMatch = presets.firstOrNull { it.schemeName == currentSchemeName }
            val schemeLabel = currentSchemeName
                ?.removeSuffix(".properties")
                ?.takeIf { it.isNotBlank() }
                ?.replace("_", " ")
                ?.trim()
                ?: "Termux"

            return presetMatch?.name?.takeIf { it.isNotBlank() }
                ?: schemeLabel
        }
    }
}
