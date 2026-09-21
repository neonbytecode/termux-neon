package com.termux.styling

import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import dev.neonbytecode.neon.themeengine.PreviewMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.neonbytecode.neon.designsystem.FavoriteStar
import dev.neonbytecode.neon.designsystem.GlowText
import dev.neonbytecode.neon.designsystem.GridBackdrop
import dev.neonbytecode.neon.designsystem.NeonButton
import dev.neonbytecode.neon.designsystem.NeonCard
import dev.neonbytecode.neon.designsystem.NeonCyan
import dev.neonbytecode.neon.designsystem.NeonGreen
import dev.neonbytecode.neon.designsystem.NeonMagenta
import dev.neonbytecode.neon.designsystem.NeonRed
import dev.neonbytecode.neon.designsystem.NeonBg
import dev.neonbytecode.neon.designsystem.NeonBgAlt
import dev.neonbytecode.neon.designsystem.NeonAmber
import dev.neonbytecode.neon.designsystem.NeonSearchField
import dev.neonbytecode.neon.designsystem.NeonSurfaceHigh
import dev.neonbytecode.neon.designsystem.NeonTextSecondary
import dev.neonbytecode.neon.designsystem.NeonTheme
import dev.neonbytecode.neon.designsystem.ScanlineOverlay
import dev.neonbytecode.neon.designsystem.SectionLabel
import dev.neonbytecode.neon.designsystem.StatusPill
import dev.neonbytecode.neon.designsystem.TerminalPreview
import dev.neonbytecode.neon.termux.Selectable
import dev.neonbytecode.neon.termux.TermuxEnvironment
import dev.neonbytecode.neon.themeengine.AnsiPalette
import dev.neonbytecode.neon.themeengine.TermuxColorsParser

import kotlinx.coroutines.delay
import java.io.File

@Composable
fun NeonScreen(viewModel: MainViewModel, onBack: () -> Unit = {}) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    LaunchedEffect(ui.message) {
        if (ui.message != null) {
            delay(5000)
            viewModel.clearMessage()
        }

    }

    // Re-sync applied badges / preview on every resume so external edits (e.g.
    // `echo` into ~/.termux/colors.properties from a Termux shell) show up.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    // Gesture/hardware back always deliberately returns to Termux rather than
    // relying on the default back-stack pop, which can land elsewhere
    // depending on how this activity was launched.
    BackHandler(onBack = onBack)

    NeonStylingScreen(
        ui = ui,
        onSelectScheme = viewModel::selectScheme,
        onSelectFont = viewModel::selectFont,
        onQueryChange = viewModel::setQuery,
        onToggleFavoriteScheme = viewModel::toggleFavoriteScheme,
        onToggleFavoriteFont = viewModel::toggleFavoriteFont,
        onShuffle = viewModel::shuffle,
        onSmartPick = viewModel::smartPick,
        onBack = onBack,
        onTextColorSelect = viewModel::selectTextColor,
        onApplyAll = viewModel::applyAll,
        onPreviewModeSelect = viewModel::setPreviewMode,
        onFontSizeSelect = viewModel::setPreviewFontSize,
        onCustomTextChange = viewModel::setCustomPreviewText,
        onSavePreset = viewModel::saveCurrentPreset,
        onApplyPreset = viewModel::applyPreset,
        onDeletePreset = viewModel::deletePreset,
        onImportPresets = viewModel::importPresets,
        onExportPresets = viewModel::exportPresets,
        onImportFont = viewModel::importFont,
        onDeleteCustomFont = viewModel::deleteCustomFont,
        onImportScheme = viewModel::importScheme,
        onSaveCustomScheme = viewModel::saveCustomScheme,
        onDeleteCustomScheme = viewModel::deleteCustomScheme,
    )
}

