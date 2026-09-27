package com.aieq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.ui.theme.DarkSurfaceVariant
import com.aieq.app.ui.theme.NeonCyan
import com.aieq.app.ui.theme.PurpleAccent
import com.aieq.app.ui.theme.SliderTrack
import com.aieq.app.ui.theme.SoftTeal
import com.aieq.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiControlsCard(
    isAiEnabled: Boolean,
    onAiToggle: (Boolean) -> Unit,
    aiIntensity: Float,
    onIntensityChange: (Float) -> Unit,
    selectedPreference: SoundPreference,
    onPreferenceChange: (SoundPreference) -> Unit,
    aiReasoning: String,
    onResetToAutoEq: () -> Unit,
    onResetToFlat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with AI Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI HARMONIC EQUALIZER",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isAiEnabled) "Real-time acoustic tuning active" else "AI disabled • Static AutoEq baseline",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAiEnabled) NeonCyan else TextSecondary
                    )
                }

                Switch(
                    checked = isAiEnabled,
                    onCheckedChange = onAiToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = SliderTrack
                    )
                )
            }

            // AI Reasoning Bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x22B388FF))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "AI REASONING",
                        style = MaterialTheme.typography.labelSmall,
                        color = PurpleAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = aiReasoning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // AI Intensity Slider
            if (isAiEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "AI Intensity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(aiIntensity * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = aiIntensity,
                        onValueChange = onIntensityChange,
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = SliderTrack
                        )
                    )
                }
            }

            // Sound Preference Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TARGET SOUND PROFILE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (pref in SoundPreference.values()) {
                        val isSelected = pref == selectedPreference
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) NeonCyan.copy(alpha = 0.25f)
                                    else Color(0x33475569)
                                )
                                .clickable { onPreferenceChange(pref) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = pref.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Action Buttons: Reset to AutoEq, Reset to Flat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onResetToAutoEq,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTeal)
                ) {
                    Text("AutoEq Baseline", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onResetToFlat,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Flat (0 dB)", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
