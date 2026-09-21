package dev.neonbytecode.neon.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.neonbytecode.neon.themeengine.AnsiPalette
import dev.neonbytecode.neon.themeengine.DemoScreen
import dev.neonbytecode.neon.themeengine.ScreenLine

private val PanelShape = RoundedCornerShape(14.dp)

/**
 * The hero: a believable slice of a running terminal rendered with the
 * selected (or currently applied) color scheme + font on a real Termux-style
 * pipeline. Includes CRT scanlines and a corner-glove of the palette flavors.
 */
@Composable
fun TerminalPreview(
    palette: AnsiPalette,
    schemeName: String,
    fontName: String,
    font: FontFamily = FontFamily.Monospace,
    modifier: Modifier = Modifier,
    scanlineStrength: Float = 1f,
) {
    val lines = remember(palette, schemeName, fontName, font) {
        DemoScreen.build(palette, schemeName, fontName)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .neonGlow(glowFor(palette), intensity = 0.9f)
            .clip(PanelShape)
            .background(Color(palette.background)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        ) {
            PreviewTitleBar(palette, schemeName)
            Spacer(modifier = Modifier.height(2.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(3.dp),
            ) {
                lines.forEach { line ->
                    Text(
                        text = line.toAnnotated(palette),
                        fontFamily = font,
                        fontSize = 12.5.sp,
                        lineHeight = 15.sp,
                        color = Color(palette.foreground),
                        maxLines = 1,
                    )
                }
            }
        }
        ScanlineOverlay(strength = scanlineStrength, modifier = Modifier.matchParentSize().clip(PanelShape))
    }
}

@Composable
private fun PreviewTitleBar(palette: AnsiPalette, schemeName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(width = 36.dp, height = 10.dp)) {
            val dotColors = listOf(
                0xFFFF5F56.toInt(),
                0xFFFFBD2E.toInt(),
                0xFF27C93F.toInt(),
            )
            val radius = 4.dp.toPx()
            val y = size.height / 2f
            dotColors.forEachIndexed { index, color ->
                drawCircle(
                    color = Color(color),
                    radius = radius,
                    center = Offset(radius + index * radius * 2.2f, y),
                )
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = "termux-neon  ▸  $schemeName",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            color = Color(palette.colors[7]),
            maxLines = 1,
            modifier = Modifier.weight(1f),
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

private fun ScreenLine.toAnnotated(palette: AnsiPalette): AnnotatedString = buildAnnotatedString {
    segments.forEach { seg ->
        val intColor = seg.color?.let { palette.colors.getOrNull(it) } ?: palette.foreground
        withStyle(
            SpanStyle(
                color = Color(intColor),
                fontWeight = if (seg.bold) FontWeight.SemiBold else FontWeight.Normal,
            ),
        ) {
            append(seg.text)
        }
    }
}

private fun glowFor(palette: AnsiPalette): Color {
    val accent = palette.colors.getOrNull(14)
    return if (accent != null && accent != palette.background) Color(accent) else NeonCyan
}