@Composable
private fun ThemeHealthBadge(health: ThemeHealth) {
    val accent = if (health.readable) NeonGreen else NeonAmber
    val text = if (health.readable) {
        "READABILITY OK • ${"%.1f".format(health.contrastRatio)}:1 contrast"
    } else {
        "CHECK CONTRAST • ${"%.1f".format(health.contrastRatio)}:1"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = accent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun NeonStylingScreen(
    ui: UiState,
    onSelectScheme: (Selectable) -> Unit,
    onSelectFont: (Selectable) -> Unit,
    onQueryChange: (String) -> Unit = {},
    onToggleFavoriteScheme: (String) -> Unit = {},
    onToggleFavoriteFont: (String) -> Unit = {},
    onShuffle: () -> Unit = {},
    onSmartPick: () -> Unit = {},
    onBack: () -> Unit = {},
    onTextColorSelect: (Int) -> Unit = {},
    onApplyAll: () -> Unit = {},
    onPreviewModeSelect: (PreviewMode) -> Unit = {},
    onFontSizeSelect: (Float) -> Unit = {},
    onCustomTextChange: (String) -> Unit = {},
    onSavePreset: (String) -> Unit = {},
    onApplyPreset: (StylingPreset) -> Unit = {},
    onDeletePreset: (String) -> Unit = {},
    onImportPresets: (Uri) -> Unit = {},
    onExportPresets: (Uri) -> Unit = {},
    onImportFont: (Uri) -> Unit = {},
    onDeleteCustomFont: (String) -> Unit = {},
    onImportScheme: (Uri) -> Unit = {},
    onSaveCustomScheme: (AnsiPalette) -> Unit = {},
    onDeleteCustomScheme: (String) -> Unit = {},
) {
    val fontPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onImportFont)
    }

    val schemePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onImportScheme)
    }
    val presetImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(onImportPresets)
    }
    val presetExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(onExportPresets)
    }
    var showSchemeEditorDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NeonBgAlt, NeonBg))),
    ) {
        GridBackdrop(modifier = Modifier.fillMaxSize())
        ScanlineOverlay(strength = 0.35f, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            NeonHeader(termuxReady = ui.termuxReady, onBack = onBack)
            Spacer(modifier = Modifier.height(18.dp))
            TerminalPreview(
                palette = ui.previewPalette,
                schemeName = ui.previewSchemeName,
                fontName = ui.previewFontName,
                font = ui.previewFont,
                mode = ui.previewMode,
                customText = ui.customPreviewText,
                fontSize = ui.previewFontSizeSp.sp,
            )
            ThemeHealthBadge(ui.themeHealth)
            Spacer(modifier = Modifier.height(10.dp))
            PreviewControls(
                mode = ui.previewMode,
                fontSizeSp = ui.previewFontSizeSp,
                customText = ui.customPreviewText,
                onModeSelect = onPreviewModeSelect,
                onFontSizeSelect = onFontSizeSelect,
                onCustomTextChange = onCustomTextChange,
            )
            Spacer(modifier = Modifier.height(18.dp))
            FilterBar(query = ui.query, onQueryChange = onQueryChange, onShuffle = onShuffle, onSmartPick = onSmartPick)
            Spacer(modifier = Modifier.height(22.dp))
            PresetsSection(
                presets = ui.presets,
                onSavePreset = onSavePreset,
                onApplyPreset = onApplyPreset,
                onDeletePreset = onDeletePreset,
                onImportPresets = { presetImportLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                onExportPresets = { presetExportLauncher.launch("termux-neon-presets.json") },
            )
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionLabel("COLOR SCHEMES")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                            .clickable { schemePickerLauncher.launch("*/*") }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                    ) {
                        Text("+ IMPORT", style = MaterialTheme.typography.labelSmall, color = NeonCyan, letterSpacing = 1.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                            .clickable { showSchemeEditorDialog = true }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                    ) {
                        Text("+ EDIT", style = MaterialTheme.typography.labelSmall, color = NeonGreen, letterSpacing = 1.sp)
                    }
                }
            }
            SchemeRow(ui, onSelectScheme, onToggleFavoriteScheme, onDeleteCustomScheme)
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionLabel("FONTS")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonMagenta.copy(alpha = 0.15f))
                        .border(1.dp, NeonMagenta, RoundedCornerShape(8.dp))
                        .clickable { fontPickerLauncher.launch("*/*") }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = "+ IMPORT FONT",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonMagenta,
                        letterSpacing = 1.sp,
                    )
                }

            }
            FontRow(ui, onSelectFont, onToggleFavoriteFont, onDeleteCustomFont)
            Spacer(modifier = Modifier.height(22.dp))
            SectionLabel("TEXT COLOR")
            TextColorSection(ui, onTextColorSelect)
            // Trailing clearance so the floating apply dock never covers content.
            Spacer(modifier = Modifier.height(230.dp))
        }

        FloatingApplyDock(
            ui = ui,
            onApplyAll = onApplyAll,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showSchemeEditorDialog) {
        SchemeEditorDialog(
            onDismiss = { showSchemeEditorDialog = false },
            onConfirm = { palette ->
                onSaveCustomScheme(palette)
                showSchemeEditorDialog = false
            },
        )
    }
}

