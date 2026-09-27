package com.aieq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aieq.app.domain.model.EqBand
import com.aieq.app.ui.theme.DarkSurfaceVariant
import com.aieq.app.ui.theme.NeonCyan
import com.aieq.app.ui.theme.SliderTrack
import com.aieq.app.ui.theme.TextSecondary

@Composable
fun EqBandSliders(
    bands: List<EqBand>,
    onBandGainChanged: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "EQUALIZER BANDS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (band in bands) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Gain Value
                        Text(
                            text = if (band.gainDb > 0) "+${band.displayGain}" else band.displayGain,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (band.gainDb != 0f) NeonCyan else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        // Vertical Slider Representation using standard Slider
                        Box(
                            modifier = Modifier
                                .height(140.dp)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Slider rotated or compact vertical representation
                            // Compose Slider in Column
                            Slider(
                                value = band.gainDb,
                                onValueChange = { onBandGainChanged(band.index, it) },
                                valueRange = -12.0f..12.0f,
                                steps = 23, // 1.0 dB increments
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = SliderTrack
                                ),
                                modifier = Modifier
                                    .height(130.dp)
                                    .padding(horizontal = 2.dp)
                            )
                        }

                        // Frequency Label
                        Text(
                            text = band.displayFreq,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
