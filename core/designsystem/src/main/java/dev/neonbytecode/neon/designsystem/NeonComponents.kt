package dev.neonbytecode.neon.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ------------------------------------------------------------------ */
/*  Glow primitives — the "glow budget": accent on interactive and    */
/*  hero elements only, never on body text.                            */
/* ------------------------------------------------------------------ */

/**
 * Draws a layered neon halo around a rounded element. Cheap (vector strokes),
 * no RenderEffect dependency, works from API 28.
 */
fun Modifier.neonGlow(color: Color, intensity: Float = 1f): Modifier = drawBehind {
    val stroke = 1.5.dp.toPx()
    val radius = CornerRadius(14.dp.toPx())
    for (i in 3 downTo 1) {
        val inflate = stroke * i
        drawRoundRect(
            color = color.copy(alpha = intensity * 0.055f * i),
            topLeft = Offset(-inflate, -inflate),
            size = Size(size.width + inflate * 2, size.height + inflate * 2),
            cornerRadius = CornerRadius(radius.x + inflate, radius.y + inflate),
            style = Stroke(width = stroke),
        )
    }
    drawRoundRect(
        color = color.copy(alpha = intensity * 0.35f),
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = radius,
        style = Stroke(width = stroke),
    )
}

/** Subtle CRT horizontal scanlines rendered on top of content. */
@Composable
fun ScanlineOverlay(strength: Float, modifier: Modifier = Modifier) {
    if (strength <= 0f) return
    Canvas(modifier) {
        val step = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawRect(
                color = Color.White.copy(alpha = 0.018f * strength),
                topLeft = Offset(0f, y),
                size = Size(size.width, 1.dp.toPx()),
            )
            y += step
        }
        // soft vignette
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.35f * strength)),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = size.minDimension / 2f * 1.15f,
            ),
        )
    }
}

/** Faint desktop-grid backdrop, typical of cyberpunk HUD surfaces. */
@Composable
fun GridBackdrop(modifier: Modifier = Modifier, color: Color = NeonCyan) {
    Canvas(modifier) {
        val step = 32.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(color.copy(alpha = 0.035f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(color.copy(alpha = 0.035f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    }
}

/** Text with a synthesized neon bloom behind it. */
@Composable
fun GlowText(
    text: String,
    color: Color = NeonCyan,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    glowAlpha: Float = 0.55f,
) {
    androidx.compose.material3.Text(
        text = text,
        color = color,
        style = style.copy(shadow = Shadow(color = color.copy(alpha = glowAlpha), blurRadius = 28f, offset = Offset.Zero)),
        modifier = modifier,
    )
}

/* ------------------------------------------------------------------ */
/*  Neon components                                                     */
/* ------------------------------------------------------------------ */

@Composable
fun NeonCard(
    selected: Boolean = false,
    accent: Color = NeonCyan,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val border = BorderStroke(
        1.dp,
        when {
            selected -> accent.copy(alpha = 0.95f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        },
    )
    val shape = MaterialTheme.shapes.medium
    val base = modifier.then(if (selected) Modifier.neonGlow(accent, intensity = 1f) else Modifier)

    Box(
        modifier = base
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(border, shape)
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(onClick = onClick)
                        .semantics {
                            role = Role.Button
                            if (contentDescription != null) {
                                this.contentDescription = contentDescription
                            }
                        }
                } else {
                    if (contentDescription != null) {
                        Modifier.semantics { this.contentDescription = contentDescription }
                    } else {
                        Modifier
                    }
                },
            ),
        content = { content() },
    )
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    accent: Color = NeonCyan,
    filled: Boolean = true,
) {
    val shape = CircleShape
    val container = if (filled) accent.copy(alpha = 0.16f) else Color.Transparent
    val contentColor = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val border = BorderStroke(
        1.dp,
        if (enabled) accent.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    )

    androidx.compose.material3.Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .semantics {
                if (contentDescription != null) this.contentDescription = contentDescription
                if (loading) stateDescription = "In progress"
            }
            .then(Modifier.inlineGlow(if (enabled) accent else Color.Transparent, intensity = if (enabled) 1f else 0f))
            .clip(shape),
        shape = shape,
        color = container,
        contentColor = contentColor,
        border = border,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 28.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = contentColor,
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Glow sized for pill buttons (uses the same halo routine, ellipse-friendly). */
private fun Modifier.inlineGlow(color: Color, intensity: Float): Modifier = if (intensity > 0f) drawBehind {
    val stroke = 1.5.dp.toPx()
    for (i in 2 downTo 1) {
        val inflate = stroke * i
        drawCircle(
            color = color.copy(alpha = 0.06f * i * intensity),
            radius = size.minDimension / 2f + inflate,
            center = Offset(size.width / 2f, size.height / 2f),
            style = Stroke(width = stroke),
        )
    }
} else this

@Composable
fun StatusPill(text: String, tone: Color = NeonGreen, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = tone.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, tone.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(tone))
            Text(
                text = text,
                color = tone,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Retro terminal-prompt styled search/filter input. */
@Composable
fun NeonSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "filter",
    accent: Color = NeonCyan,
) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.4f)), shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics { contentDescription = "Filter schemes and fonts" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = ">",
            color = accent,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Text(
                text = "×",
                color = accent,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onValueChange("") }
                    .padding(start = 6.dp),
            )
        }
    }
}

/** Small star toggle used on scheme/font chips to pin favorites. */
@Composable
fun FavoriteStar(
    favorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = NeonAmber,
    itemName: String? = null,
) {
    Text(
        text = if (favorite) "★" else "☆",
        color = if (favorite) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onToggle() }
            .semantics {
                role = Role.Button
                contentDescription = buildString {
                    append(if (favorite) "Unfavorite" else "Favorite")
                    itemName?.let { append(" $it") }
                }
            },
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, accent: Color = NeonTextSecondary) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 2.dp)
                .background(accent.copy(alpha = 0.7f)),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            letterSpacing = 3.sp,
        )
    }
}