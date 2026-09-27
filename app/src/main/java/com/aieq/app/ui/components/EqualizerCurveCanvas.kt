package com.aieq.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aieq.app.domain.model.EqBand
import com.aieq.app.ui.theme.DarkSurfaceVariant
import com.aieq.app.ui.theme.GridLineColor
import com.aieq.app.ui.theme.NeonCyan
import com.aieq.app.ui.theme.PurpleAccent

@Composable
fun EqualizerCurveCanvas(
    bands: List<EqBand>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw horizontal reference grid lines: +12dB, +6dB, 0dB, -6dB, -12dB
            val dbRange = 24.0f // -12 to +12
            fun dbToY(db: Float): Float {
                val normalized = 1.0f - ((db + 12.0f) / dbRange)
                return normalized.coerceIn(0f, 1f) * h
            }

            val zeroY = dbToY(0f)
            val plus6Y = dbToY(6f)
            val minus6Y = dbToY(-6f)

            drawLine(GridLineColor, Offset(0f, plus6Y), Offset(w, plus6Y), strokeWidth = 1f)
            drawLine(Color(0x5500E5FF), Offset(0f, zeroY), Offset(w, zeroY), strokeWidth = 1.5f)
            drawLine(GridLineColor, Offset(0f, minus6Y), Offset(w, minus6Y), strokeWidth = 1f)

            if (bands.isEmpty()) return@Canvas

            // 2. Compute Node Points
            val nodeOffsets = bands.mapIndexed { idx, band ->
                val x = (idx + 0.5f) / bands.size * w
                val y = dbToY(band.gainDb)
                Offset(x, y)
            }

            // 3. Smooth Cubic Spline Path
            val curvePath = Path().apply {
                moveTo(0f, nodeOffsets.first().y)
                lineTo(nodeOffsets.first().x, nodeOffsets.first().y)

                for (i in 0 until nodeOffsets.size - 1) {
                    val p0 = nodeOffsets[i]
                    val p1 = nodeOffsets[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y

                    cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }

                lineTo(w, nodeOffsets.last().y)
            }

            // 4. Gradient Fill Under Curve
            val fillPath = Path().apply {
                addPath(curvePath)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.35f), PurpleAccent.copy(alpha = 0.05f), Color.Transparent),
                    startY = 0f,
                    endY = h
                )
            )

            // 5. Draw Glowing Curve Stroke
            drawPath(
                path = curvePath,
                brush = Brush.horizontalGradient(listOf(NeonCyan, PurpleAccent)),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // 6. Draw Control Nodes
            for (node in nodeOffsets) {
                // Outer glow
                drawCircle(NeonCyan.copy(alpha = 0.3f), radius = 10f, center = node)
                // Inner ring
                drawCircle(Color.White, radius = 5f, center = node)
                // Center dot
                drawCircle(NeonCyan, radius = 3f, center = node)
            }
        }
    }
}
