package com.example.calculator.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.data.gemini.GeminiMathService
import com.example.calculator.data.gemini.MathAssistantMessage
import com.example.calculator.data.gemini.MessageSender
import com.example.ui.theme.LocalCalculatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiMathAssistantBottomSheet(
    messages: List<MathAssistantMessage>,
    isLoading: Boolean,
    selectedModel: String,
    errorMessage: String?,
    currentExpression: String,
    currentDisplayValue: String,
    isApiKeyConfigured: Boolean,
    onSendMessage: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onClearChat: () -> Unit,
    onInsertResultToCalculator: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalCalculatorColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.chassisTop,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = colors.chassisHighlight.copy(alpha = 0.8f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .imePadding()
                .testTag("gemini_math_assistant_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.keyOperatorTop),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini AI",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Gemini Math Assistant",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        // Model Selector Pill
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.lcdBezelInner)
                                    .clickable { showModelMenu = true }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val modelLabel = when (selectedModel) {
                                    GeminiMathService.MODEL_OFFLINE -> "⚡ Offline Engine (On-Device)"
                                    GeminiMathService.MODEL_PRO -> "Gemini 3.1 Pro (Cloud)"
                                    else -> "Gemini 3.5 Flash (Cloud)"
                                }
                                Text(
                                    text = modelLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedModel == GeminiMathService.MODEL_OFFLINE) colors.textPrimary else colors.secondaryLabelGold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Model",
                                    tint = colors.secondaryLabelGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showModelMenu,
                                onDismissRequest = { showModelMenu = false },
                                modifier = Modifier.background(colors.lcdBezelInner)
                            ) {
                                GeminiMathService.AVAILABLE_MODELS.forEach { (modelId, label) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = if (selectedModel == modelId) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedModel == modelId) colors.secondaryLabelGold else colors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            onSelectModel(modelId)
                                            showModelMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier.size(34.dp).testTag("btn_clear_assistant_chat")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear conversation",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp).testTag("btn_close_assistant")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = colors.chassisHighlight.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Calculator Context Banner
            val hasActiveCalc = currentExpression.isNotBlank() || (currentDisplayValue != "0" && currentDisplayValue.isNotBlank())
            if (hasActiveCalc) {
                Surface(
                    color = colors.lcdBezelInner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.chassisHighlight.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CALCULATOR ACTIVE STATE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.secondaryLabelGold,
                                letterSpacing = 0.5.sp
                            )
                            val calcSummary = if (currentExpression.isNotBlank() && currentExpression != currentDisplayValue) {
                                "$currentExpression = $currentDisplayValue"
                            } else {
                                currentDisplayValue
                            }
                            Text(
                                text = calcSummary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                onSendMessage("Explain step-by-step how to solve or calculate: $currentDisplayValue (from expression: $currentExpression)")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.keyOperatorTop,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp).testTag("btn_explain_active_calc")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Explain", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Offline Status / Cloud API Key Indicator
            if (selectedModel == GeminiMathService.MODEL_OFFLINE) {
                Surface(
                    color = colors.lcdBezelInner,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.chassisHighlight.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = colors.secondaryLabelGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "100% Offline Mode Active • Zero internet or API keys needed. All equations, steps, and formulas process locally on your phone.",
                            fontSize = 11.sp,
                            color = colors.textPrimary,
                            lineHeight = 15.sp
                        )
                    }
                }
            } else if (!isApiKeyConfigured) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Cloud Gemini models require GEMINI_API_KEY. You can switch to the '⚡ Offline Engine' above for 100% offline solving with no key needed.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Quick Topic Chips
            val quickChips = listOf(
                "📐 Solve equation" to "Solve for x: ",
                "📈 Derivative" to "Find the derivative d/dx of: ",
                "∫ Integral" to "Compute the integral of: ",
                "💡 Explain formula" to "Explain the formula: ",
                "🔄 Simplify" to "Simplify this mathematical expression: ",
                "🧪 Physics / STEM" to "Calculate physics problem: "
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickChips.forEach { (label, prefix) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.keyFunctionTop,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.keyFunctionRim),
                        modifier = Modifier
                            .clickable {
                                inputText = if (hasActiveCalc) {
                                    "$prefix$currentDisplayValue"
                                } else {
                                    prefix
                                }
                            }
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.keyFunctionText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Error display banner
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Chat Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    // Empty State with Guidance
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.keyOperatorTop.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = colors.keyOperatorTop,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "HuCalc Math Assistant • 100% Offline",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Solve equations, quadratic formulas, step-by-step arithmetic, calculus rules, trigonometry exact values, prime factors, and physics constants entirely on your device with zero internet connection!",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Suggested example questions
                        val sampleQuestions = listOf(
                            "Solve 2x² - 5x + 3 = 0 with steps",
                            "Prime factors of 360",
                            "Find derivative d/dx of sin(x)",
                            "What is 25% of 160?"
                        )

                        sampleQuestions.forEach { question ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.lcdBezelInner,
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.chassisHighlight.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { onSendMessage(question) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = colors.secondaryLabelGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = question,
                                        fontSize = 11.5.sp,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageCard(
                                message = msg,
                                onCopy = { textToCopy ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Math Solution", textToCopy))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                onInsertToCalculator = { resultVal ->
                                    onInsertResultToCalculator(resultVal)
                                    Toast.makeText(context, "Inserted '$resultVal' into Calculator", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        if (isLoading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = colors.keyOperatorTop,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Gemini is solving step-by-step...",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = colors.chassisBottom,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.chassisHighlight.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Button to paste active formula
                    if (hasActiveCalc) {
                        IconButton(
                            onClick = {
                                val active = if (currentExpression.isNotBlank()) currentExpression else currentDisplayValue
                                inputText = if (inputText.isNotBlank()) "$inputText $active" else active
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.lcdBezelInner)
                                .testTag("btn_paste_calc_formula")
                        ) {
                            Text(
                                text = "Expr",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.secondaryLabelGold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("assistant_input_field"),
                        placeholder = {
                            Text(
                                text = "Type math problem or equation...",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.keyOperatorTop,
                            unfocusedBorderColor = colors.chassisHighlight.copy(alpha = 0.6f),
                            focusedContainerColor = colors.lcdBezelInner,
                            unfocusedContainerColor = colors.lcdBezelInner
                        ),
                        shape = RoundedCornerShape(6.dp),
                        maxLines = 3,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val query = inputText.trim()
                                inputText = ""
                                onSendMessage(query)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (inputText.isNotBlank() && !isLoading) colors.keyOperatorTop else colors.keyFunctionTop)
                            .testTag("btn_send_assistant")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isLoading) Color.White else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageCard(
    message: MathAssistantMessage,
    onCopy: (String) -> Unit,
    onInsertToCalculator: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            if (!isUser) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.secondaryLabelGold,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = if (isUser) "You" else "Gemini Math",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUser) colors.keyOperatorTop else colors.secondaryLabelGold
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = if (isUser) 8.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 8.dp
            ),
            color = if (isUser) colors.keyFunctionTop else colors.lcdBezelInner,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) colors.keyFunctionRim else colors.chassisHighlight.copy(alpha = 0.7f)
            ),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                SelectionContainer {
                    Text(
                        text = message.text,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        color = colors.textPrimary,
                        fontFamily = if (isUser) FontFamily.SansSerif else FontFamily.SansSerif
                    )
                }

                // Extracted Final Answer Action Banner
                if (!isUser && !message.extractedAnswer.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.keyOperatorTop.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.keyOperatorTop),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CALCULATED ANSWER",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.secondaryLabelGold
                                )
                                Text(
                                    text = message.extractedAnswer,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = { onInsertToCalculator(message.extractedAnswer) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.keyOperatorTop,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(3.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp).testTag("btn_insert_calc_result")
                            ) {
                                Text(
                                    text = "▶ Use in Calc",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Copy Action Button
                if (!isUser) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { onCopy(message.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy answer",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
