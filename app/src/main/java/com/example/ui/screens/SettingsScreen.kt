package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.localization.LocalAppStrings
import com.example.model.AppLanguage
import com.example.model.SettingsUiState
import com.example.ui.components.CosmicGradientBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.BallBlue
import com.example.ui.theme.BallGreen
import com.example.ui.theme.BallPink
import com.example.ui.theme.BallPurple
import com.example.ui.theme.LocalGamePalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: SettingsUiState,
    onSoundChanged: (Boolean) -> Unit,
    onVibrationChanged: (Boolean) -> Unit,
    onMusicChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onResetStatistics: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val palette = LocalGamePalette.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    BackHandler(onBack = onNavigateBack)

    CosmicGradientBackground(showFloatingBalls = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconCircleButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = strings.home,
                        testTag = "settings_back_button",
                        onClick = onNavigateBack
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = strings.settings,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Audio, Haptics & Theme Switches
                GlassCard(elevated = true, cornerRadius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsSwitchRow(
                            icon = Icons.AutoMirrored.Rounded.VolumeUp,
                            iconTint = ArcadeGold,
                            title = strings.soundEffects,
                            subtitle = strings.soundDesc,
                            checked = settings.soundEnabled,
                            testTag = "switch_sound",
                            onCheckedChange = onSoundChanged
                        )
                        SettingsSwitchRow(
                            icon = Icons.Rounded.Vibration,
                            iconTint = BallBlue,
                            title = strings.vibration,
                            subtitle = strings.vibrationDesc,
                            checked = settings.vibrationEnabled,
                            testTag = "switch_vibration",
                            onCheckedChange = onVibrationChanged
                        )
                        SettingsSwitchRow(
                            icon = Icons.Rounded.MusicNote,
                            iconTint = BallPink,
                            title = strings.backgroundMusic,
                            subtitle = strings.musicDesc,
                            checked = settings.musicEnabled,
                            testTag = "switch_music",
                            onCheckedChange = onMusicChanged
                        )
                        SettingsSwitchRow(
                            icon = Icons.Rounded.DarkMode,
                            iconTint = BallPurple,
                            title = strings.darkMode,
                            subtitle = strings.darkModeDesc,
                            checked = settings.darkMode,
                            testTag = "switch_dark_mode",
                            onCheckedChange = onDarkModeChanged
                        )
                    }
                }

                // Language Selection
                GlassCard(elevated = true, cornerRadius = 24.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = null,
                                tint = BallGreen
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.languageLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.primaryText
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                val selected = settings.language == lang
                                FilterChip(
                                    selected = selected,
                                    onClick = { onLanguageSelected(lang) },
                                    label = {
                                        Text(
                                            text = lang.displayName,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ArcadeGold,
                                        selectedLabelColor = Color(0xFF1F1200),
                                        labelColor = palette.primaryText
                                    ),
                                    modifier = Modifier.testTag("lang_chip_${lang.code}")
                                )
                            }
                        }
                    }
                }

                // Reset Statistics Action
                GlassCard(
                    elevated = false,
                    cornerRadius = 22.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .testTag("reset_stats_button")
                        .clickable { showResetDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteForever,
                            contentDescription = null,
                            tint = palette.dangerAccent,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.resetStatistics,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.primaryText
                            )
                            Text(
                                text = strings.resetStatsDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.secondaryText
                            )
                        }
                    }
                }

                // About App Action
                GlassCard(
                    elevated = false,
                    cornerRadius = 22.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .testTag("about_app_button")
                        .clickable { showAboutDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = BallBlue,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.aboutApp,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.primaryText
                            )
                            Text(
                                text = "v1.0 · com.vex.guessthenumber",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.secondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(strings.resetConfirmTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.resetConfirmBody) },
            confirmButton = {
                Button(
                    onClick = {
                        onResetStatistics()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.dangerAccent),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text(strings.confirm, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(strings.aboutApp, fontWeight = FontWeight.Bold) },
            text = { Text(strings.aboutAppBody) },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text(strings.gotIt)
                }
            }
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    val palette = LocalGamePalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.primaryText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.secondaryText
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF1F1200),
                checkedTrackColor = ArcadeGold
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
