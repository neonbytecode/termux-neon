package com.termux.styling

import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import dev.neonbytecode.neon.designsystem.ScanlineOverlay
import dev.neonbytecode.neon.designsystem.SectionLabel
import dev.neonbytecode.neon.designsystem.StatusPill
import dev.neonbytecode.neon.designsystem.TerminalPreview
import dev.neonbytecode.neon.termux.Selectable
import kotlinx.coroutines.delay

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
        onApplyScheme = viewModel::applyScheme,
        onApplyFont = viewModel::applyFont,
        onQueryChange = viewModel::setQuery,
        onToggleFavoriteScheme = viewModel::toggleFavoriteScheme,
        onToggleFavoriteFont = viewModel::toggleFavoriteFont,
        onShuffle = viewModel::shuffle,
        onBack = onBack,
        onTextColorSelect = viewModel::selectTextColor,
        onApplyTextColor = viewModel::applyTextColor,
        onResetTextColor = viewModel::resetTextColor,
    )
}

@Composable
fun NeonStylingScreen(
    ui: UiState,
    onSelectScheme: (Selectable) -> Unit,
    onSelectFont: (Selectable) -> Unit,
    onApplyScheme: () -> Unit,
    onApplyFont: () -> Unit,
    onQueryChange: (String) -> Unit = {},
    onToggleFavoriteScheme: (String) -> Unit = {},
    onToggleFavoriteFont: (String) -> Unit = {},
    onShuffle: () -> Unit = {},
    onBack: () -> Unit = {},
    onTextColorSelect: (Int) -> Unit = {},
    onApplyTextColor: () -> Unit = {},
    onResetTextColor: () -> Unit = {},
) {
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
            )
            Spacer(modifier = Modifier.height(18.dp))
            FilterBar(query = ui.query, onQueryChange = onQueryChange, onShuffle = onShuffle)
            Spacer(modifier = Modifier.height(22.dp))
            SectionLabel("COLOR SCHEMES")
            SchemeRow(ui, onSelectScheme, onToggleFavoriteScheme)
            Spacer(modifier = Modifier.height(22.dp))
            SectionLabel("FONTS")
            FontRow(ui, onSelectFont, onToggleFavoriteFont)
            Spacer(modifier = Modifier.height(22.dp))
            SectionLabel("TEXT COLOR")
            TextColorSection(ui, onTextColorSelect)
            // Trailing clearance so the floating apply dock never covers content.
            Spacer(modifier = Modifier.height(230.dp))
        }

        FloatingApplyDock(
            ui = ui,
            onApplyScheme = onApplyScheme,
            onApplyFont = onApplyFont,
            onApplyTextColor = onApplyTextColor,
            onResetTextColor = onResetTextColor,
            modifier = Modifier.align(Alignment.BottomCenter),
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
private fun FilterBar(query: String, onQueryChange: (String) -> Unit, onShuffle: () -> Unit) {
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
            text = "⟲",
            onClick = onShuffle,
            accent = NeonMagenta,
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
private fun SchemeRow(ui: UiState, onSelect: (Selectable) -> Unit, onToggleFavorite: (String) -> Unit) {
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
            )
        }
    }
}

@Composable
private fun FontRow(ui: UiState, onSelect: (Selectable) -> Unit, onToggleFavorite: (String) -> Unit) {
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
                FavoriteStar(favorite = favorite, onToggle = onToggleFavorite)
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

/** Loads (and memoizes) the real typeface for a bundled font so chips preview it live. */
@Composable
private fun rememberChipFontFamily(assetName: String): FontFamily {
    val assets = LocalContext.current.assets
    return remember(assetName) {
        if (assetName == Selectable.DEFAULT_FILENAME) {
            FontFamily.Monospace
        } else {
            runCatching { FontFamily(Typeface.createFromAsset(assets, "fonts/$assetName")) }
                .getOrDefault(FontFamily.Monospace)
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
) {
    val chipFont = rememberChipFontFamily(entry.selectable.name)
    NeonCard(
        modifier = Modifier.width(116.dp).height(92.dp),
        selected = selected,
        accent = NeonMagenta,
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
                    color = NeonMagenta,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        letterSpacing = 2.sp,
                        fontFamily = chipFont,
                    ),
                    glowAlpha = 0.35f,
                )
                Spacer(modifier = Modifier.weight(1f))
                FavoriteStar(favorite = favorite, onToggle = onToggleFavorite)
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
 * Pinned to the bottom of the screen (not the scroll content) so the primary
 * actions are always one tap away, regardless of how far the schemes/fonts
 * rows have been scrolled — no more hunting for an Apply button at the
 * bottom of a long page.
 */
@Composable
private fun FloatingApplyDock(
    ui: UiState,
    onApplyScheme: () -> Unit,
    onApplyFont: () -> Unit,
    onApplyTextColor: () -> Unit,
    onResetTextColor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NeonSurfaceHigh)
            .border(
                BorderStroke(1.dp, (if (ui.termuxReady) NeonCyan else NeonRed).copy(alpha = 0.35f)),
                shape = RectangleShape,
            )
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!ui.termuxReady) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TERMUX NOT FOUND",
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 2.sp,
                    color = NeonRed,
                )
                Text(
                    text = "Install Termux and reinstall this add-on with the matching signature to apply styles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return
        }

        if (ui.message != null) {
            StatusMessage(ui.message!!)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NeonButton(
                text = "APPLY SCHEME",
                onClick = onApplyScheme,
                modifier = Modifier.weight(1f),
                enabled = ui.selectedScheme != null && !ui.busy,
                loading = ui.busy,
                accent = NeonCyan,
            )
            NeonButton(
                text = "APPLY FONT",
                onClick = onApplyFont,
                modifier = Modifier.weight(1f),
                enabled = ui.selectedFont != null && !ui.busy,
                loading = ui.busy,
                accent = NeonMagenta,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NeonButton(
                text = "APPLY TEXT COLOR",
                onClick = onApplyTextColor,
                modifier = Modifier.weight(1f),
                enabled = ui.textColorOverride != null && !ui.busy,
                accent = NeonGreen,
            )
            NeonButton(
                text = "RESET",
                onClick = onResetTextColor,
                enabled = ui.textColorOverride != null && !ui.busy,
                accent = NeonRed,
                filled = false,
            )
        }
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