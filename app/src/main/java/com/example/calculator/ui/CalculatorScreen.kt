package com.example.calculator.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.data.CalculationHistoryEntity
import com.example.calculator.ui.components.AboutDialog
import com.example.calculator.ui.components.CalculatorDisplay
import com.example.calculator.ui.components.CalculatorKeypad
import com.example.calculator.ui.components.GeminiMathAssistantBottomSheet
import com.example.calculator.ui.components.HistoryBottomSheet
import com.example.calculator.ui.components.MachinedScrewRivet
import com.example.calculator.ui.components.ScientificConstantsDialog
import com.example.calculator.ui.components.SettingsDialog
import com.example.calculator.ui.components.UnitConversionDialog
import com.example.calculator.ui.localization.AppLanguage
import com.example.calculator.ui.localization.HuCalcStrings
import com.example.calculator.ui.localization.LocalHuCalcStrings
import com.example.ui.theme.LocalCalculatorColors
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val isHaptic by viewModel.isHapticEnabled.collectAsStateWithLifecycle()
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showHistory by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showConstantsDialog by remember { mutableStateOf(false) }
    var showConversionsDialog by remember { mutableStateOf(false) }
    var showGeminiAssistant by remember { mutableStateOf(false) }

    // Predictive / Android Back Navigation
    BackHandler(
        enabled = showGeminiAssistant || showHistory || showAbout || showSettings || showConstantsDialog || showConversionsDialog || uiState.expression.isNotEmpty()
    ) {
        when {
            showGeminiAssistant -> showGeminiAssistant = false
            showHistory -> showHistory = false
            showAbout -> showAbout = false
            showSettings -> showSettings = false
            showConstantsDialog -> showConstantsDialog = false
            showConversionsDialog -> showConversionsDialog = false
            uiState.expression.isNotEmpty() -> viewModel.onDelete()
        }
    }

    val strings = remember(uiState.language) { HuCalcStrings(uiState.language) }

    CompositionLocalProvider(
        LocalHuCalcStrings provides strings,
        com.example.ui.theme.LocalHapticEnabled provides isHaptic
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(colors.chassisBrush),
            containerColor = Color.Transparent,
            topBar = {
                PolishedMetalTopBar(
                    currentMode = uiState.mode,
                    themeMode = uiState.themeMode,
                    currentLanguage = uiState.language,
                    historyCount = historyList.size,
                    isHapticEnabled = isHaptic,
                    onToggleHaptics = { viewModel.toggleHaptics() },
                    onSelectLanguage = { viewModel.setLanguage(it) },
                    onSwitchMode = { viewModel.switchMode(it) },
                    onCycleTheme = { viewModel.cycleThemeMode() },
                    onOpenHistory = { showHistory = true },
                    onOpenSettings = { showSettings = true },
                    onOpenAbout = { showAbout = true },
                    onOpenGeminiAssistant = { showGeminiAssistant = true }
                )
            }
        ) { innerPadding ->
            // Main Metal Chassis Container with Chamfer Rim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                    ) {
                        CalculatorDisplay(
                            displayValue = uiState.displayValue,
                            expression = uiState.expression,
                            previousExpression = uiState.previousExpression,
                            mode = uiState.mode,
                            angleMode = uiState.angleMode,
                            isInverse = uiState.isInverse,
                            isAlpha = uiState.isAlpha,
                            hasMemory = uiState.hasMemory,
                            isError = uiState.errorMessage != null,
                            onToggleAngleMode = { viewModel.toggleAngleMode() },
                            onOpenGeminiAssistant = { showGeminiAssistant = true },
                            onFractionToggle = { viewModel.onFractionToggle() },
                            onQuickSolveSteps = {
                                viewModel.onQuickSolveCurrent()
                                showGeminiAssistant = true
                            },
                            onPaste = { text -> viewModel.onPasteFromClipboard(text) },
                            modifier = Modifier.wrapContentHeight()
                        )

                        QuickConvenienceBar(
                            lastHistoryItem = historyList.firstOrNull(),
                            onUseLastResult = { res -> viewModel.insertMathResultToCalculator(res) },
                            onUseAns = { viewModel.onAns() },
                            onQuickSolve = {
                                viewModel.onQuickSolveCurrent()
                                showGeminiAssistant = true
                            }
                        )
                    }

                    CalculatorKeypad(
                        mode = uiState.mode,
                        isInverse = uiState.isInverse,
                        isAlpha = uiState.isAlpha,
                        angleMode = uiState.angleMode,
                        onDigit = viewModel::onDigit,
                        onOperator = viewModel::onOperator,
                        onDecimal = viewModel::onDecimal,
                        onToggleSign = viewModel::onToggleSign,
                        onClear = viewModel::onClear,
                        onDelete = viewModel::onDelete,
                        onEquals = viewModel::onEquals,
                        onPercentage = viewModel::onPercentage,
                        onFunction = viewModel::onFunction,
                        onSquare = viewModel::onSquare,
                        onCube = viewModel::onCube,
                        onPowerY = viewModel::onPowerY,
                        onNthRoot = viewModel::onNthRoot,
                        onReciprocal = viewModel::onReciprocal,
                        onAbsoluteValue = viewModel::onAbsoluteValue,
                        onFactorial = viewModel::onFactorial,
                        onConstant = viewModel::onConstant,
                        onParenthesis = viewModel::onParenthesis,
                        onExp = viewModel::onExp,
                        onToggleAngleMode = viewModel::toggleAngleMode,
                        onToggleInverse = viewModel::toggleInverse,
                        onToggleAlpha = viewModel::toggleAlpha,
                        onAns = viewModel::onAns,
                        onReplay = viewModel::onReplayPrevious,
                        onFractionToggle = viewModel::onFractionToggle,
                        onEng = viewModel::onEng,
                        onDms = viewModel::onDms,
                        onMemoryAdd = viewModel::onMemoryAdd,
                        onMemorySubtract = viewModel::onMemorySubtract,
                        onMemoryRecall = viewModel::onMemoryRecall,
                        onMemoryClear = viewModel::onMemoryClear,
                        onPermutation = viewModel::onPermutation,
                        onCombination = viewModel::onCombination,
                        onRandomNumber = viewModel::onRandomNumber,
                        onOpenConstants = { showConstantsDialog = true },
                        onOpenConversions = { showConversionsDialog = true },
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    CalculatorDisplay(
                        displayValue = uiState.displayValue,
                        expression = uiState.expression,
                        previousExpression = uiState.previousExpression,
                        mode = uiState.mode,
                        angleMode = uiState.angleMode,
                        isInverse = uiState.isInverse,
                        isAlpha = uiState.isAlpha,
                        hasMemory = uiState.hasMemory,
                        isError = uiState.errorMessage != null,
                        onToggleAngleMode = { viewModel.toggleAngleMode() },
                        onOpenGeminiAssistant = { showGeminiAssistant = true },
                        onFractionToggle = { viewModel.onFractionToggle() },
                        onQuickSolveSteps = {
                            viewModel.onQuickSolveCurrent()
                            showGeminiAssistant = true
                        },
                        onPaste = { text -> viewModel.onPasteFromClipboard(text) },
                        modifier = Modifier.wrapContentHeight()
                    )

                    // Quick Mini-History & Action Strip
                    QuickConvenienceBar(
                        lastHistoryItem = historyList.firstOrNull(),
                        onUseLastResult = { res -> viewModel.insertMathResultToCalculator(res) },
                        onUseAns = { viewModel.onAns() },
                        onQuickSolve = {
                            viewModel.onQuickSolveCurrent()
                            showGeminiAssistant = true
                        }
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    CalculatorKeypad(
                        mode = uiState.mode,
                        isInverse = uiState.isInverse,
                        isAlpha = uiState.isAlpha,
                        angleMode = uiState.angleMode,
                        onDigit = viewModel::onDigit,
                        onOperator = viewModel::onOperator,
                        onDecimal = viewModel::onDecimal,
                        onToggleSign = viewModel::onToggleSign,
                        onClear = viewModel::onClear,
                        onDelete = viewModel::onDelete,
                        onEquals = viewModel::onEquals,
                        onPercentage = viewModel::onPercentage,
                        onFunction = viewModel::onFunction,
                        onSquare = viewModel::onSquare,
                        onCube = viewModel::onCube,
                        onPowerY = viewModel::onPowerY,
                        onNthRoot = viewModel::onNthRoot,
                        onReciprocal = viewModel::onReciprocal,
                        onAbsoluteValue = viewModel::onAbsoluteValue,
                        onFactorial = viewModel::onFactorial,
                        onConstant = viewModel::onConstant,
                        onParenthesis = viewModel::onParenthesis,
                        onExp = viewModel::onExp,
                        onToggleAngleMode = viewModel::toggleAngleMode,
                        onToggleInverse = viewModel::toggleInverse,
                        onToggleAlpha = viewModel::toggleAlpha,
                        onAns = viewModel::onAns,
                        onReplay = viewModel::onReplayPrevious,
                        onFractionToggle = viewModel::onFractionToggle,
                        onEng = viewModel::onEng,
                        onDms = viewModel::onDms,
                        onMemoryAdd = viewModel::onMemoryAdd,
                        onMemorySubtract = viewModel::onMemorySubtract,
                        onMemoryRecall = viewModel::onMemoryRecall,
                        onMemoryClear = viewModel::onMemoryClear,
                        onPermutation = viewModel::onPermutation,
                        onCombination = viewModel::onCombination,
                        onRandomNumber = viewModel::onRandomNumber,
                        onOpenConstants = { showConstantsDialog = true },
                        onOpenConversions = { showConversionsDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }

        if (showHistory) {
            HistoryBottomSheet(
                historyList = historyList,
                onDismiss = { showHistory = false },
                onSelectItem = { item, useResult -> viewModel.onHistoryItemClick(item, useResult) },
                onClearAll = { viewModel.clearHistory() },
                onDeleteItem = { id -> viewModel.deleteHistoryItem(id) }
            )
        }

        if (showAbout) {
            AboutDialog(
                onDismiss = { showAbout = false }
            )
        }

        if (showSettings) {
            SettingsDialog(
                themeMode = uiState.themeMode,
                isHapticEnabled = isHaptic,
                currentLanguage = uiState.language,
                angleMode = uiState.angleMode,
                historyCount = historyList.size,
                onSelectTheme = { viewModel.setThemeMode(it) },
                onSelectLanguage = { viewModel.setLanguage(it) },
                onToggleHaptics = { viewModel.toggleHaptics() },
                onToggleAngleMode = { viewModel.toggleAngleMode() },
                onClearHistory = { viewModel.clearHistory() },
                onDismiss = { showSettings = false }
            )
        }

        if (showConstantsDialog) {
            ScientificConstantsDialog(
                onDismiss = { showConstantsDialog = false },
                onSelectConstant = { constItem ->
                    viewModel.onInsertConstant(constItem.symbol, constItem.value)
                    showConstantsDialog = false
                }
            )
        }

        if (showConversionsDialog) {
            UnitConversionDialog(
                onDismiss = { showConversionsDialog = false },
                onSelectConversion = { conv ->
                    viewModel.onApplyConversion(conv)
                    showConversionsDialog = false
                }
            )
        }

        if (showGeminiAssistant) {
            val assistantMessages by viewModel.assistantMessages.collectAsStateWithLifecycle()
            val isAssistantLoading by viewModel.isAssistantLoading.collectAsStateWithLifecycle()
            val selectedModel by viewModel.assistantSelectedModel.collectAsStateWithLifecycle()
            val assistantError by viewModel.assistantError.collectAsStateWithLifecycle()

            GeminiMathAssistantBottomSheet(
                messages = assistantMessages,
                isLoading = isAssistantLoading,
                selectedModel = selectedModel,
                errorMessage = assistantError,
                currentExpression = uiState.expression,
                currentDisplayValue = uiState.displayValue,
                isApiKeyConfigured = viewModel.isGeminiKeyConfigured(),
                onSendMessage = { prompt -> viewModel.askGeminiMathAssistant(prompt) },
                onSelectModel = { model -> viewModel.selectAssistantModel(model) },
                onClearChat = { viewModel.clearAssistantChat() },
                onInsertResultToCalculator = { value ->
                    viewModel.insertMathResultToCalculator(value)
                    showGeminiAssistant = false
                },
                onDismiss = { showGeminiAssistant = false }
            )
        }
    }
}
}

