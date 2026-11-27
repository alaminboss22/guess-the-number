package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.LocalAppStrings
import com.example.model.GameDifficulty
import com.example.model.SettingsUiState
import com.example.ui.components.AnimatedQuestionMark3D
import com.example.ui.components.ArcadeButton
import com.example.ui.components.CosmicGradientBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.ArcadeOrange
import com.example.ui.theme.BallBlue
import com.example.ui.theme.BallGreen
import com.example.ui.theme.BallPink
import com.example.ui.theme.BallPurple
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalGamePalette

@Composable
fun HomeScreen(
    settings: SettingsUiState,
    onStartGame: (GameDifficulty) -> Unit,
    onSelectDifficulty: (GameDifficulty) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onToggleSound: () -> Unit,
    onExitApp: () -> Unit
) {
    val strings = LocalAppStrings.current
    val palette = LocalGamePalette.current

    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showHowToPlayDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitConfirmDialog = true
    }

    CosmicGradientBackground(showFloatingBalls = true) {
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Header with App Logo, History & Quick Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_art),
                            contentDescription = strings.appTitleLine1 + " " + strings.appTitleLine2,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(8.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .border(2.dp, ArcadeGold, RoundedCornerShape(14.dp))
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = palette.glassFill,
                            border = BorderStroke(1.dp, palette.glassBorder)
                        ) {
                            Text(
                                text = "${strings.difficultyName(settings.selectedDifficulty)} · ${settings.selectedDifficulty.maxAttempts}",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.primaryText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassIconCircleButton(
                            icon = Icons.Rounded.History,
                            contentDescription = strings.gameHistory,
                            testTag = "history_button",
                            onClick = onOpenHistory
                        )
                        GlassIconCircleButton(
                            icon = if (settings.soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                            contentDescription = strings.soundEffects,
                            testTag = "home_sound_toggle",
                            onClick = onToggleSound
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Animated 3D Question Mark Hero
                AnimatedQuestionMark3D(size = 76.dp)

                // Bold Arcade Title
                Text(
                    text = strings.appTitleLine1,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    lineHeight = 40.sp,
                    color = ArcadeGold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = strings.appTitleLine2,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    color = palette.primaryText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.subtitleTagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Best Score & Total Games Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatGlassCard(
                        icon = Icons.Rounded.EmojiEvents,
                        iconTint = ArcadeGold,
                        label = strings.bestScore,
                        value = settings.bestScore.toString(),
                        testTag = "home_best_score",
                        modifier = Modifier.weight(1f)
                    )
                    StatGlassCard(
                        icon = Icons.Rounded.SportsEsports,
                        iconTint = BallBlue,
                        label = strings.totalGames,
                        value = settings.totalGames.toString(),
                        testTag = "home_total_games",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Start Game CTA
                ArcadeButton(
                    text = strings.startGame,
                    icon = Icons.Rounded.PlayArrow,
                    testTag = "start_game_button",
                    gradientColors = listOf(ArcadeGold, ArcadeOrange),
                    contentColor = Color(0xFF1F1200),
                    onClick = { onStartGame(settings.selectedDifficulty) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Difficulty Selector Button
                ArcadeButton(
                    text = "${strings.difficulty}: ${strings.difficultyName(settings.selectedDifficulty)}",
                    icon = Icons.Rounded.Tune,
                    testTag = "difficulty_button",
                    gradientColors = listOf(BallPink, BallPurple),
                    contentColor = Color.White,
                    onClick = { showDifficultyDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Row: How To Play & Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryActionCard(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        label = strings.howToPlay,
                        tint = BallBlue,
                        testTag = "how_to_play_button",
                        onClick = { showHowToPlayDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryActionCard(
                        icon = Icons.Rounded.Settings,
                        label = strings.settings,
                        tint = ArcadeGold,
                        testTag = "settings_button",
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showDifficultyDialog) {
        DifficultySelectionDialog(
            selectedDifficulty = settings.selectedDifficulty,
            onSelect = { diff ->
                onSelectDifficulty(diff)
                showDifficultyDialog = false
            },
            onDismiss = { showDifficultyDialog = false }
        )
    }

    if (showHowToPlayDialog) {
        AlertDialog(
            onDismissRequest = { showHowToPlayDialog = false },
            title = {
                Text(
                    text = strings.howToPlayTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    strings.howToPlaySteps.forEach { step ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHowToPlayDialog = false },
                    modifier = Modifier.testTag("how_to_play_got_it")
                ) {
                    Text(strings.gotIt)
                }
            }
        )
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text(strings.exitAppTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.exitAppBody) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onExitApp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.dangerAccent)
                ) {
                    Text(strings.leave, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text(strings.stay)
                }
            }
        )
    }
}

@Composable
fun GlassIconCircleButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    val palette = LocalGamePalette.current
    Surface(
        shape = CircleShape,
        color = palette.glassFill,
        border = BorderStroke(1.dp, palette.glassBorder),
        modifier = Modifier.size(48.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = palette.primaryText
            )
        }
    }
}

@Composable
private fun StatGlassCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    GlassCard(
        elevated = true,
        cornerRadius = 22.dp,
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = palette.primaryText
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = palette.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SecondaryActionCard(
    icon: ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    GlassCard(
        elevated = false,
        cornerRadius = 20.dp,
        modifier = modifier
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(20.dp))
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DifficultySelectionDialog(
    selectedDifficulty: GameDifficulty,
    onSelect: (GameDifficulty) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.selectDifficulty,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DifficultyOptionRow(
                    title = strings.easy,
                    subtitle = strings.attemptsLabel(GameDifficulty.EASY.maxAttempts),
                    accentColor = BallGreen,
                    isSelected = selectedDifficulty == GameDifficulty.EASY,
                    testTag = "difficulty_option_easy",
                    onClick = { onSelect(GameDifficulty.EASY) }
                )
                DifficultyOptionRow(
                    title = strings.medium,
                    subtitle = strings.attemptsLabel(GameDifficulty.MEDIUM.maxAttempts),
                    accentColor = ArcadeGold,
                    isSelected = selectedDifficulty == GameDifficulty.MEDIUM,
                    testTag = "difficulty_option_medium",
                    onClick = { onSelect(GameDifficulty.MEDIUM) }
                )
                DifficultyOptionRow(
                    title = strings.hard,
                    subtitle = strings.attemptsLabel(GameDifficulty.HARD.maxAttempts),
                    accentColor = BallPink,
                    isSelected = selectedDifficulty == GameDifficulty.HARD,
                    testTag = "difficulty_option_hard",
                    onClick = { onSelect(GameDifficulty.HARD) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
private fun DifficultyOptionRow(
    title: String,
    subtitle: String,
    accentColor: Color,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = title,
                    tint = accentColor
                )
            }
        }
    }
}