@Preview(
    name = "Termux Neon style control",
    showBackground = true,
    showSystemUi = true,
    widthDp = 412,
    heightDp = 915,
)
@Composable
private fun NeonStylingScreenPreview() {
    val previewPalette = AnsiPalette(
        colors = listOf(
            0xFF10131C.toInt(), 0xFFFF5370.toInt(), 0xFFC3E88D.toInt(), 0xFFFFCB6B.toInt(),
            0xFF82AAFF.toInt(), 0xFFC792EA.toInt(), 0xFF89DDFF.toInt(), 0xFFEEFFFF.toInt(),
            0xFF434758.toInt(), 0xFFFF5370.toInt(), 0xFFC3E88D.toInt(), 0xFFFFCB6B.toInt(),
            0xFF82AAFF.toInt(), 0xFFC792EA.toInt(), 0xFF89DDFF.toInt(), 0xFFFFFFFF.toInt(),
        ),
        foreground = 0xFFEEFFFF.toInt(),
        background = 0xFF10131C.toInt(),
        cursor = 0xFF82AAFF.toInt(),
        name = "Preview",
    )
    val scheme = Selectable("preview.properties")
    val font = Selectable("JetBrainsMono.ttf")
    val previewState = UiState(
        termuxReady = true,
        schemes = listOf(SchemeEntry(scheme, previewPalette)),
        fonts = listOf(FontEntry(font)),
        selectedScheme = scheme,
        selectedFont = font,
        appliedScheme = scheme,
        appliedFont = font,
        previewPalette = previewPalette,
        previewSchemeName = "Preview",
        previewFontName = "JetBrains Mono",
        themeHealth = ThemeHealth(8.7, true, emptyList()),
        screenStatus = ScreenStatus.READY,
    )

    NeonTheme {
        NeonStylingScreen(
            ui = previewState,
            onSelectScheme = {},
            onSelectFont = {},
        )
    }
}

@Composable
private fun NeonHeader(termuxReady: Boolean, onBack: () -> Unit) {
    Column {
        Text(
            text = "‹ BACK TO TERMUX",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = NeonTextSecondary,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onBack() }
                .semantics {
                    role = Role.Button
                    contentDescription = "Back to Termux"
                }
                .padding(vertical = 6.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "TERMUX // STYLE CONTROL",
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 3.sp,
                    color = NeonTextSecondary,
                )
                GlowText(text = "NEON", color = NeonCyan, style = MaterialTheme.typography.displayMedium)
            }
            StatusPill(
                text = if (termuxReady) "TERMUX LINKED" else "TERMUX OFFLINE",
                tone = if (termuxReady) NeonGreen else NeonRed,
            )
        }
    }
}

