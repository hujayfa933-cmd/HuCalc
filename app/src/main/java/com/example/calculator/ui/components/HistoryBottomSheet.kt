package com.example.calculator.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.data.CalculationHistoryEntity
import com.example.calculator.ui.localization.LocalHuCalcStrings
import com.example.ui.theme.LocalCalculatorColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    historyList: List<CalculationHistoryEntity>,
    onDismiss: () -> Unit,
    onSelectItem: (CalculationHistoryEntity, Boolean) -> Unit,
    onClearAll: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val colors = LocalCalculatorColors.current
    val strings = LocalHuCalcStrings.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showConfirmClear by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterKey by remember { mutableStateOf("ALL") }

    val filteredList = remember(historyList, searchQuery, selectedFilterKey) {
        historyList.filter { item ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.expression.contains(searchQuery, ignoreCase = true) ||
                item.result.contains(searchQuery, ignoreCase = true)
            }
            val matchesFilter = when (selectedFilterKey) {
                "SCIENTIFIC" -> item.mode.equals("Scientific", ignoreCase = true)
                "GENERAL" -> item.mode.equals("General", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.textSecondary.copy(alpha = 0.5f)) },
        modifier = Modifier.testTag("history_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.historySheetTitle,
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (historyList.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.buttonOperator.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${historyList.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.buttonOperator,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (historyList.isNotEmpty()) {
                        IconButton(
                            onClick = { showConfirmClear = true },
                            modifier = Modifier.testTag("btn_clear_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = strings.clearHistoryTitle,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            // Search Bar & Filter Chips (v1.0.6 Major Feature)
            if (historyList.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(strings.historySearchPlaceholder, fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.buttonOperator,
                        unfocusedBorderColor = colors.chassisHighlight,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .testTag("history_search_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to strings.historyTabAll,
                        "SCIENTIFIC" to strings.historyTabScientific,
                        "GENERAL" to strings.historyTabGeneral
                    )

                    filterOptions.forEach { (key, label) ->
                        val isSelected = selectedFilterKey == key
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) colors.buttonOperator else colors.chassisHighlight.copy(alpha = 0.4f),
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(0.6.dp, colors.chassisHighlight) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedFilterKey = key }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = colors.surfaceVariant, thickness = 1.dp)

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (historyList.isEmpty()) strings.historyEmptyState else strings.historyNoResults,
                        color = colors.textSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onSelectItem(item, true)
                                    onDismiss()
                                }
                                .testTag("history_item_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = colors.surface
                                    ) {
                                        val displayMode = if (strings.isBangla) {
                                            if (item.mode.equals("Scientific", ignoreCase = true)) strings.historyTabScientific else strings.historyTabGeneral
                                        } else {
                                            item.mode
                                        }
                                        Text(
                                            text = displayMode,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = dateFormat.format(Date(item.timestamp)),
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.expression,
                                    fontSize = 15.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(item.result))
                                                val msg = if (strings.isBangla) "ফলাফল কপি করা হয়েছে" else "Result copied"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy result",
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteItem(item.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = strings.historyDeleteTooltip,
                                                tint = colors.textSecondary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "= ${item.result}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.buttonOperator,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text(strings.confirmClearHistoryTitle) },
            text = { Text(strings.confirmClearHistoryMsg) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showConfirmClear = false
                    }
                ) {
                    Text(strings.btnClear, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text(strings.btnCancel)
                }
            }
        )
    }
}
