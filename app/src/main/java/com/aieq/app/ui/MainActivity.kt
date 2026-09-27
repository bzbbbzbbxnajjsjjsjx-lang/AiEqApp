package com.aieq.app.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aieq.app.ui.components.AiControlsCard
import com.aieq.app.ui.components.ApiKeyDialog
import com.aieq.app.ui.components.CapabilityBanner
import com.aieq.app.ui.components.EqBandSliders
import com.aieq.app.ui.components.EqualizerCurveCanvas
import com.aieq.app.ui.components.HeadphoneSelectorSheet
import com.aieq.app.ui.components.NowPlayingCard
import com.aieq.app.ui.theme.AiEqTheme
import com.aieq.app.ui.theme.DarkBackground
import com.aieq.app.ui.theme.DarkSurfaceVariant
import com.aieq.app.ui.theme.NeonCyan
import com.aieq.app.ui.theme.SoftTeal

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AiEqTheme {
                val uiState by viewModel.uiState.collectAsState()

                Scaffold(
                    topBar = {
                        AiEqTopAppBar(
                            selectedHeadphoneName = uiState.selectedHeadphone.displayName,
                            hasApiKey = uiState.hasApiKey,
                            onHeadphoneClick = { viewModel.showHeadphoneSelector(true) },
                            onSettingsClick = { viewModel.showApiKeyDialog(true) }
                        )
                    },
                    containerColor = DarkBackground,
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // 1. Hardware & Session Capability Banner
                        CapabilityBanner(
                            outputInfo = uiState.outputInfo,
                            hasNotificationAccess = uiState.hasNotificationAccess,
                            onRequestNotificationAccess = {
                                openNotificationAccessSettings()
                            }
                        )

                        // 2. Now Playing Card
                        NowPlayingCard(
                            track = uiState.currentTrack,
                            onSelectNextMockTrack = { viewModel.nextMockTrack() }
                        )

                        // 3. Real-time Equalizer Curve Canvas Visualizer
                        EqualizerCurveCanvas(
                            bands = uiState.finalEqProfile.bands
                        )

                        // 4. AI Controls & Target Sound Profile
                        AiControlsCard(
                            isAiEnabled = uiState.isAiEnabled,
                            onAiToggle = { viewModel.onAiToggle(it) },
                            aiIntensity = uiState.aiIntensity,
                            onIntensityChange = { viewModel.onIntensityChange(it) },
                            selectedPreference = uiState.soundPreference,
                            onPreferenceChange = { viewModel.onSoundPreferenceChange(it) },
                            aiReasoning = uiState.finalEqProfile.aiReasoning,
                            onResetToAutoEq = { viewModel.onResetToAutoEq() },
                            onResetToFlat = { viewModel.onResetToFlat() }
                        )

                        // 5. Interactive EQ Band Sliders
                        EqBandSliders(
                            bands = uiState.finalEqProfile.bands,
                            onBandGainChanged = { bandIdx, gain ->
                                viewModel.onManualBandGainChanged(bandIdx, gain)
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Headphone Selection Bottom Sheet
                if (uiState.showHeadphoneSelector) {
                    HeadphoneSelectorSheet(
                        currentHeadphone = uiState.selectedHeadphone,
                        onHeadphoneSelected = { viewModel.onHeadphoneSelected(it) },
                        onDismiss = { viewModel.showHeadphoneSelector(false) }
                    )
                }

                // API Key Configuration Dialog
                if (uiState.showApiKeyDialog) {
                    ApiKeyDialog(
                        currentApiKey = viewModel.getApiKey(),
                        onSaveKey = { viewModel.saveApiKey(it) },
                        onClearKey = { viewModel.clearApiKey() },
                        onDismiss = { viewModel.showApiKeyDialog(false) }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh session detection on resume safely
        try {
            viewModel.recomputeAndApplyEq()
        } catch (t: Throwable) {
            // Ignore resume errors
        }
    }

    private fun openNotificationAccessSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to app details
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiEqTopAppBar(
    selectedHeadphoneName: String,
    hasApiKey: Boolean,
    onHeadphoneClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "AI EQ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Headphone Selector Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceVariant)
                    .clickable(onClick = onHeadphoneClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🎧",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = selectedHeadphoneName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyan,
                        maxLines = 1
                    )
                }
            }

            // API Key Settings Action
            IconButton(onClick = onSettingsClick) {
                Text(
                    text = if (hasApiKey) "🔑" else "⚙️",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBackground
        )
    )
}
