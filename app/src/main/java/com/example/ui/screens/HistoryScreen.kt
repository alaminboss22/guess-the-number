package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.GameRecordEntity
import com.example.localization.LocalAppStrings
import com.example.model.GameDifficulty
import com.example.model.SettingsUiState
import com.example.ui.components.CosmicGradientBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.BallBlue
import com.example.ui.theme.BallGreen
import com.example.ui.theme.LocalGamePalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    records: List<GameRecordEntity>,
    settings: SettingsUiState,
    onClearHistory: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val palette = LocalGamePalette.current
    var showClearDialog by remember { mutableStateOf(false) }

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
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlassIconCircleButton(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.home,
                            testTag = "history_back_button",
                            onClick = onNavigateBack
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = strings.gameHistory,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.primaryText
                        )
                    }

                    if (records.isNotEmpty()) {
                        GlassIconCircleButton(
                            icon = Icons.Rounded.DeleteSweep,
                            contentDescription = strings.resetStatistics,
                            testTag = "clear_history_button",
                            onClick = { showClearDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Strip
                GlassCard(elevated = true, cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SummaryStatItem(strings.totalGames, settings.totalGames.toString(), BallBlue)
                        SummaryStatItem(strings.wins, settings.winsCount.toString(), BallGreen)
                        SummaryStatItem(strings.bestScore, settings.bestScore.toString(), ArcadeGold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (records.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = palette.secondaryText.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.historyEmpty,
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.secondaryText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("history_empty_text")
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(records, key = { it.id }) { item ->
                            HistoryRecordCard(record = item)
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.resetConfirmTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.resetConfirmBody) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.dangerAccent)
                ) {
                    Text(strings.confirm, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun SummaryStatItem(label: String, value: String, color: Color) {
    val palette = LocalGamePalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.secondaryText
        )
    }
}

@Composable
private fun HistoryRecordCard(record: GameRecordEntity) {
    val strings = LocalAppStrings.current
    val palette = LocalGamePalette.current
    val badgeColor = if (record.isWin) BallGreen else palette.dangerAccent
    val difficulty = runCatching { GameDifficulty.valueOf(record.difficultyName) }.getOrDefault(GameDifficulty.MEDIUM)
    val formattedDate = remember(record.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(record.timestamp))
    }

    GlassCard(elevated = false, cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, badgeColor)
                    ) {
                        Text(
                            text = if (record.isWin) strings.wonBadge else strings.lostBadge,
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = strings.difficultyName(difficulty),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${strings.secretNumber}: ${record.secretNumber} · ${record.attemptsUsed}/${record.maxAttempts} · $formattedDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryText
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = ArcadeGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = record.score.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ArcadeGold
                )
            }
        }
    }
}
