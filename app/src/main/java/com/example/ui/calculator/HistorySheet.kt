package com.example.ui.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CalculationEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalLabColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryPanelContent(
    history: List<CalculationEntity>,
    showCloseButton: Boolean,
    onClose: () -> Unit,
    onRequestClearAll: () -> Unit,
    onRecallResult: (CalculationEntity) -> Unit,
    onRecallExpression: (CalculationEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val labColors = LocalLabColors.current
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = labColors.keyOperatorContainer,
                        border = BorderStroke(1.dp, labColors.keyOperatorBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = stringResource(R.string.history_local_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (history.isEmpty()) {
                        stringResource(R.string.history_count_entries, 0)
                    } else {
                        "${stringResource(R.string.history_count_entries, history.size)} · ${stringResource(R.string.history_tap_hint)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = labColors.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = onRequestClearAll,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = labColors.keyDestructiveContent
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = stringResource(R.string.cd_clear_history),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.history_clear_action))
                    }
                }

                if (showCloseButton) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.cd_close_history),
                            tint = labColors.mutedText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            EmptyHistoryState(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("history_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    items = history,
                    key = { it.id }
                ) { entry ->
                    HistoryItemCard(
                        entry = entry,
                        onRecallResult = { onRecallResult(entry) },
                        onRecallExpression = { onRecallExpression(entry) },
                        onDelete = { onDeleteEntry(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    entry: CalculationEntity,
    onRecallResult: () -> Unit,
    onRecallExpression: () -> Unit,
    onDelete: () -> Unit
) {
    val labColors = LocalLabColors.current
    val primary = MaterialTheme.colorScheme.primary
    val formattedTime = remember(entry.timestamp) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRecallResult)
            .testTag("history_item_${entry.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = labColors.keyScientificContainer,
                        border = BorderStroke(1.dp, labColors.keyScientificBorder)
                    ) {
                        Text(
                            text = when (entry.mode) {
                                "SCIENTIFIC" -> "SCI · ${entry.angleUnit}"
                                "PERCENT" -> "PERCENT"
                                else -> "BASIC"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = labColors.keyScientificContent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = labColors.mutedText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (entry.mode != "PERCENT") {
                        IconButton(
                            onClick = onRecallExpression,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("history_edit_${entry.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EditNote,
                                contentDescription = stringResource(R.string.cd_reuse_expression),
                                tint = labColors.mutedText,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("history_delete_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete_history_item),
                            tint = labColors.keyDestructiveContent.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = entry.expression,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily
                ),
                color = labColors.mutedText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "= ${entry.result}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    val labColors = LocalLabColors.current
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("history_empty_state"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = labColors.keyOperatorContainer,
                border = BorderStroke(1.dp, labColors.keyOperatorBorder),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.history_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.history_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = labColors.mutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ClearHistoryConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val labColors = LocalLabColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(R.string.dialog_clear_history_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_clear_history_message),
                style = MaterialTheme.typography.bodyMedium,
                color = labColors.mutedText
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = labColors.keyDestructiveContent
                ),
                modifier = Modifier.testTag("confirm_clear_history_button")
            ) {
                Text(
                    text = stringResource(R.string.dialog_confirm_clear),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_clear_history_button")
            ) {
                Text(text = stringResource(R.string.dialog_cancel))
            }
        }
    )
}
