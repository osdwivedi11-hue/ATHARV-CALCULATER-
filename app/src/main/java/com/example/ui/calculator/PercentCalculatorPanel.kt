package com.example.ui.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalLabColors

@Composable
fun PercentCalculatorPanel(
    state: PercentCalculatorState,
    onSelectType: (PercentCalcType) -> Unit,
    onInputAChange: (String) -> Unit,
    onInputBChange: (String) -> Unit,
    onSaveToHistory: () -> Unit,
    onSendToCalculator: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labColors = LocalLabColors.current
    val primary = MaterialTheme.colorScheme.primary

    val (labelA, labelB) = when (state.type) {
        PercentCalcType.PERCENT_OF -> Pair(
            stringResource(R.string.percent_label_percentage),
            stringResource(R.string.percent_label_base_value)
        )
        PercentCalcType.WHAT_PERCENT -> Pair(
            stringResource(R.string.percent_label_part_value),
            stringResource(R.string.percent_label_whole_value)
        )
        PercentCalcType.PERCENT_CHANGE -> Pair(
            stringResource(R.string.percent_label_initial_value),
            stringResource(R.string.percent_label_final_value)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-mode segmented chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PercentModeChip(
                label = stringResource(R.string.percent_mode_of),
                selected = state.type == PercentCalcType.PERCENT_OF,
                testTag = "percent_mode_of",
                onClick = { onSelectType(PercentCalcType.PERCENT_OF) },
                modifier = Modifier.weight(1f)
            )
            PercentModeChip(
                label = stringResource(R.string.percent_mode_what),
                selected = state.type == PercentCalcType.WHAT_PERCENT,
                testTag = "percent_mode_what",
                onClick = { onSelectType(PercentCalcType.WHAT_PERCENT) },
                modifier = Modifier.weight(1f)
            )
            PercentModeChip(
                label = stringResource(R.string.percent_mode_change),
                selected = state.type == PercentCalcType.PERCENT_CHANGE,
                testTag = "percent_mode_change",
                onClick = { onSelectType(PercentCalcType.PERCENT_CHANGE) },
                modifier = Modifier.weight(1f)
            )
        }

        // Result Readout Console Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("percent_result_card"),
            shape = RoundedCornerShape(22.dp),
            color = labColors.displayBackground,
            border = BorderStroke(1.dp, primary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Percent,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.percent_result_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = labColors.mutedText
                    )
                }

                when {
                    state.error != null -> {
                        Text(
                            text = mathErrorText(state.error),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("percent_error_text")
                        )
                    }
                    state.resultFormatted != null -> {
                        Text(
                            text = state.resultFormatted,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            ),
                            color = primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("percent_result_value")
                        )
                        if (state.formulaSummary != null) {
                            Text(
                                text = state.formulaSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = labColors.mutedText
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.percent_hint_enter_values),
                            style = MaterialTheme.typography.bodyMedium,
                            color = labColors.mutedText
                        )
                    }
                }
            }
        }

        // Numeric Input Fields
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = state.inputA,
                    onValueChange = onInputAChange,
                    label = { Text(labelA) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = primary,
                        cursorColor = primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("percent_input_a")
                )

                OutlinedTextField(
                    value = state.inputB,
                    onValueChange = onInputBChange,
                    label = { Text(labelB) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = primary,
                        cursorColor = primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("percent_input_b")
                )
            }
        }

        // Action Buttons
        val hasValidResult = state.resultFormatted != null && state.error == null
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSaveToHistory,
                enabled = hasValidResult,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .testTag("percent_save_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.percent_save_history),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .heightIn(min = 52.dp)
                    .testTag("percent_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.percent_reset),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        FilledTonalButton(
            onClick = onSendToCalculator,
            enabled = hasValidResult,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = labColors.keyOperatorContainer,
                contentColor = primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("percent_send_to_calc_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Calculate,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.percent_use_in_calc))
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PercentModeChip(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labColors = LocalLabColors.current
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) labColors.keyOperatorContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) primary else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) primary else labColors.mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
