package com.example.ui.calculator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.LocalLabColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabCalculatorScreen(
    viewModel: CalculatorViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val copiedMsg = stringResource(R.string.copied_to_clipboard)
    val savedMsg = stringResource(R.string.saved_to_history)
    val clearedMsg = stringResource(R.string.history_cleared)

    // Handle system back press when history sheet is visible
    BackHandler(enabled = uiState.isHistoryVisible) {
        viewModel.setHistoryVisible(false)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isExpandedLayout = maxWidth >= 820.dp
            val isCompactHeight = maxHeight < 700.dp

            if (isExpandedLayout) {
                // Canonical Supporting-Pane Layout on Tablets / Foldables
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    CalculatorWorkspace(
                        uiState = uiState,
                        showHistoryButton = false,
                        compactHeight = isCompactHeight,
                        viewModel = viewModel,
                        onCopyResult = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            coroutineScope.launch { snackbarHostState.showSnackbar(copiedMsg) }
                        },
                        onPercentSaved = {
                            coroutineScope.launch { snackbarHostState.showSnackbar(savedMsg) }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                    )

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .weight(0.95f)
                            .fillMaxHeight()
                    ) {
                        HistoryPanelContent(
                            history = uiState.history,
                            showCloseButton = false,
                            onClose = {},
                            onRequestClearAll = { viewModel.requestClearHistoryDialog(true) },
                            onRecallResult = viewModel::recallHistoryResult,
                            onRecallExpression = viewModel::recallHistoryExpression,
                            onDeleteEntry = viewModel::deleteHistoryEntry
                        )
                    }
                }
            } else {
                // Compact & Medium Handheld Layout (centered max-width for medium screens)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CalculatorWorkspace(
                        uiState = uiState,
                        showHistoryButton = true,
                        compactHeight = isCompactHeight,
                        viewModel = viewModel,
                        onCopyResult = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            coroutineScope.launch { snackbarHostState.showSnackbar(copiedMsg) }
                        },
                        onPercentSaved = {
                            coroutineScope.launch { snackbarHostState.showSnackbar(savedMsg) }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 560.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (uiState.isHistoryVisible) {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.setHistoryVisible(false) },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    ) {
                        HistoryPanelContent(
                            history = uiState.history,
                            showCloseButton = true,
                            onClose = { viewModel.setHistoryVisible(false) },
                            onRequestClearAll = { viewModel.requestClearHistoryDialog(true) },
                            onRecallResult = viewModel::recallHistoryResult,
                            onRecallExpression = viewModel::recallHistoryExpression,
                            onDeleteEntry = viewModel::deleteHistoryEntry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 380.dp, max = 620.dp)
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }

    if (uiState.showClearHistoryDialog) {
        ClearHistoryConfirmationDialog(
            onConfirm = {
                viewModel.confirmClearHistory()
                coroutineScope.launch { snackbarHostState.showSnackbar(clearedMsg) }
            },
            onDismiss = { viewModel.requestClearHistoryDialog(false) }
        )
    }
}

@Composable
private fun CalculatorWorkspace(
    uiState: CalculatorUiState,
    showHistoryButton: Boolean,
    compactHeight: Boolean,
    viewModel: CalculatorViewModel,
    onCopyResult: (String) -> Unit,
    onPercentSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (compactHeight) 10.dp else 14.dp)
    ) {
        LabTopHeader(
            isDarkMode = uiState.isDarkMode,
            historyCount = uiState.history.size,
            showHistoryButton = showHistoryButton,
            onOpenHistory = { viewModel.setHistoryVisible(true) },
            onToggleTheme = viewModel::toggleDarkMode
        )

        ModeSelectorBar(
            selectedMode = uiState.mode,
            onSelectMode = viewModel::selectMode
        )

        if (uiState.mode == CalculatorMode.PERCENT) {
            PercentCalculatorPanel(
                state = uiState.percentState,
                onSelectType = viewModel::selectPercentType,
                onInputAChange = viewModel::updatePercentInputA,
                onInputBChange = viewModel::updatePercentInputB,
                onSaveToHistory = {
                    if (viewModel.savePercentToHistory()) {
                        onPercentSaved()
                    }
                },
                onSendToCalculator = viewModel::sendPercentResultToCalculator,
                onReset = viewModel::resetPercentInputs,
                modifier = Modifier.weight(1f)
            )
        } else {
            DisplayConsole(
                uiState = uiState,
                onToggleAngleUnit = viewModel::toggleAngleUnit,
                onCopyResult = onCopyResult,
                modifier = Modifier.weight(1f)
            )

            CalculatorKeypadSection(
                isScientificMode = uiState.mode == CalculatorMode.SCIENTIFIC,
                isDegreeMode = uiState.isDegreeMode,
                compactHeight = compactHeight,
                onDigit = viewModel::onDigit,
                onDecimal = viewModel::onDecimalPoint,
                onOperator = viewModel::onOperator,
                onPostfix = viewModel::onPostfix,
                onFunction = viewModel::onFunction,
                onConstant = viewModel::onConstant,
                onParenthesis = viewModel::onParenthesis,
                onToggleSign = viewModel::onToggleSign,
                onToggleAngleUnit = viewModel::toggleAngleUnit,
                onInsertAns = viewModel::onInsertAns,
                onDelete = viewModel::onDelete,
                onClearAll = viewModel::onClearAll,
                onEvaluate = viewModel::onEvaluate
            )
        }
    }
}

@Composable
private fun LabTopHeader(
    isDarkMode: Boolean,
    historyCount: Int,
    showHistoryButton: Boolean,
    onOpenHistory: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val labColors = LocalLabColors.current
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = labColors.keyOperatorContainer,
                border = BorderStroke(1.dp, labColors.keyOperatorBorder),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.app_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = primary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (showHistoryButton) {
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("history_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (historyCount > 0) {
                                Badge(
                                    containerColor = primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = if (historyCount > 99) "99+" else historyCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = stringResource(R.string.cd_toggle_history),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = stringResource(R.string.cd_toggle_theme),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun ModeSelectorBar(
    selectedMode: CalculatorMode,
    onSelectMode: (CalculatorMode) -> Unit
) {
    val labColors = LocalLabColors.current
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val modes = listOf(
                Triple(CalculatorMode.BASIC, stringResource(R.string.mode_basic), "mode_tab_basic"),
                Triple(CalculatorMode.SCIENTIFIC, stringResource(R.string.mode_scientific), "mode_tab_scientific"),
                Triple(CalculatorMode.PERCENT, stringResource(R.string.mode_percent), "mode_tab_percent")
            )
            modes.forEach { (mode, label, tag) ->
                val isSelected = selectedMode == mode
                Surface(
                    onClick = { onSelectMode(mode) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) primary else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .height(48.dp)
                        .testTag(tag)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else labColors.mutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
