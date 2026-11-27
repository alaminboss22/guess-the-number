package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalAppStrings
import com.example.model.GameResult
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ConfettiOverlay
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
fun ResultScreen(
    result: GameResult?,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val strings = LocalAppStrings.current
    val palette = LocalGamePalette.current

    BackHandler(onBack = onGoHome)

    if (result == null) {
        onGoHome()
        return
    }

    val badgeScale = remember { Animatable(0.4f) }
    val loseShakeX = remember { Animatable(0f) }
    var animateScoreTarget by remember { mutableStateOf(0) }

    LaunchedEffect(result) {
        animateScoreTarget = result.score
        badgeScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        if (!result.isWin) {
            listOf(-18f, 18f, -12f, 12f, -6f, 0f).forEach { target ->
                loseShakeX.animateTo(target, animationSpec = tween(55, easing = FastOutSlowInEasing))
            }
        }
    }

    val animatedScore by animateIntAsState(
        targetValue = animateScoreTarget,
        animationSpec = tween(durationMillis = 700),
        label = "score_countup"
    )

    CosmicGradientBackground(showFloatingBalls = true) {
        if (result.isWin) {
            ConfettiOverlay()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hero Win/Lose Emblem
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .graphicsLayer {
                            scaleX = badgeScale.value
                            scaleY = badgeScale.value
                            translationX = loseShakeX.value
                        }
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (result.isWin) {
                                    listOf(ArcadeGold, ArcadeOrange)
                                } else {
                                    listOf(BallPink, BallPurple)
                                }
                            )
                        )
                        .border(3.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (result.isWin) Icons.Rounded.EmojiEvents else Icons.Rounded.SentimentDissatisfied,
                        contentDescription = if (result.isWin) strings.youWin else strings.gameOver,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (result.isWin) strings.youWin else strings.gameOver,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = if (result.isWin) ArcadeGold else palette.dangerAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("result_status_title")
                )

                if (result.isNewRecord && result.isWin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = BallGreen.copy(alpha = 0.25f),
                        border = BorderStroke(1.5.dp, BallGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = ArcadeGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = strings.newRecord,
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.primaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Secret Number Reveal + Stats Card
                GlassCard(elevated = true, cornerRadius = 26.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.secretNumber,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.secondaryText
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(BallBlue, BallPurple)
                                    )
                                )
                                .border(2.5.dp, ArcadeGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = result.secretNumber.toString(),
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = Color.White,
                                modifier = Modifier.testTag("result_secret_number")
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ResultMetricBox(
                                label = strings.attemptsUsed,
                                value = "${result.attemptsUsed} / ${result.maxAttempts}",
                                valueColor = palette.primaryText,
                                testTag = "result_attempts_used",
                                modifier = Modifier.weight(1f)
                            )
                            ResultMetricBox(
                                label = strings.finalScore,
                                value = animatedScore.toString(),
                                valueColor = ArcadeGold,
                                testTag = "result_final_score",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ResultMetricBox(
                            label = strings.bestScore,
                            value = result.bestScore.toString(),
                            valueColor = BallGreen,
                            testTag = "result_best_score",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                ArcadeButton(
                    text = strings.playAgain,
                    icon = Icons.Rounded.Refresh,
                    testTag = "play_again_button",
                    gradientColors = listOf(ArcadeGold, ArcadeOrange),
                    contentColor = Color(0xFF1F1200),
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ArcadeButton(
                    text = strings.home,
                    icon = Icons.Rounded.Home,
                    testTag = "result_home_button",
                    gradientColors = listOf(BallBlue, BallPurple),
                    contentColor = Color.White,
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ResultMetricBox(
    label: String,
    value: String,
    valueColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalGamePalette.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = palette.glassFill,
        border = BorderStroke(1.dp, palette.glassBorder.copy(alpha = 0.5f)),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = palette.secondaryText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}