@Composable
private fun PreviewControls(
    mode: PreviewMode,
    fontSizeSp: Float,
    customText: String,
    onModeSelect: (PreviewMode) -> Unit,
    onFontSizeSelect: (Float) -> Unit,
    onCustomTextChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                items(PreviewMode.entries.toTypedArray()) { itemMode ->
                    val selected = mode == itemMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) NeonCyan.copy(alpha = 0.2f) else NeonSurfaceHigh)
                            .border(
                                width = 1.dp,
                                color = if (selected) NeonCyan else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                            )
                            .clickable { onModeSelect(itemMode) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = itemMode.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) NeonCyan else NeonTextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(10f, 12.5f, 15f, 18f).forEach { sizeSp ->
                    val selected = fontSizeSp == sizeSp
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) NeonMagenta.copy(alpha = 0.25f) else NeonSurfaceHigh)
                            .border(
                                width = 1.dp,
                                color = if (selected) NeonMagenta else Color.Transparent,
                                shape = CircleShape,
                            )
                            .clickable { onFontSizeSelect(sizeSp) }
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = if (sizeSp == 12.5f) "12.5p" else "${sizeSp.toInt()}p",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (selected) NeonMagenta else NeonTextSecondary,
                        )
                    }
                }
            }
        }

        if (mode == PreviewMode.CUSTOM) {
            NeonSearchField(
                value = customText,
                onValueChange = onCustomTextChange,
                placeholder = "type custom preview text...",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PresetsSection(
    presets: List<StylingPreset>,
    onSavePreset: (String) -> Unit,
    onApplyPreset: (StylingPreset) -> Unit,
    onDeletePreset: (String) -> Unit,
    onImportPresets: () -> Unit,
    onExportPresets: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel("SAVED PRESETS")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PresetAction("IMPORT", NeonMagenta, onImportPresets)
                PresetAction("EXPORT", NeonGreen, onExportPresets)
                PresetAction("+ SAVE", NeonCyan) { showDialog = true }
            }
        }

        if (presets.isEmpty()) {
            NeonCard(selected = false, accent = NeonCyan.copy(alpha = 0.3f)) {
                Text(
                    text = "No saved presets yet. Tap '+ SAVE PRESET' to save your current scheme, font & text color setup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp),
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(presets, key = { it.id }) { preset ->
                    PresetChip(
                        preset = preset,
                        onApply = { onApplyPreset(preset) },
                        onDelete = { onDeletePreset(preset.id) },
                    )
                }
            }
        }
    }

    if (showDialog) {
        SavePresetDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name ->
                onSavePreset(name)
                showDialog = false
            },
        )
    }
}

@Composable
private fun PresetAction(label: String, accent: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.15f))
            .border(1.dp, accent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = accent, letterSpacing = 1.sp)
    }
}

@Composable
private fun PresetChip(
    preset: StylingPreset,
    onApply: () -> Unit,
    onDelete: () -> Unit,
) {
    NeonCard(
        modifier = Modifier.width(140.dp).height(88.dp),
        selected = false,
        accent = NeonCyan,
        onClick = onApply,
        contentDescription = "Preset ${preset.name}",
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonRed,
                    modifier = Modifier
                        .clickable { onDelete() }
                        .padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = preset.schemeName?.removeSuffix(".properties") ?: "default scheme",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = NeonTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = preset.fontName?.removeSuffix(".ttf") ?: "default font",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = NeonMagenta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SavePresetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var presetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "SAVE PRESET",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter a name for this style combination:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                NeonSearchField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    placeholder = "e.g. Cyber Night",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            NeonButton(
                text = "SAVE",
                onClick = { onConfirm(presetName) },
                accent = NeonCyan,
            )
        },
        dismissButton = {
            NeonButton(
                text = "CANCEL",
                onClick = onDismiss,
                accent = NeonTextSecondary,
            )
        },
        containerColor = NeonBgAlt,
    )
}

@Composable
private fun FilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onShuffle: () -> Unit,
    onSmartPick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeonSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "filter schemes & fonts",
            modifier = Modifier.weight(1f),
        )
        NeonButton(
            text = "✨",
            onClick = onSmartPick,
            accent = NeonCyan,
            contentDescription = "Choose an intelligent theme suggestion",
        )
        NeonButton(
            text = "⟲",
            onClick = onShuffle,
            accent = NeonMagenta,
            contentDescription = "Shuffle scheme and font preview",
        )
    }
}

/** Default first, then favorites, then the rest — each group keeping catalog order. */
private fun <T> sortFavoritesFirst(entries: List<T>, isDefault: (T) -> Boolean, isFavorite: (T) -> Boolean): List<T> {
    val (defaults, rest) = entries.partition(isDefault)
    val (favorites, others) = rest.partition(isFavorite)
    return defaults + favorites + others
}

private fun matchesQuery(displayName: String, query: String): Boolean =
    query.isBlank() || displayName.contains(query, ignoreCase = true)

