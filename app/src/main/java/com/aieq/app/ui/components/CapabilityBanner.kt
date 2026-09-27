package com.aieq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aieq.app.domain.model.AudioOutputInfo
import com.aieq.app.domain.model.EqCapabilityStatus
import com.aieq.app.ui.theme.StatusAmber
import com.aieq.app.ui.theme.StatusGreen
import com.aieq.app.ui.theme.StatusRed
import com.aieq.app.ui.theme.TextSecondary

@Composable
fun CapabilityBanner(
    outputInfo: AudioOutputInfo,
    hasNotificationAccess: Boolean,
    onRequestNotificationAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, badgeText) = when (outputInfo.capabilityStatus) {
        EqCapabilityStatus.SUPPORTED -> StatusGreen to "DSP ACTIVE"
        EqCapabilityStatus.RESTRICTED_SESSION_ZERO -> StatusAmber to "LIMITED"
        EqCapabilityStatus.NO_ACTIVE_SESSION -> Color(0xFF64B5F6) to "STANDBY"
        EqCapabilityStatus.UNSUPPORTED -> StatusRed to "UNSUPPORTED"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = outputInfo.capabilityStatus.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }

            Text(
                text = "${outputInfo.deviceName} (${outputInfo.deviceType})" +
                        if (outputInfo.activeSessionId > 0) " • Session ID #${outputInfo.activeSessionId}" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = outputInfo.capabilityStatus.message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (!hasNotificationAccess) {
                OutlinedButton(
                    onClick = onRequestNotificationAccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Notification Access for Live Track Detection")
                }
            }
        }
    }
}
