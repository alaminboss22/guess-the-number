package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcadeGold
import com.example.ui.theme.ArcadeOrange
import com.example.ui.theme.ArcadeRed
import com.example.ui.theme.BallBlue
import com.example.ui.theme.BallGreen
import com.example.ui.theme.BallOrange
import com.example.ui.theme.BallPink
import com.example.ui.theme.BallPurple
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalGamePalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class FloatingBallSpec(
    val number: String,
    val color: Color,
    val xFraction: Float,
    val yFraction: Float,
    val sizeDp: Dp,
    val phaseOffset: Float,
    val amplitudeDp: Float
)

private val ReferenceBalls = listOf(
    FloatingBallSpec("25", BallBlue, 0.12f, 0.14f, 54.dp, 0.0f, 10f),
    FloatingBallSpec("72", BallPurple, 0.08f, 0.36f, 46.dp, 1.3f, 8f),
    FloatingBallSpec("1", BallOrange, 0.14f, 0.72f, 52.dp, 2.4f, 11f),
    FloatingBallSpec("100", BallGreen, 0.88f, 0.28f, 50.dp, 0.9f, 9f),
    FloatingBallSpec("50", BallPink, 0.85f, 0.68f, 58.dp, 2.0f, 12f)
)

@Composable
fun CosmicGradientBackground(
    modifier: Modifier = Modifier,
    showFloatingBalls: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalGamePalette.current
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_bg")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ball_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.bgTop,
                        palette.bgMiddle,
                        palette.bgBottom
                    )
                )
            )
    ) {
        // Subtle radial burst rays inspired by the reference poster
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.35f)
            val radius = maxOf(size.width, size.height) * 0.95f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.radialGlow.copy(alpha = if (palette.isDark) 0.55f else 0.45f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 0.7f
                ),
                center = center,
                radius = radius * 0.7f
            )

            val rayCount = 12
            val rayAlpha = if (palette.isDark) 0.045f else 0.14f
            for (i in 0 until rayCount) {
                val startAngle = (i * (360f / rayCount)) * (PI / 180.0)
                val endAngle = startAngle + (14.0 * PI / 180.0)
                val path = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(
                        center.x + (cos(startAngle) * radius).toFloat(),
                        center.y + (sin(startAngle) * radius).toFloat()
                    )
                    lineTo(
                        center.x + (cos(endAngle) * radius).toFloat(),
                        center.y + (sin(endAngle) * radius).toFloat()
                    )
                    close()
                }
                drawPath(path = path, color = Color.White.copy(alpha = rayAlpha))
            }
        }

        if (showFloatingBalls) {
            Box(modifier = Modifier.fillMaxSize()) {
                ReferenceBalls.forEach { ball ->
                    val dy = sin(phase + ball.phaseOffset) * ball.amplitudeDp
                    val dx = cos(phase * 0.7f + ball.phaseOffset) * (ball.amplitudeDp * 0.4f)
                    NumberBall(
                        number = ball.number,
                        color = ball.color,
                        sizeDp = ball.sizeDp,
                        modifier = Modifier
                            .align(
                                androidx.compose.ui.BiasAlignment(
                                    horizontalBias = (ball.xFraction * 2f) - 1f,
                                    verticalBias = (ball.yFraction * 2f) - 1f
                                )
                            )
                            .offset(x = dx.dp, y = dy.dp)
                            .graphicsLayer { alpha = if (palette.isDark) 0.72f else 0.85f }
                    )
                }
            }
        }

        content()
    }
}

@Composable
fun NumberBall(
    number: String,
    color: Color,
    sizeDp: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .shadow(elevation = 8.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        color,
                        color.copy(
                            red = (color.red * 0.55f),
                            green = (color.green * 0.55f),
                            blue = (color.blue * 0.55f)
                        )
                    ),
                    center = Offset(0.3f * sizeDp.value, 0.3f * sizeDp.value)
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (sizeDp.value * 0.36f).sp,
            color = Color.White
        )
    }
}

