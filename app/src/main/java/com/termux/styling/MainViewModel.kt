package com.termux.styling

import android.app.Application
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.neonbytecode.neon.termux.AppliedStyleReader
import dev.neonbytecode.neon.termux.BundledStyleCatalog
import dev.neonbytecode.neon.termux.Selectable
import dev.neonbytecode.neon.termux.TermuxEnvironment
import dev.neonbytecode.neon.termux.TermuxStyleWriter
import dev.neonbytecode.neon.themeengine.AnsiPalette
import dev.neonbytecode.neon.themeengine.TermuxColorsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface StatusNote {
    val text: String

    data class Info(override val text: String) : StatusNote
    data class Success(override val text: String) : StatusNote
    data class Error(override val text: String) : StatusNote
}

data class SchemeEntry(
    val selectable: Selectable,
    val palette: AnsiPalette,
)

data class FontEntry(
    val selectable: Selectable,
)

data class UiState(
    val termuxReady: Boolean = false,
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
    val busy: Boolean = false,
    val message: StatusNote? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val catalog = BundledStyleCatalog(application.assets)
    private val reader = AppliedStyleReader(catalog)
    private val writer = TermuxStyleWriter(application, catalog)
    private var environment: TermuxEnvironment.State = TermuxEnvironment.State.NONE

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            environment = TermuxEnvironment.evaluate(context)
            val state = environment

            val schemes = buildList {
                add(SchemeEntry(Selectable(Selectable.DEFAULT_FILENAME), AnsiPalette.DEFAULT))
                catalog.schemeNames().forEach { name ->
                    add(SchemeEntry(Selectable(name), parseScheme(name)))
                }
            }
            val fonts = buildList {
                add(FontEntry(Selectable(Selectable.DEFAULT_FILENAME)))
                catalog.fontNames().forEach { add(FontEntry(Selectable(it))) }
            }

            val applied = if (state.accessible) reader.read(state) else dev.neonbytecode.neon.termux.AppliedStyle.NONE

            _ui.update {
                it.copy(
                    termuxReady = state.accessible,
                    schemes = schemes,
                    fonts = fonts,
                    appliedScheme = applied.schemeName?.let { s -> Selectable(s) },
                    appliedFont = applied.fontName?.let { s -> Selectable(s) },
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

    fun applyScheme() {
        _ui.value.selectedScheme?.let { apply(colors = true, bundle = it.name) }
    }

    fun applyFont() {
        _ui.value.selectedFont?.let { apply(colors = false, bundle = it.name) }
    }

    fun clearMessage() {
        _ui.update { it.copy(message = null) }
    }

    private fun apply(colors: Boolean, bundle: String) {
        val state = environment
        if (!state.accessible) {
            _ui.update {
                it.copy(message = StatusNote.Error("Termux is not installed or not signed with the matching key."))
            }
            return
        }
        _ui.update { it.copy(busy = true, message = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = writer.apply(state, colors, bundle)
            val applied = reader.read(state)
            _ui.update {
                it.copy(
                    busy = false,
                    appliedScheme = applied.schemeName?.let { s -> Selectable(s) },
                    appliedFont = applied.fontName?.let { s -> Selectable(s) },
                    message = result.fold(
                        onSuccess = {
                            val label = Selectable(bundle).displayName
                            StatusNote.Success(
                                if (colors) "Scheme applied → $label" else "Font applied → $label",
                            )
                        },
                        onFailure = { e ->
                            StatusNote.Error("Failed to install: ${e.message ?: e::class.simpleName}")
                        },
                    ),
                )
            }
            rebuildPreview()
        }
    }

    private fun rebuildPreview() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _ui.value
            val schemeKey = current.selectedScheme ?: current.appliedScheme
            val fontKey = current.selectedFont ?: current.appliedFont

            val palette: AnsiPalette
            val schemeLabel: String
            if (schemeKey != null) {
                palette = current.schemes.firstOrNull { it.selectable == schemeKey }?.palette
                    ?: parseScheme(schemeKey.name)
                schemeLabel = schemeKey.displayName
            } else {
                palette = reader.installedPalette(environment) ?: AnsiPalette.DEFAULT
                schemeLabel = "default"
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
                )
            }
        }
    }

    private fun parseScheme(name: String): AnsiPalette = runCatching {
        TermuxColorsParser.parseProperties(catalog.schemeStream(name), Selectable(name).displayName)
    }.getOrElse { AnsiPalette.DEFAULT }

    private fun loadFontFromAssets(name: String): FontFamily = runCatching {
        FontFamily(Typeface.createFromAsset(context.assets, "fonts/$name"))
    }.getOrElse { FontFamily.Monospace }

    private fun readInstalledFont(): FontFamily? = runCatching {
        val file = reader.read(environment).installedFontFile
        if (file != null && file.isFile && file.length() > 0L) {
            FontFamily(Typeface.createFromFile(file))
        } else null
    }.getOrNull()
}