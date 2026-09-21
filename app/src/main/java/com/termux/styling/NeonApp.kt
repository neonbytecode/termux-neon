package com.termux.styling

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
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
import dev.neonbytecode.neon.designsystem.NeonTextSecondary
import dev.neonbytecode.neon.designsystem.ScanlineOverlay
import dev.neonbytecode.neon.designsystem.SectionLabel
import dev.neonbytecode.neon.designsystem.StatusPill
import dev.neonbytecode.neon.designsystem.TerminalPreview
import dev.neonbytecode.neon.termux.Selectable
import kotlinx.coroutines.delay

@Composable
fun NeonScreen(viewModel: MainViewModel) {
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

    NeonStylingScreen(
        ui = ui,
        onSelectScheme = viewModel::selectScheme,
        onSelectFont = viewModel::selectFont,
        onApplyScheme = viewModel::applyScheme,
        onApplyFont = viewModel::applyFont,
    )
}

@Composable
fun NeonStylingScreen(
    ui: UiState,
    onSelectScheme: (Selectable) -> Unit,
    onSelectFont: (Selectable) -> Unit,
    onApplyScheme: () -> Unit,
    onApplyFont: () -> Unit,
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
            NeonHeader(termuxReady = ui.termuxReady)
            Spacer(modifier = Modifier.height(18.dp))
            TerminalPreview(
                palette = ui.previewPalette,
                schemeName = ui.previewSchemeName,
                fontName = ui.previewFontName,
                font = ui.previewFont,
            )
            Spacer(modifier = Modifier.height(22.dp))
            SectionLabel("COLOR SCHEMES")
            SchemeRow(ui, onSelectScheme)
            Spacer(modifier = Modifier.height(18.dp))
            SectionLabel("FONTS")
            FontRow(ui, onSelectFont)
            Spacer(modifier = Modifier.height(22.dp))
            ApplyDock(ui, onApplyScheme, onApplyFont)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NeonHeader(termuxReady: Boolean) {
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

@Composable
private fun SchemeRow(ui: UiState, onSelect: (Selectable) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(ui.schemes, key = { it.selectable.name }) { entry ->
            SchemeChip(
                entry = entry,
                selected = ui.selectedScheme == entry.selectable,
                applied = ui.appliedScheme == entry.selectable,
                onClick = { onSelect(entry.selectable) },
            )
        }
    }
}

@Composable
private fun FontRow(ui: UiState, onSelect: (Selectable) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(ui.fonts, key = { it.selectable.name }) { entry ->
            FontChip(
                entry = entry,
                selected = ui.selectedFont == entry.selectable,
                applied = ui.appliedFont == entry.selectable,
                onClick = { onSelect(entry.selectable) },
            )
        }
    }
}

@Composable
private fun SchemeChip(
    entry: SchemeEntry,
    selected: Boolean,
    applied: Boolean,
    onClick: () -> Unit,
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
        },
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(entry.palette.background)
                Spacer(modifier = Modifier.width(4.dp))
                ColorDot(entry.palette.foreground)
                Spacer(modifier = Modifier.width(4.dp))
                ColorDot(entry.palette.cursor)
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

@Composable
private fun FontChip(
    entry: FontEntry,
    selected: Boolean,
    applied: Boolean,
    onClick: () -> Unit,
) {
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
        },
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            GlowText(
                text = "Aa",
                color = NeonMagenta,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, letterSpacing = 2.sp),
                glowAlpha = 0.35f,
            )
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

@Composable
private fun ColorDot(color: Int) {
    Box(
        modifier = Modifier
            .size(11.dp)
            .clip(CircleShape)
            .background(Color(color)),
    )
}

@Composable
private fun ApplyDock(
    ui: UiState,
    onApplyScheme: () -> Unit,
    onApplyFont: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!ui.termuxReady) {
            NeonCard(selected = false, accent = NeonRed) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            }
            return
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

        if (ui.message != null) {
            StatusMessage(ui.message!!)
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