@Composable
private fun SchemeRow(
    ui: UiState,
    onSelect: (Selectable) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDeleteCustom: (String) -> Unit = {},
) {
    val filtered = ui.schemes.filter { matchesQuery(it.selectable.displayName, ui.query) }
    val sorted = sortFavoritesFirst(
        filtered,
        isDefault = { it.selectable.name == Selectable.DEFAULT_FILENAME },
        isFavorite = { it.selectable.name in ui.favoriteSchemes },
    )
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(sorted, key = { it.selectable.name }) { entry ->
            SchemeChip(
                entry = entry,
                selected = ui.selectedScheme == entry.selectable,
                applied = ui.appliedScheme == entry.selectable,
                favorite = entry.selectable.name in ui.favoriteSchemes,
                onClick = { onSelect(entry.selectable) },
                onToggleFavorite = { onToggleFavorite(entry.selectable.name) },
                onDeleteCustom = { onDeleteCustom(entry.selectable.name) },
            )
        }
    }
}

@Composable
private fun FontRow(
    ui: UiState,
    onSelect: (Selectable) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDeleteCustom: (String) -> Unit = {},
) {
    val filtered = ui.fonts.filter { matchesQuery(it.selectable.displayName, ui.query) }
    val sorted = sortFavoritesFirst(
        filtered,
        isDefault = { it.selectable.name == Selectable.DEFAULT_FILENAME },
        isFavorite = { it.selectable.name in ui.favoriteFonts },
    )
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(sorted, key = { it.selectable.name }) { entry ->
            FontChip(
                entry = entry,
                selected = ui.selectedFont == entry.selectable,
                applied = ui.appliedFont == entry.selectable,
                favorite = entry.selectable.name in ui.favoriteFonts,
                onClick = { onSelect(entry.selectable) },
                onToggleFavorite = { onToggleFavorite(entry.selectable.name) },
                onDeleteCustom = { onDeleteCustom(entry.selectable.name) },
            )
        }
    }
}

@Composable
private fun SchemeChip(
    entry: SchemeEntry,
    selected: Boolean,
    applied: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteCustom: () -> Unit = {},
) {
    NeonCard(
        modifier = Modifier.width(112.dp).height(92.dp),
        selected = selected,
        accent = Color(entry.palette.colors[14]),
        onClick = onClick,
        contentDescription = buildString {
            append(entry.selectable.displayName)
            when {
                selected -> append(", previewing")
                applied -> append(", applied")
            }
            if (favorite) append(", favorite")
        },
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(entry.palette.background)
                Spacer(modifier = Modifier.width(4.dp))
                ColorDot(entry.palette.foreground)
                Spacer(modifier = Modifier.width(4.dp))
                ColorDot(entry.palette.cursor)
                Spacer(modifier = Modifier.weight(1f))
                if (entry.isCustom) {
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRed,
                        modifier = Modifier
                            .clickable { onDeleteCustom() }
                            .padding(2.dp),
                    )
                } else {
                    FavoriteStar(
                        favorite = favorite,
                        onToggle = onToggleFavorite,
                        itemName = entry.selectable.displayName,
                    )
                }
            }
            Text(
                text = entry.selectable.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row {
                if (selected) {
                    Text(
                        text = "PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = NeonCyan,
                    )
                } else if (applied) {
                    Text(
                        text = "APPLIED",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = NeonGreen,
                    )
                }
            }
        }
    }
}

