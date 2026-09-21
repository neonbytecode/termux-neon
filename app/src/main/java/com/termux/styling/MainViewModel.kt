package com.termux.styling

import android.app.Application
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.neonbytecode.neon.termux.AppliedStyleReader
import dev.neonbytecode.neon.termux.BundledStyleCatalog
import dev.neonbytecode.neon.termux.Selectable
import dev.neonbytecode.neon.termux.TermuxEnvironment
import dev.neonbytecode.neon.termux.TermuxStyleWriter
import dev.neonbytecode.neon.themeengine.AnsiPalette
import dev.neonbytecode.neon.themeengine.PreviewMode
import dev.neonbytecode.neon.themeengine.TermuxColorsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

sealed interface StatusNote {
    val text: String

    data class Info(override val text: String) : StatusNote
    data class Success(override val text: String) : StatusNote
    data class Error(override val text: String) : StatusNote
}

data class SchemeEntry(
    val selectable: Selectable,
    val palette: AnsiPalette,
    val isCustom: Boolean = false,
)

data class FontEntry(
    val selectable: Selectable,
    val isCustom: Boolean = false,
)

enum class ScreenStatus {
    IDLE,
    LOADING,
    READY,
    APPLYING,
    ERROR,
}

data class UiState(
    val termuxReady: Boolean = false,
    val termuxProblem: TermuxEnvironment.AccessProblem = TermuxEnvironment.AccessProblem.NONE,
    val schemes: List<SchemeEntry> = emptyList(),
    val fonts: List<FontEntry> = emptyList(),
    val selectedScheme: Selectable? = null,
    val selectedFont: Selectable? = null,
    val appliedScheme: Selectable? = null,
    val appliedFont: Selectable? = null,
    val previewPalette: AnsiPalette = AnsiPalette.DEFAULT,
    val previewFont: FontFamily = FontFamily.Monospace,
    val previewSchemeName: String = "default",
    val previewFontName: String = "monospace",
    val themeHealth: ThemeHealth = ThemeHealth(21.0, true, emptyList()),
    val previewMode: PreviewMode = PreviewMode.PROMPT,
    val previewFontSizeSp: Float = 12.5f,
    val customPreviewText: String = "",
    val presets: List<StylingPreset> = emptyList(),
    val screenStatus: ScreenStatus = ScreenStatus.IDLE,
    val busy: Boolean = false,
    val message: StatusNote? = null,
    val query: String = "",
    val favoriteSchemes: Set<String> = emptySet(),
    val favoriteFonts: Set<String> = emptySet(),
    /** Null means no override picked yet — the preview shows whatever scheme/applied foreground is active. */
    val textColorOverride: Int? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val catalog = BundledStyleCatalog(application.assets)
    private val reader = AppliedStyleReader(catalog)
    private val writer = TermuxStyleWriter(application, catalog)
    private val favorites = FavoritesStore(application)
    private val presetsStore = PresetsStore(application)
    private val customFontStore = CustomFontStore(application)
    private val customSchemeStore = CustomSchemeStore(application)
    private var environment: TermuxEnvironment.State = TermuxEnvironment.State.NONE

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    fun refresh() {
        _ui.update { it.copy(screenStatus = ScreenStatus.LOADING, busy = false) }

        viewModelScope.launch(Dispatchers.IO) {
            environment = TermuxEnvironment.evaluate(context)
            val state = environment

            val schemes = buildList {
                add(SchemeEntry(Selectable(Selectable.DEFAULT_FILENAME), AnsiPalette.DEFAULT))
                catalog.schemeNames().forEach { name ->
                    add(SchemeEntry(Selectable(name), parseScheme(name)))
                }
                customSchemeStore.list().forEach { customScheme ->
                    add(SchemeEntry(Selectable(customScheme.name), customScheme.palette, isCustom = true))
                }
            }
            val fonts = buildList {
                add(FontEntry(Selectable(Selectable.DEFAULT_FILENAME)))
                catalog.fontNames().forEach { add(FontEntry(Selectable(it))) }
                customFontStore.list().forEach { customFont ->
                    add(FontEntry(Selectable(customFont.name), isCustom = true))
                }
            }

            val applied = if (state.accessible) reader.read(state) else dev.neonbytecode.neon.termux.AppliedStyle.NONE

            _ui.update {
                it.copy(
                    termuxReady = state.accessible,
                    termuxProblem = state.problem,
                    schemes = schemes,
                    fonts = fonts,
                    appliedScheme = applied.schemeName?.let { s -> Selectable(s) },
                    appliedFont = applied.fontName?.let { s -> Selectable(s) },
                    favoriteSchemes = favorites.schemes(),
                    favoriteFonts = favorites.fonts(),
                    presets = presetsStore.list(),
                    screenStatus = if (state.accessible) ScreenStatus.READY else ScreenStatus.ERROR,
                    busy = false,
                )
            }
            rebuildPreview()
        }
    }

    fun selectScheme(item: Selectable?) {
        _ui.update { it.copy(selectedScheme = item) }
        rebuildPreview()
    }

    fun selectFont(item: Selectable?) {
        _ui.update { it.copy(selectedFont = item) }
        rebuildPreview()
    }

    fun setQuery(text: String) {
        _ui.update { it.copy(query = text) }
    }

    fun toggleFavoriteScheme(name: String) {
        _ui.update { it.copy(favoriteSchemes = favorites.toggleScheme(name)) }
    }

    fun toggleFavoriteFont(name: String) {
        _ui.update { it.copy(favoriteFonts = favorites.toggleFont(name)) }
    }

    /** Previews a random scheme + font combo; still requires APPLY to commit. */
    fun shuffle() {
        val current = _ui.value
        val scheme = current.schemes.map { it.selectable }.filter { it.name != Selectable.DEFAULT_FILENAME }.randomOrNull()
        val font = current.fonts.map { it.selectable }.filter { it.name != Selectable.DEFAULT_FILENAME }.randomOrNull()
        _ui.update { it.copy(selectedScheme = scheme ?: it.selectedScheme, selectedFont = font ?: it.selectedFont) }
        rebuildPreview()
    }

    fun smartPick() {
        val recommendedScheme = ThemeRecommendationEngine.recommendSchemeName()
        if (recommendedScheme == null) return

        _ui.update {
            it.copy(
                selectedScheme = Selectable(recommendedScheme),
                message = StatusNote.Info("Smart pick: ${recommendedScheme.removeSuffix(".properties").replace("-", " ").replace("_", " ")}"),
            )
        }
        rebuildPreview()
    }

    fun applyAll() {
        val state = environment
        if (!state.accessible) {
            _ui.update {
                it.copy(
                    screenStatus = ScreenStatus.ERROR,
                    busy = false,
                    message = StatusNote.Error("Termux is not installed or not signed with the matching key."),
                )
            }
            return
        }
        _ui.update { it.copy(screenStatus = ScreenStatus.APPLYING, busy = true, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val current = _ui.value
            val hex = current.textColorOverride?.let { String.format("#%06X", it and 0xFFFFFF) }
            val result = writer.applyAll(
                state = state,
                schemeName = current.selectedScheme?.name,
                fontName = current.selectedFont?.name,
                foregroundHex = hex,
            )
            val applied = reader.read(state)

            _ui.update {
                it.copy(
                    screenStatus = if (result.isSuccess) ScreenStatus.READY else ScreenStatus.ERROR,
                    busy = false,
                    appliedScheme = applied.schemeName?.let { s -> Selectable(s) },
                    appliedFont = applied.fontName?.let { s -> Selectable(s) },
                    message = if (result.isSuccess) {
                        val appliedParts = buildList {
                            if (current.selectedScheme != null) add("Scheme")
                            if (current.selectedFont != null) add("Font")
                            if (current.textColorOverride != null) add("Text Color")
                        }.joinToString(" + ")
                        StatusNote.Success("All styles applied: $appliedParts")
                    } else {
                        StatusNote.Error("Nothing was changed: ${result.exceptionOrNull()?.message ?: "apply failed"}")
                    },
                )
            }
            rebuildPreview()
        }
    }

    fun clearMessage() {
        _ui.update {
            it.copy(
                message = null,
                screenStatus = if (it.busy) ScreenStatus.APPLYING else ScreenStatus.READY,
            )
        }
    }

    /** Tapping the already-selected swatch clears the override (toggle, not a separate reset action). */
    fun selectTextColor(argb: Int) {
        _ui.update { it.copy(textColorOverride = if (it.textColorOverride == argb) null else argb) }
        rebuildPreview()
    }

    fun setPreviewMode(mode: PreviewMode) {
        _ui.update { it.copy(previewMode = mode) }
    }

    fun setPreviewFontSize(sizeSp: Float) {
        _ui.update { it.copy(previewFontSizeSp = sizeSp) }
    }

    fun setCustomPreviewText(text: String) {
        _ui.update { it.copy(customPreviewText = text) }
    }

    fun saveCurrentPreset(name: String) {
        val current = _ui.value
        val newPreset = StylingPreset(
            name = name.ifBlank { "Custom Preset" },
            schemeName = current.selectedScheme?.name ?: current.appliedScheme?.name,
            fontName = current.selectedFont?.name ?: current.appliedFont?.name,
            textColorOverride = current.textColorOverride,
        )
        val updated = presetsStore.save(newPreset)
        _ui.update { it.copy(presets = updated, message = StatusNote.Success("Saved preset: ${newPreset.name}")) }
    }

    fun applyPreset(preset: StylingPreset) {
        val current = _ui.value
        val scheme = preset.schemeName?.let { name -> current.schemes.firstOrNull { it.selectable.name == name }?.selectable }
        val font = preset.fontName?.let { name -> current.fonts.firstOrNull { it.selectable.name == name }?.selectable }
        _ui.update {
            it.copy(
                selectedScheme = scheme,
                selectedFont = font,
                textColorOverride = preset.textColorOverride,
                message = StatusNote.Info("Loaded preset: ${preset.name}"),
            )
        }
        rebuildPreview()
    }

    fun deletePreset(id: String) {
        val updated = presetsStore.delete(id)
        _ui.update { it.copy(presets = updated, message = StatusNote.Info("Preset deleted")) }
    }

    fun exportPresets(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(presetsStore.export().toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("Cannot write to the selected location.")
                StatusNote.Success("Presets exported")
            }.getOrElse { error ->
                StatusNote.Error("Preset export failed: ${error.message ?: "unknown error"}")
            }
            _ui.update { it.copy(message = note) }
        }
    }

    fun importPresets(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = runCatching {
                val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: throw IllegalStateException("Cannot read the selected file.")
                val imported = presetsStore.import(raw).getOrThrow()
                _ui.update { it.copy(presets = imported) }
                StatusNote.Success("Imported ${imported.size} preset(s)")
            }.getOrElse { error ->
                StatusNote.Error("Preset import failed: ${error.message ?: "unknown error"}")
            }
            _ui.update { it.copy(message = note) }
        }
    }

    fun importFont(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = customFontStore.importFont(uri)
            val note = if (result.isSuccess) {
                val customFont = result.getOrThrow()
                StatusNote.Success("Imported font: ${customFont.name}")
            } else {
                StatusNote.Error("Font import failed: ${result.exceptionOrNull()?.message}")
            }
            _ui.update { it.copy(message = note) }
            refresh()
        }
    }

    fun deleteCustomFont(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val deleted = customFontStore.deleteFont(name)
            if (deleted) {
                _ui.update {
                    val newSelected = if (it.selectedFont?.name == name) null else it.selectedFont
                    it.copy(selectedFont = newSelected, message = StatusNote.Info("Custom font removed"))
                }
                refresh()
            }
        }
    }

    fun importScheme(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = customSchemeStore.importScheme(uri)
            val note = if (result.isSuccess) {
                val customScheme = result.getOrThrow()
                StatusNote.Success("Imported scheme: ${customScheme.palette.name}")
            } else {
                StatusNote.Error("Scheme import failed: ${result.exceptionOrNull()?.message}")
            }
            _ui.update { it.copy(message = note) }
            refresh()
        }
    }

    fun saveCustomScheme(palette: AnsiPalette) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = customSchemeStore.saveScheme(palette)
            if (result.isSuccess) {
                val saved = result.getOrThrow()
                _ui.update {
                    it.copy(
                        selectedScheme = Selectable(saved.name),
                        message = StatusNote.Success("Saved scheme: ${saved.palette.name}"),
                    )
                }
                refresh()
            } else {
                _ui.update { StatusNote.Error("Failed to save custom scheme").let { note -> it.copy(message = note) } }
            }
        }
    }

    fun deleteCustomScheme(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val deleted = customSchemeStore.deleteScheme(name)
            if (deleted) {
                _ui.update {
                    val newSelected = if (it.selectedScheme?.name == name) null else it.selectedScheme
                    it.copy(selectedScheme = newSelected, message = StatusNote.Info("Custom scheme removed"))
                }
                refresh()
            }
        }
    }

    private fun rebuildPreview() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _ui.value
            val schemeKey = current.selectedScheme ?: current.appliedScheme
            val fontKey = current.selectedFont ?: current.appliedFont

            var palette: AnsiPalette
            val schemeLabel: String
            if (schemeKey != null) {
                palette = current.schemes.firstOrNull { it.selectable == schemeKey }?.palette
                    ?: parseScheme(schemeKey.name)
                schemeLabel = schemeKey.displayName
            } else {
                palette = reader.installedPalette(environment) ?: AnsiPalette.DEFAULT
                schemeLabel = "default"
            }
            current.textColorOverride?.let { override ->
                palette = palette.copy(foreground = override)
            }

            val font: FontFamily
            val fontLabel: String
            if (fontKey != null && fontKey.name != Selectable.DEFAULT_FILENAME) {
                font = loadFontFromAssets(fontKey.name)
                fontLabel = fontKey.displayName
            } else {
                val installed = readInstalledFont()
                font = installed ?: FontFamily.Monospace
                fontLabel = if (installed != null) "custom" else "monospace"
            }

            _ui.update {
                it.copy(
                    previewPalette = palette,
                    previewFont = font,
                    previewSchemeName = schemeLabel,
                    previewFontName = fontLabel,
                    themeHealth = ThemeHealthChecker.inspect(palette),
                )
            }
        }
    }

    private fun parseScheme(name: String): AnsiPalette = runCatching {
        TermuxColorsParser.parseProperties(catalog.schemeStream(name), Selectable(name).displayName)
    }.getOrElse { AnsiPalette.DEFAULT }

    private fun loadFontFromAssets(name: String): FontFamily = runCatching {
        val customFile = File(customFontStore.fontsDir, name)
        if (customFile.isFile && customFile.length() > 0L) {
            FontFamily(Typeface.createFromFile(customFile))
        } else {
            FontFamily(Typeface.createFromAsset(context.assets, "fonts/$name"))
        }
    }.getOrElse { FontFamily.Monospace }

    private fun readInstalledFont(): FontFamily? = runCatching {
        val file = reader.read(environment).installedFontFile
        if (file != null && file.isFile && file.length() > 0L) {
            FontFamily(Typeface.createFromFile(file))
        } else null
    }.getOrNull()
}