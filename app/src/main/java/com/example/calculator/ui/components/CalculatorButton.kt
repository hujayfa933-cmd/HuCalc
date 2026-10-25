package com.example.calculator.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCalculatorColors

enum class CalculatorButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    SCIENTIFIC,
    CLEAR,
    EQUALS,
    ACCENT_TOGGLE
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: CalculatorButtonType = CalculatorButtonType.NUMBER,
    secondaryText: String? = null,
    isSecondaryActive: Boolean = false,
    contentDesc: String = text,
    fontSize: TextUnit = 20.sp,
    isActiveToggle: Boolean = false,
    cornerRadius: Dp = 4.dp,
    onLongClick: (() -> Unit)? = null
) {
    val colors = LocalCalculatorColors.current
    val isHapticEnabled = com.example.ui.theme.LocalHapticEnabled.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Immediate zero-latency tactile travel (no frame animations)
    val pressOffset = if (isPressed) 1.5.dp else 0.dp

    // Color resolution without object allocations
    val topColor: Color
    val bottomColor: Color
    val rimColor: Color
    val shadowColor: Color
    val textColor: Color

    when (type) {
        CalculatorButtonType.NUMBER -> {
            topColor = colors.keyNumberTop
            bottomColor = colors.keyNumberBottom
            rimColor = colors.keyNumberRim
            shadowColor = colors.keyNumberShadow
            textColor = colors.keyNumberText
        }
        CalculatorButtonType.OPERATOR -> {
            topColor = colors.keyOperatorTop
            bottomColor = colors.keyOperatorBottom
            rimColor = colors.keyOperatorRim
            shadowColor = colors.keyOperatorShadow
            textColor = colors.keyOperatorText
        }
        CalculatorButtonType.FUNCTION -> {
            topColor = colors.keyFunctionTop
            bottomColor = colors.keyFunctionBottom
            rimColor = colors.keyFunctionRim
            shadowColor = colors.keyFunctionShadow
            textColor = colors.keyFunctionText
        }
        CalculatorButtonType.SCIENTIFIC -> {
            if (isActiveToggle) {
                topColor = colors.keyOperatorTop
                bottomColor = colors.keyOperatorBottom
                rimColor = colors.keyOperatorRim
                shadowColor = colors.keyOperatorShadow
                textColor = colors.keyOperatorText
            } else {
                topColor = colors.keyScientificTop
                bottomColor = colors.keyScientificBottom
                rimColor = colors.keyScientificRim
                shadowColor = colors.keyScientificShadow
                textColor = colors.keyScientificText
            }
        }
        CalculatorButtonType.CLEAR -> {
            topColor = colors.keyClearTop
            bottomColor = colors.keyClearBottom
            rimColor = colors.keyClearRim
            shadowColor = colors.keyClearShadow
            textColor = Color.White
        }
        CalculatorButtonType.EQUALS -> {
            topColor = colors.keyEqualsTop
            bottomColor = colors.keyEqualsBottom
            rimColor = colors.keyEqualsRim
            shadowColor = colors.keyEqualsShadow
            textColor = Color.White
        }
        CalculatorButtonType.ACCENT_TOGGLE -> {
            if (isActiveToggle) {
                topColor = colors.keyOperatorTop
                bottomColor = colors.keyOperatorBottom
                rimColor = colors.keyOperatorRim
                shadowColor = colors.keyOperatorShadow
                textColor = colors.keyOperatorText
            } else {
                topColor = colors.keyFunctionTop
                bottomColor = colors.keyFunctionBottom
                rimColor = colors.keyFunctionRim
                shadowColor = colors.keyFunctionShadow
                textColor = colors.keyFunctionText
            }
        }
    }

    // Cached tag computation
    val sanitizedTag = remember(text) {
        "btn_" + text.replace(" ", "_")
            .replace("(", "open_paren")
            .replace(")", "close_paren")
            .replace("÷", "div")
            .replace("×", "mul")
            .replace("−", "sub")
            .replace("+", "add")
            .replace("=", "equals")
            .replace(".", "dot")
            .replace("%", "percent")
            .replace("!", "fact")
            .replace("^", "pow")
            .replace("√", "sqrt")
            .replace("π", "pi")
    }

    val keyBrush = remember(isPressed, topColor, bottomColor) {
        Brush.verticalGradient(
            listOf(
                if (isPressed) bottomColor else topColor,
                bottomColor
            )
        )
    }

    val textWeight = remember(type) {
        when (type) {
            CalculatorButtonType.NUMBER, CalculatorButtonType.OPERATOR,
            CalculatorButtonType.EQUALS, CalculatorButtonType.CLEAR -> FontWeight.Bold
            else -> FontWeight.SemiBold
        }
    }

    // Molded Aluminum Key Well
    Box(
        modifier = modifier
            .semantics { contentDescription = contentDesc }
            .testTag(sanitizedTag)
            .padding(horizontal = 1.5.dp, vertical = 1.5.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shadowColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (isHapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onClick()
                },
                onLongClick = if (onLongClick != null) {
                    {
                        if (isHapticEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onLongClick()
                    }
                } else null
            )
    ) {
        // High-precision Machined Keycap (Fast standard Box without BoxWithConstraints subcomposition)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = pressOffset,
                    bottom = if (isPressed) 0.5.dp else 1.6.dp
                )
                .clip(RoundedCornerShape(cornerRadius))
                .background(keyBrush)
                .border(0.8.dp, rimColor.copy(alpha = if (isPressed) 0.4f else 0.85f), RoundedCornerShape(cornerRadius))
                .drawBehind {
                    // Specular reflection line along top polished chamfer
                    val highlightAlpha = if (isPressed) 0.15f else (if (colors.isDark) 0.35f else 0.7f)
                    drawLine(
                        color = Color.White.copy(alpha = highlightAlpha),
                        start = Offset(3f, 1.2f),
                        end = Offset(size.width - 3f, 1.2f),
                        strokeWidth = 1.6f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (secondaryText != null) {
                // Dual label key (Secondary Gold + Primary White/Dark)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 1.dp, vertical = 1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = secondaryText,
                        color = if (isSecondaryActive) colors.secondaryLabelGold else colors.secondaryLabelGold.copy(alpha = 0.9f),
                        fontSize = 8.5.sp,
                        fontWeight = if (isSecondaryActive) FontWeight.ExtraBold else FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = true
                            )
                        )
                    )

                    Text(
                        text = text,
                        color = textColor,
                        fontSize = (fontSize.value * 0.75f).coerceIn(11f, 14.5f).sp,
                        fontWeight = textWeight,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = true
                            )
                        )
                    )
                }
            } else {
                // Single label key
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = textColor,
                        fontSize = fontSize,
                        fontWeight = textWeight,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = true
                            )
                        )
                    )
                }
            }
        }
    }
}
