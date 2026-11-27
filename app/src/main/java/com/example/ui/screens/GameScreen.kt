package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalAppStrings
import com.example.model.GameUiState
import com.example.model.GuessFeedback
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
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    gameState: GameUiState,
    activeHintText: String?,
    duplicateGuessFlag: Boolean,
    soundEnabled: Boolean,
    onDigitClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onSubmitGuess: () -> Unit,
    onRequestHint: () -> Unit,
    onRestartGame: () -> Unit,
    onToggleSound: () -> Unit,
    onNavigateBackHome: () -> Unit,
    onGameFinished: () -> Unit
) {
    val strings = LocalAppStrings.current
    val palette = LocalGamePalette.current
    var showLeaveDialog by remember { mutableStateOf(false) }

    // Trigger navigation to Result screen cleanly after a short celebration/reveal beat
    LaunchedEffect(gameState.isGameOver, gameState.lastResult) {
        if (gameState.isGameOver && gameState.lastResult != null) {
            delay(380L)
            onGameFinished()
        }
    }

    val handleBack: () -> Unit = {
        if (gameState.attemptsUsed > 0 && !gameState.isGameOver) {
            showLeaveDialog = true
        } else {
            onNavigateBackHome()
        }
    }

    BackHandler(onBack = handleBack)

    // Pop-up scale animation whenever digits are entered
    val numberPopScale = remember { Animatable(1f) }
    LaunchedEffect(gameState.currentInput) {
        if (gameState.currentInput.isNotEmpty()) {
            numberPopScale.snapTo(1.16f)
            numberPopScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Back, Difficulty Badge, Restart, Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconCircleButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = strings.home,
                        testTag = "game_back_button",
                        onClick = handleBack
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.glassFillElevated,
                        border = BorderStroke(1.2.dp, palette.glassBorder)
                    ) {
                        Text(
                            text = strings.difficultyName(gameState.difficulty).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = ArcadeGold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassIconCircleButton(
                            icon = Icons.Rounded.Refresh,
                            contentDescription = strings.restart,
                            testTag = "restart_game_button",
                            onClick = onRestartGame
                        )
                        GlassIconCircleButton(
                            icon = if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                            contentDescription = strings.soundEffects,
                            testTag = "game_sound_toggle",
                            onClick = onToggleSound
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Score & Remaining Attempts + Animated Progress Indicator
                GlassCard(elevated = true, cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = strings.remainingAttempts,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.secondaryText
                                )
                                Text(
                                    text = "${gameState.attemptsRemaining} / ${gameState.maxAttempts}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (gameState.attemptsRemaining <= 2) palette.dangerAccent else palette.primaryText,
                                    modifier = Modifier.testTag("attempts_remaining_text")
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = strings.currentScore,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.secondaryText
                                )
                                Text(
                                    text = "${gameState.currentScore}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ArcadeGold,
                                    modifier = Modifier.testTag("current_score_text")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val animatedProgress by animateFloatAsState(
                            targetValue = gameState.progressFraction.coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 320),
                            label = "attempts_progress"
                        )
                        val barColor = when {
                            animatedProgress > 0.5f -> BallGreen
                            animatedProgress > 0.25f -> ArcadeGold
                            else -> palette.dangerAccent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.16f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(barColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Feedback Card (Too High / Too Low / Range / Hint)
                FeedbackBannerCard(
                    gameState = gameState,
                    activeHintText = activeHintText,
                    duplicateGuessFlag = duplicateGuessFlag
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Previous Guesses Strip
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.previousGuesses,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.secondaryText
                        )
                        Text(
                            text = strings.rangeLabel(gameState.minPossible, gameState.maxPossible),
                            style = MaterialTheme.typography.labelMedium,
                            color = ArcadeGold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (gameState.guesses.isEmpty()) {
                        Text(
                            text = strings.noGuessesYet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.secondaryText.copy(alpha = 0.85f)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 8.dp)
                        ) {
                            items(gameState.guesses.reversed()) { entry ->
                                val chipColor = when (entry.feedback) {
                                    GuessFeedback.TOO_HIGH -> ArcadeOrange
                                    GuessFeedback.TOO_LOW -> BallBlue
                                    GuessFeedback.CORRECT -> BallGreen
                                    else -> BallPurple
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = chipColor.copy(alpha = 0.28f),
                                    border = BorderStroke(1.dp, chipColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = entry.value.toString(),
                                            fontFamily = FredokaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.primaryText
                                        )
                                        Icon(
                                            imageVector = when (entry.feedback) {
                                                GuessFeedback.TOO_HIGH -> Icons.Rounded.ArrowDownward
                                                GuessFeedback.TOO_LOW -> Icons.Rounded.ArrowUpward
                                                else -> Icons.Rounded.CheckCircle
                                            },
                                            contentDescription = null,
                                            tint = chipColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Number Input Display + Hint Action Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassCard(
                        elevated = true,
                        cornerRadius = 22.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .testTag("guess_input_display")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = if (gameState.inputError) 2.dp else 0.dp,
                                    color = if (gameState.inputError) palette.dangerAccent else Color.Transparent,
                                    shape = RoundedCornerShape(22.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gameState.currentInput.ifEmpty { strings.enterNumberHint },
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (gameState.currentInput.isEmpty()) 22.sp else 36.sp,
                                color = if (gameState.currentInput.isEmpty()) {
                                    palette.secondaryText.copy(alpha = 0.55f)
                                } else {
                                    ArcadeGold
                                },
                                modifier = Modifier.graphicsLayer {
                                    scaleX = numberPopScale.value
                                    scaleY = numberPopScale.value
                                }
                            )
                        }
                    }

                    val hintsLeft = gameState.maxHints - gameState.hintsUsed
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = if (hintsLeft > 0 && !gameState.isGameOver) {
                            BallPurple.copy(alpha = 0.72f)
                        } else {
                            Color.Gray.copy(alpha = 0.25f)
                        },
                        border = BorderStroke(1.2.dp, palette.glassBorder),
                        modifier = Modifier
                            .height(72.dp)
                            .widthIn(min = 96.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .testTag("hint_button")
                            .clickable(enabled = hintsLeft > 0 && !gameState.isGameOver, onClick = onRequestHint)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = strings.hintButton,
                                tint = ArcadeGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = strings.hintsLeft(hintsLeft),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // One-Handed Ergonomic Numeric Keypad
                ArcadeNumericKeypad(
                    clearLabel = strings.clear,
                    enabled = !gameState.isGameOver,
                    onDigitClick = onDigitClick,
                    onClearClick = onClearClick,
                    onBackspaceClick = onBackspaceClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Submit Guess Button
                ArcadeButton(
                    text = strings.submitGuess,
                    icon = Icons.AutoMirrored.Rounded.Send,
                    testTag = "submit_guess_button",
                    gradientColors = listOf(ArcadeGold, ArcadeOrange),
                    contentColor = Color(0xFF1F1200),
                    enabled = !gameState.isGameOver && gameState.currentInput.isNotEmpty(),
                    onClick = onSubmitGuess,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text(strings.exitGameTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.exitGameBody) },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveDialog = false
                        onNavigateBackHome()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.dangerAccent)
                ) {
                    Text(strings.leave, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text(strings.stay)
                }
            }
        )
    }
}

@Composable
private fun FeedbackBannerCard(
    gameState: GameUiState,
    activeHintText: String?,
    duplicateGuessFlag: Boolean
) {
    val strings = LocalAppStrings.current
    val palette = LocalGamePalette.current

    val (message, accent, icon) = when {
        duplicateGuessFlag -> Triple(strings.alreadyGuessed, palette.dangerAccent, Icons.Rounded.ErrorOutline)
        gameState.feedback == GuessFeedback.INVALID -> Triple(strings.invalidInput, palette.dangerAccent, Icons.Rounded.ErrorOutline)
        gameState.feedback == GuessFeedback.TOO_HIGH -> Triple("${gameState.lastGuess} · ${strings.tooHigh}", ArcadeOrange, Icons.Rounded.ArrowDownward)
        gameState.feedback == GuessFeedback.TOO_LOW -> Triple("${gameState.lastGuess} · ${strings.tooLow}", BallBlue, Icons.Rounded.ArrowUpward)
        gameState.feedback == GuessFeedback.CORRECT -> Triple(strings.correctAnswer, BallGreen, Icons.Rounded.CheckCircle)
        else -> Triple(strings.guessPrompt, ArcadeGold, Icons.Rounded.AutoAwesome)
    }

    GlassCard(
        elevated = false,
        cornerRadius = 20.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feedback_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = message,
                transitionSpec = {
                    (fadeIn(tween(200)) + scaleIn(initialScale = 0.92f)) togetherWith fadeOut(tween(150))
                },
                label = "feedback_anim"
            ) { targetText ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = targetText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryText,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (activeHintText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BallPink.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, BallPink.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = activeHintText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.primaryText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("active_hint_text")
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcadeNumericKeypad(
    clearLabel: String,
    enabled: Boolean,
    onDigitClick: (Int) -> Unit,
    onClearClick: () -> Unit,
    onBackspaceClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7, 8, 9)
        )
        rows.forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowDigits.forEach { digit ->
                    KeypadButton(
                        label = digit.toString(),
                        testTag = "keypad_$digit",
                        enabled = enabled,
                        onClick = { onDigitClick(digit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(
                label = clearLabel,
                testTag = "keypad_clear",
                enabled = enabled,
                accentColor = BallPink,
                onClick = onClearClick,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                label = "0",
                testTag = "keypad_0",
                enabled = enabled,
                onClick = { onDigitClick(0) },
                modifier = Modifier.weight(1f)
            )
            KeypadIconButton(
                icon = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = "Backspace",
                testTag = "keypad_backspace",
                enabled = enabled,
                onClick = onBackspaceClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    testTag: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    val palette = LocalGamePalette.current
    val bgColor = accentColor?.copy(alpha = 0.35f) ?: palette.keypadSurface
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, palette.glassBorder.copy(alpha = 0.55f)),
        modifier = modifier
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(16.dp))
            .testTag(testTag)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                text = label,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (label.length > 1) 16.sp else 22.sp,
                color = palette.keypadText
            )
        }
    }
}

@Composable
private fun KeypadIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BallBlue.copy(alpha = 0.32f),
        border = BorderStroke(1.dp, palette.glassBorder.copy(alpha = 0.55f)),
        modifier = modifier
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(16.dp))
            .testTag(testTag)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = palette.keypadText
            )
        }
    }
}