/** Loads (and memoizes) the real typeface for a bundled or custom font so chips preview it live. */
@Composable
private fun rememberChipFontFamily(assetName: String): FontFamily {
    val context = LocalContext.current
    return remember(assetName) {
        if (assetName == Selectable.DEFAULT_FILENAME) {
            FontFamily.Monospace
        } else {
            val customFile = File(context.filesDir, "custom_fonts/$assetName")
            if (customFile.isFile && customFile.length() > 0L) {
                runCatching { FontFamily(Typeface.createFromFile(customFile)) }
                    .getOrDefault(FontFamily.Monospace)
            } else {
                runCatching { FontFamily(Typeface.createFromAsset(context.assets, "fonts/$assetName")) }
                    .getOrDefault(FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun FontChip(
    entry: FontEntry,
    selected: Boolean,
    applied: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteCustom: () -> Unit = {},
) {
    val chipFont = rememberChipFontFamily(entry.selectable.name)
    NeonCard(
        modifier = Modifier.width(116.dp).height(92.dp),
        selected = selected,
        accent = if (entry.isCustom) NeonCyan else NeonMagenta,
        onClick = onClick,
        contentDescription = buildString {
            append(entry.selectable.displayName)
            when {
                selected -> append(", previewing")
                applied -> append(", applied")
            }
            if (favorite) append(", favorite")
        },
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlowText(
                    text = "Aa",
                    color = if (entry.isCustom) NeonCyan else NeonMagenta,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        letterSpacing = 2.sp,
                        fontFamily = chipFont,
                    ),
                    glowAlpha = 0.35f,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (entry.isCustom) {
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRed,
                        modifier = Modifier
                            .clickable { onDeleteCustom() }
                            .padding(2.dp),
                    )
                } else {
                    FavoriteStar(
                        favorite = favorite,
                        onToggle = onToggleFavorite,
                        itemName = entry.selectable.displayName,
                    )
                }
            }
            Text(
                text = entry.selectable.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row {
                if (entry.isCustom) {
                    Text(
                        text = "IMPORTED",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = NeonCyan,
                    )
                } else if (selected) {
                    Text(
                        text = "PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = NeonMagenta,
                    )
                } else if (applied) {
                    Text(
                        text = "APPLIED",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = NeonGreen,
                    )
                }
            }
        }
    }
}

/** Shared inner padding for full-width content cards (matches TerminalPreview's hero card). */
private val CardPadding = 16.dp

/** Ten curated, vivid, readable-on-dark-background terminal text colors. */
private val TEXT_COLOR_SWATCHES: List<Int> = listOf(
    0xFFFFFFFF.toInt(), // White
    0xFFE5E5E5.toInt(), // Off-white
    0xFF8BE9FD.toInt(), // Cyan
    0xFF50FA7B.toInt(), // Green
    0xFFFFB000.toInt(), // Amber
    0xFFFF79C6.toInt(), // Pink
    0xFF7AA2F7.toInt(), // Light blue
    0xFFC4A7E7.toInt(), // Lavender
    0xFF95E6CB.toInt(), // Mint
    0xFFFF9E64.toInt(), // Coral
)

@Composable
private fun TextColorSection(
    ui: UiState,
    onSelect: (Int) -> Unit,
) {
    val selected = ui.textColorOverride
    NeonCard(selected = selected != null, accent = selected?.let { Color(it) } ?: NeonAmber) {
        Column(modifier = Modifier.padding(CardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // All 10 shown at once (two rows of 5) rather than a scrollable
            // row — with a fixed, small set of swatches, a hidden scroll
            // just hides colors nobody knows to look for.
            TEXT_COLOR_SWATCHES.chunked(5).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { argb ->
                        ColorSwatch(argb = argb, selected = selected == argb, onClick = { onSelect(argb) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(argb: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(argb))
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .semantics {
                role = Role.Button
                contentDescription = "#%06X".format(argb and 0xFFFFFF)
                stateDescription = if (selected) "Selected" else "Not selected"
            },
    )
}

@Composable
private fun ColorDot(color: Int) {
    Box(
        modifier = Modifier
            .size(11.dp)
            .clip(CircleShape)
            .background(Color(color)),
    )
}

/**
 * Pinned to the bottom of the screen (not the scroll content) so the single
 * primary action is always one tap away, regardless of how far the
 * schemes/fonts rows have been scrolled. Deliberately borderless: a soft
 * vertical scrim (transparent → bg) keeps the pill legible over content
 * without drawing a hard panel edge — matches macOS's translucent bottom
 * toolbar treatment rather than a boxed Android app bar.
 */
@Composable
private fun FloatingApplyDock(
    ui: UiState,
    onApplyAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasPendingChanges = ui.selectedScheme != null || ui.selectedFont != null || ui.textColorOverride != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, NeonBg.copy(alpha = 0.85f), NeonBg),
                ),
            )
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!ui.termuxReady) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "TERMUX NOT FOUND",
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 2.sp,
                    color = NeonRed,
                )
                Text(
                    text = when (ui.termuxProblem) {
                        TermuxEnvironment.AccessProblem.NOT_INSTALLED ->
                            "Install Termux before applying styles."
                        TermuxEnvironment.AccessProblem.INCOMPATIBLE_SIGNATURE ->
                            "Termux was detected, but this app is signed by a different source. Install both apps from the same distribution."
                        TermuxEnvironment.AccessProblem.ACCESS_ERROR ->
                            "Termux was detected, but its private environment is inaccessible. Reinstall both apps from the same distribution."
                        TermuxEnvironment.AccessProblem.NONE ->
                            "Install Termux and reinstall this add-on with the matching signature to apply styles."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            return
        }

        ui.message?.let { note -> StatusMessage(note) }

        NeonButton(
            text = if (ui.screenStatus == ScreenStatus.APPLYING) "APPLYING…" else "APPLY ALL CHANGES",
            onClick = onApplyAll,
            contentDescription = if (ui.screenStatus == ScreenStatus.APPLYING) "Applying all changes" else "Apply all selected changes",
            modifier = Modifier.fillMaxWidth(0.82f),
            enabled = hasPendingChanges && ui.screenStatus != ScreenStatus.APPLYING,
            loading = ui.screenStatus == ScreenStatus.APPLYING,
            accent = NeonCyan,
        )
    }
}

@Composable
private fun StatusMessage(note: StatusNote) {
    val tone = when (note) {
        is StatusNote.Success -> NeonGreen
        is StatusNote.Error -> NeonRed
        is StatusNote.Info -> NeonCyan
    }
    NeonCard(selected = true, accent = tone) {
        Text(
            text = note.text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SchemeEditorDialog(
    initialPalette: AnsiPalette = AnsiPalette.DEFAULT,
    onDismiss: () -> Unit,
    onConfirm: (AnsiPalette) -> Unit,
) {
    var name by remember { mutableStateOf("My Custom Scheme") }
    var bgHex by remember { mutableStateOf("#101014") }
    var fgHex by remember { mutableStateOf("#CCCCCC") }
    var cursorHex by remember { mutableStateOf("#00E5FF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "CUSTOM SCHEME BUILDER",
                style = MaterialTheme.typography.titleMedium,
                color = NeonGreen,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Scheme Name:", style = MaterialTheme.typography.labelSmall, color = NeonTextSecondary)
                NeonSearchField(value = name, onValueChange = { name = it }, placeholder = "Scheme Name", modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Background:", style = MaterialTheme.typography.labelSmall, color = NeonTextSecondary)
                        NeonSearchField(value = bgHex, onValueChange = { bgHex = it }, placeholder = "#101014", modifier = Modifier.fillMaxWidth())
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Foreground:", style = MaterialTheme.typography.labelSmall, color = NeonTextSecondary)
                        NeonSearchField(value = fgHex, onValueChange = { fgHex = it }, placeholder = "#CCCCCC", modifier = Modifier.fillMaxWidth())
                    }
                }

                Text(text = "Cursor Color:", style = MaterialTheme.typography.labelSmall, color = NeonTextSecondary)
                NeonSearchField(value = cursorHex, onValueChange = { cursorHex = it }, placeholder = "#00E5FF", modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            NeonButton(
                text = "SAVE SCHEME",
                onClick = {
                    val bg = TermuxColorsParser.parseHexColor(bgHex)
                        ?: AnsiPalette.DEFAULT_BACKGROUND
                    val fg = TermuxColorsParser.parseHexColor(fgHex)
                        ?: AnsiPalette.DEFAULT_FOREGROUND
                    val cursor = TermuxColorsParser.parseHexColor(cursorHex)
                        ?: AnsiPalette.DEFAULT_CURSOR
                    val newPalette = AnsiPalette(
                        colors = initialPalette.colors,
                        foreground = fg,
                        background = bg,
                        cursor = cursor,
                        name = name.ifBlank { "Custom Scheme" },
                    )
                    onConfirm(newPalette)
                },
                accent = NeonGreen,
            )
        },
        dismissButton = {
            NeonButton(text = "CANCEL", onClick = onDismiss, accent = NeonTextSecondary)
        },
        containerColor = NeonBgAlt,
    )
}