@Composable
fun AnimatedQuestionMark3D(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "question_3d")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "q_rotate"
    )
    val bobY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "q_bob"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = rotation
                translationY = bobY
            },
        contentAlignment = Alignment.Center
    ) {
        // Glowing halo behind the 3D question mark
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ArcadeGold.copy(alpha = 0.42f),
                        BallPink.copy(alpha = 0.18f),
                        Color.Transparent
                    )
                )
            )
            // Accent spark triangles like the reference art
            val sparkColor = ArcadeGold
            val p1 = Path().apply {
                moveTo(this@Canvas.size.width * 0.84f, this@Canvas.size.height * 0.12f)
                lineTo(this@Canvas.size.width * 0.96f, this@Canvas.size.height * 0.06f)
                lineTo(this@Canvas.size.width * 0.92f, this@Canvas.size.height * 0.22f)
                close()
            }
            drawPath(p1, color = sparkColor)
        }

        // 3D Extruded Ruby-Red Depth Layer
        Text(
            text = "?",
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.78f).sp,
            color = ArcadeRed,
            modifier = Modifier.offset(x = 4.dp, y = 5.dp)
        )
        // Mid Orange Bevel Layer
        Text(
            text = "?",
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.78f).sp,
            color = ArcadeOrange,
            modifier = Modifier.offset(x = 2.dp, y = 2.dp)
        )
        // Bright Golden Front Face
        Text(
            text = "?",
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.78f).sp,
            color = ArcadeGold
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalGamePalette.current
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (elevated) palette.glassFillElevated else palette.glassFill,
        border = BorderStroke(
            width = 1.25.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    palette.glassBorder,
                    palette.glassBorder.copy(alpha = 0.25f)
                )
            )
        ),
        shadowElevation = if (palette.isDark) 0.dp else 6.dp
    ) {
        Box(content = content)
    }
}

@Composable
fun ArcadeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(ArcadeGold, ArcadeOrange),
    contentColor: Color = Color(0xFF1F1200),
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "btn_scale"
    )

    val effectiveColors = if (enabled) {
        gradientColors
    } else {
        listOf(Color.Gray.copy(alpha = 0.45f), Color.Gray.copy(alpha = 0.35f))
    }

    val baseModifier = modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .heightIn(min = 54.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(Brush.horizontalGradient(effectiveColors))
        .border(
            width = 1.5.dp,
            color = Color.White.copy(alpha = if (enabled) 0.55f else 0.2f),
            shape = RoundedCornerShape(20.dp)
        )
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(),
            enabled = enabled,
            role = Role.Button,
            onClick = onClick
        )
        .padding(horizontal = 20.dp, vertical = 14.dp)

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) contentColor else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (enabled) contentColor else Color.White.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}

private data class ConfettiParticle(
    val xFraction: Float,
    val speedMultiplier: Float,
    val sizeDp: Float,
    val color: Color,
    val swayAmplitude: Float,
    val phase: Float,
    val initialRotation: Float
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier
) {
    val particles = remember {
        val colors = listOf(ArcadeGold, BallPink, BallBlue, BallGreen, ArcadeOrange, BallPurple)
        val rng = Random(42)
        List(44) {
            ConfettiParticle(
                xFraction = rng.nextFloat(),
                speedMultiplier = 0.6f + rng.nextFloat() * 0.9f,
                sizeDp = 6f + rng.nextFloat() * 8f,
                color = colors[it % colors.size],
                swayAmplitude = 18f + rng.nextFloat() * 28f,
                phase = rng.nextFloat() * 6.28f,
                initialRotation = rng.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            val yFrac = ((progress * p.speedMultiplier) + (p.phase / 6.28f)) % 1f
            val x = p.xFraction * size.width + sin(progress * 6.28f + p.phase) * p.swayAmplitude
            val y = yFrac * size.height
            val particlePx = p.sizeDp.dp.toPx()
            rotate(
                degrees = p.initialRotation + progress * 360f,
                pivot = Offset(x, y)
            ) {
                drawRect(
                    color = p.color.copy(alpha = 0.9f),
                    topLeft = Offset(x - particlePx / 2f, y - particlePx / 3f),
                    size = Size(particlePx, particlePx * 0.65f)
                )
            }
        }
    }
}