/**
 * Convenient Mini-History & Quick Actions Bar:
 * Displays previous calculation result, 1-tap 'Ans', and 'Steps' shortcut.
 */
@Composable
private fun QuickConvenienceBar(
    lastHistoryItem: CalculationHistoryEntity?,
    onUseLastResult: (String) -> Unit,
    onUseAns: () -> Unit,
    onQuickSolve: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val strings = LocalHuCalcStrings.current

    Surface(
        color = colors.lcdBezelInner.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, colors.chassisHighlight.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (lastHistoryItem != null) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUseLastResult(lastHistoryItem.result) }
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Previous Calculation",
                        tint = colors.secondaryLabelGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${strings.quickAns}: ${lastHistoryItem.result}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "(${lastHistoryItem.expression})",
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }
            } else {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.quickBrandLine,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.keyFunctionTop,
                    modifier = Modifier.clickable { onUseAns() }
                ) {
                    Text(
                        text = strings.quickAns,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.keyFunctionText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.keyOperatorTop,
                    modifier = Modifier.clickable { onQuickSolve() }
                ) {
                    Text(
                        text = strings.quickSteps,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PolishedMetalTopBar(
    currentMode: CalculatorMode,
    themeMode: ThemeMode,
    currentLanguage: AppLanguage,
    historyCount: Int,
    isHapticEnabled: Boolean,
    onToggleHaptics: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSwitchMode: (CalculatorMode) -> Unit,
    onCycleTheme: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenGeminiAssistant: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    val strings = LocalHuCalcStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Machined Aluminum Slider Switch with Tactile Grip Grooves
        Surface(
            shape = RoundedCornerShape(2.dp),
            color = colors.lcdBezelInner,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.chassisHighlight.copy(alpha = 0.8f)),
            modifier = Modifier.testTag("mode_selector_pill")
        ) {
            Row(
                modifier = Modifier.padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isGeneral = currentMode == CalculatorMode.GENERAL
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = if (isGeneral) colors.keyOperatorTop else colors.lcdBezelInner,
                    border = if (isGeneral) androidx.compose.foundation.BorderStroke(0.8.dp, colors.keyOperatorRim) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .clickable { onSwitchMode(CalculatorMode.GENERAL) }
                        .testTag("tab_general")
                ) {
                    Text(
                        text = strings.modeGeneral,
                        fontSize = 9.sp,
                        fontWeight = if (isGeneral) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.2.sp,
                        color = if (isGeneral) Color.White else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                val isScientific = currentMode == CalculatorMode.SCIENTIFIC
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = if (isScientific) colors.keyOperatorTop else colors.lcdBezelInner,
                    border = if (isScientific) androidx.compose.foundation.BorderStroke(0.8.dp, colors.keyOperatorRim) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .clickable { onSwitchMode(CalculatorMode.SCIENTIFIC) }
                        .testTag("tab_scientific")
                ) {
                    Text(
                        text = strings.modeScientific,
                        fontSize = 10.sp,
                        fontWeight = if (isScientific) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.4.sp,
                        color = if (isScientific) Color.White else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Precision Machined Tool Buttons & Action Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Math Assistant Button
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = colors.keyOperatorTop,
                border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.keyOperatorRim),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onOpenGeminiAssistant)
                    .testTag("btn_topbar_gemini_assistant")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Math Assistant",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = strings.mathAiButton,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }

            // 3-Dot Overflow Menu for About, History, Day/Night Theme, Haptics, and Language (Bangla & English)
            var showOverflowMenu by remember { mutableStateOf(false) }

            Box {
                BadgedBox(
                    badge = {
                        if (historyCount > 0) {
                            Badge(
                                containerColor = colors.keyClearTop,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (historyCount > 99) "99+" else historyCount.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = { showOverflowMenu = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_overflow_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = strings.moreOptions,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    // 1. History
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = strings.menuHistory,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                                if (historyCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.keyClearTop
                                    ) {
                                        Text(
                                            text = "$historyCount",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = colors.textPrimary
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onOpenHistory()
                        },
                        modifier = Modifier.testTag("menu_item_history")
                    )

                    // 2. Day / Night (Theme)
                    val themeLabel = when (themeMode) {
                        ThemeMode.SILVER_ALUMINUM -> strings.menuThemeSilver
                        ThemeMode.DARK_PLATINUM -> strings.menuThemeDark
                        ThemeMode.SYSTEM -> strings.menuThemeAuto
                    }
                    val themeIcon = when (themeMode) {
                        ThemeMode.SILVER_ALUMINUM -> Icons.Default.LightMode
                        ThemeMode.DARK_PLATINUM -> Icons.Default.DarkMode
                        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = themeLabel,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = themeIcon,
                                contentDescription = null,
                                tint = colors.secondaryLabelGold
                            )
                        },
                        onClick = {
                            onCycleTheme()
                        },
                        modifier = Modifier.testTag("menu_item_theme")
                    )

                    // 3. Haptics Vibration Toggle
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isHapticEnabled) strings.menuHapticsOn else strings.menuHapticsOff,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isHapticEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = if (isHapticEnabled) colors.secondaryLabelGold else colors.textSecondary
                            )
                        },
                        onClick = {
                            onToggleHaptics()
                        },
                        modifier = Modifier.testTag("menu_item_haptics")
                    )

                    HorizontalDivider(color = colors.surfaceVariant, thickness = 0.8.dp)

                    // 4. Two Language Options in 3-Dot: Bangla and English
                    // Language Option A: English
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.menuLangEnglish,
                                    fontWeight = if (currentLanguage == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Medium,
                                    color = if (currentLanguage == AppLanguage.ENGLISH) colors.secondaryLabelGold else colors.textPrimary
                                )
                                if (currentLanguage == AppLanguage.ENGLISH) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = colors.secondaryLabelGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = if (currentLanguage == AppLanguage.ENGLISH) colors.secondaryLabelGold else colors.textSecondary
                            )
                        },
                        onClick = {
                            onSelectLanguage(AppLanguage.ENGLISH)
                        },
                        modifier = Modifier.testTag("menu_item_lang_en")
                    )

                    // Language Option B: Bangla (বাংলা)
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.menuLangBangla,
                                    fontWeight = if (currentLanguage == AppLanguage.BANGLA) FontWeight.Bold else FontWeight.Medium,
                                    color = if (currentLanguage == AppLanguage.BANGLA) colors.secondaryLabelGold else colors.textPrimary
                                )
                                if (currentLanguage == AppLanguage.BANGLA) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = colors.secondaryLabelGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = if (currentLanguage == AppLanguage.BANGLA) colors.secondaryLabelGold else colors.textSecondary
                            )
                        },
                        onClick = {
                            onSelectLanguage(AppLanguage.BANGLA)
                        },
                        modifier = Modifier.testTag("menu_item_lang_bn")
                    )

                    HorizontalDivider(color = colors.surfaceVariant, thickness = 0.8.dp)

                    // 5. Settings
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = strings.menuSettings,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = colors.buttonOperator
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onOpenSettings()
                        },
                        modifier = Modifier.testTag("menu_item_settings")
                    )

                    // 6. About (with Made by Hujayfa & v1.0.6)
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = strings.menuAbout,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = strings.menuAboutSub,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = colors.buttonOperator
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onOpenAbout()
                        },
                        modifier = Modifier.testTag("menu_item_about")
                    )
                }
            }
        }
    }
}
