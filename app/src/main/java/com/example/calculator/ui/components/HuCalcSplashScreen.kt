package com.example.calculator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ChromeMirrorHighlight
import com.example.ui.theme.LocalCalculatorColors
import kotlinx.coroutines.delay

@Composable
fun HuCalcSplashScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    var isVisible by remember { mutableStateOf(true) }

    val scaleAnim = remember { Animatable(0.85f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fast crisp entrance
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 150)
        )
        // Brief 400ms display for fast boot
        delay(400)
        isVisible = false
        delay(120)
        onDismiss()
    }

    val dismissImmediate = {
        isVisible = false
        onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(120)) + scaleIn(tween(120), initialScale = 0.97f),
        exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 1.02f)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.chassisBrush)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = dismissImmediate
                )
                .testTag("splash_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
            ) {
                // Outer Polished Plaque Frame
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ChromeMirrorHighlight.copy(alpha = 0.9f),
                                    colors.lcdBezelOuter,
                                    colors.chassisMid,
                                    colors.lcdBezelInner
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = colors.chassisHighlight,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner Logo Container
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.chassisMid)
                            .border(1.dp, colors.chassisHighlight, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_hucalc_logo_pm),
                            contentDescription = "HuCalc Logo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App Name
                Text(
                    text = "HuCalc",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = "fx-991ES PLUS • 100% Offline Math Engine",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.buttonOperator,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Prominent "Made by Hujayfa" Badge
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = colors.lcdBezelInner.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        colors.secondaryLabelGold.copy(alpha = 0.8f)
                    ),
                    shadowElevation = 6.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "Made by Hujayfa",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Version 1.0.5 Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.chassisMid.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        colors.chassisHighlight.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = "Version 1.0.6",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Auto-start indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = colors.buttonOperator,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Starting automatically…",
                        fontSize = 12.sp,
                        color = colors.textSecondary.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
