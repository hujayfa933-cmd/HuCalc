package com.example.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.engine.AngleMode
import com.example.calculator.ui.localization.AppLanguage
import com.example.calculator.ui.localization.LocalHuCalcStrings
import com.example.ui.theme.LocalCalculatorColors
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsDialog(
    themeMode: ThemeMode,
    isHapticEnabled: Boolean,
    currentLanguage: AppLanguage,
    angleMode: AngleMode,
    historyCount: Int,
    onSelectTheme: (ThemeMode) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    val strings = LocalHuCalcStrings.current
    var showConfirmClearHistory by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.testTag("settings_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.buttonOperator.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = colors.buttonOperator,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = strings.settingsTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = strings.settingsSubtitle,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Appearance / Theme
                SettingsSectionHeader(title = strings.sectionTheme)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.lcdBezelInner.copy(alpha = 0.4f))
                        .border(0.8.dp, colors.chassisHighlight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(vertical = 4.dp)
                ) {
                    ThemeOptionRow(
                        title = strings.themeSilverTitle,
                        subtitle = strings.themeSilverDesc,
                        icon = Icons.Default.LightMode,
                        isSelected = themeMode == ThemeMode.SILVER_ALUMINUM,
                        onClick = { onSelectTheme(ThemeMode.SILVER_ALUMINUM) }
                    )
                    HorizontalDivider(color = colors.chassisHighlight.copy(alpha = 0.3f), thickness = 0.6.dp)
                    ThemeOptionRow(
                        title = strings.themeDarkTitle,
                        subtitle = strings.themeDarkDesc,
                        icon = Icons.Default.DarkMode,
                        isSelected = themeMode == ThemeMode.DARK_PLATINUM,
                        onClick = { onSelectTheme(ThemeMode.DARK_PLATINUM) }
                    )
                    HorizontalDivider(color = colors.chassisHighlight.copy(alpha = 0.3f), thickness = 0.6.dp)
                    ThemeOptionRow(
                        title = strings.themeSystemTitle,
                        subtitle = strings.themeSystemDesc,
                        icon = Icons.Default.BrightnessAuto,
                        isSelected = themeMode == ThemeMode.SYSTEM,
                        onClick = { onSelectTheme(ThemeMode.SYSTEM) }
                    )
                }

                // Section 2: App Language (English & Bangla)
                SettingsSectionHeader(title = strings.sectionLanguage)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.lcdBezelInner.copy(alpha = 0.4f))
                        .border(0.8.dp, colors.chassisHighlight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(vertical = 4.dp)
                ) {
                    ThemeOptionRow(
                        title = strings.langEnglishTitle,
                        subtitle = strings.langEnglishDesc,
                        icon = Icons.Default.Language,
                        isSelected = currentLanguage == AppLanguage.ENGLISH,
                        onClick = { onSelectLanguage(AppLanguage.ENGLISH) }
                    )
                    HorizontalDivider(color = colors.chassisHighlight.copy(alpha = 0.3f), thickness = 0.6.dp)
                    ThemeOptionRow(
                        title = strings.langBanglaTitle,
                        subtitle = strings.langBanglaDesc,
                        icon = Icons.Default.Translate,
                        isSelected = currentLanguage == AppLanguage.BANGLA,
                        onClick = { onSelectLanguage(AppLanguage.BANGLA) }
                    )
                }

                // Section 3: Tactile Feedback
                SettingsSectionHeader(title = strings.sectionHaptics)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.lcdBezelInner.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.chassisHighlight.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleHaptics() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isHapticEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = if (isHapticEnabled) colors.secondaryLabelGold else colors.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = strings.hapticsTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (isHapticEnabled) strings.hapticsDescOn else strings.hapticsDescOff,
                                    fontSize = 11.5.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Switch(
                            checked = isHapticEnabled,
                            onCheckedChange = { onToggleHaptics() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.buttonOperator,
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.chassisMid
                            )
                        )
                    }
                }

                // Section 4: Calculation Defaults
                SettingsSectionHeader(title = strings.sectionDefaults)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.lcdBezelInner.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.chassisHighlight.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleAngleMode() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = null,
                                tint = colors.buttonOperator,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = strings.angleTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (angleMode == AngleMode.DEG) strings.angleDescDeg else strings.angleDescRad,
                                    fontSize = 11.5.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.keyOperatorTop
                        ) {
                            Text(
                                text = angleMode.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Section 5: History Management
                SettingsSectionHeader(title = strings.sectionHistory)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.lcdBezelInner.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.chassisHighlight.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = strings.clearHistoryTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (historyCount == 0) strings.historyEmptyCount else strings.historyItemsCount(historyCount),
                                    fontSize = 11.5.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        if (historyCount > 0) {
                            TextButton(
                                onClick = { showConfirmClearHistory = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = colors.keyClearTop)
                            ) {
                                Text(strings.btnClear, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Section 6: App Information & Credits
                SettingsSectionHeader(title = strings.menuAbout)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.lcdBezelInner.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.chassisHighlight.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HuCalc",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "v1.0.6",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.buttonOperator
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (strings.isBangla) "নির্মাতা" else "Developer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Made by Hujayfa",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.secondaryLabelGold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (strings.isBangla) "ইঞ্জিন" else "Engine",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = strings.offlineBadge,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.buttonOperator),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_close_settings")
            ) {
                Text(text = strings.btnDone, color = colors.buttonOperatorText, fontWeight = FontWeight.Bold)
            }
        }
    )

    // Confirm Clear History Dialog
    if (showConfirmClearHistory) {
        AlertDialog(
            onDismissRequest = { showConfirmClearHistory = false },
            title = { Text(strings.confirmClearHistoryTitle) },
            text = { Text(strings.confirmClearHistoryMsg) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showConfirmClearHistory = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.keyClearTop)
                ) {
                    Text(strings.btnClear, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearHistory = false }) {
                    Text(strings.btnCancel)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val colors = LocalCalculatorColors.current
    Text(
        text = title,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = colors.secondaryLabelGold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 2.dp, top = 2.dp)
    )
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalCalculatorColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.buttonOperator else colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.buttonOperator,
                unselectedColor = colors.textSecondary.copy(alpha = 0.6f)
            )
        )
    }
}
