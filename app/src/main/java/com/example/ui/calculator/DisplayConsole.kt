package com.example.ui.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.engine.MathError
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalLabColors

@Composable
fun DisplayConsole(
    uiState: CalculatorUiState,
    onToggleAngleUnit: () -> Unit,
    onCopyResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val labColors = LocalLabColors.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val expressionScroll = rememberScrollState()

    LaunchedEffect(uiState.expression) {
        expressionScroll.animateScrollTo(expressionScroll.maxValue)
    }

    val activeDisplayText = when {
        uiState.justEvaluated -> uiState.displayResult
        uiState.expression.isNotEmpty() -> uiState.expression
        else -> "0"
    }

    val mainFontSize = when {
        activeDisplayText.length <= 10 -> 42.sp
        activeDisplayText.length <= 15 -> 32.sp
        activeDisplayText.length <= 22 -> 25.sp
        else -> 21.sp
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("display_console"),
        shape = RoundedCornerShape(24.dp),
        color = labColors.displayBackground,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.38f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                )
            )
        ),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Subtle precision oscilloscope dot-matrix grid
                    val stepPx = 18.dp.toPx()
                    val dotRadius = 1.dp.toPx()
                    val dotColor = labColors.displayGridDot.copy(alpha = 0.55f)
                    var x = stepPx
                    while (x < size.width) {
                        var y = stepPx
                        while (y < size.height) {
                            drawCircle(
                                color = dotColor,
                                radius = dotRadius,
                                center = Offset(x, y)
                            )
                            y += stepPx
                        }
                        x += stepPx
                    }

                    // Subtle top-right teal laboratory instrument glow
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.11f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.88f, size.height * 0.12f),
                            radius = size.width * 0.65f
                        )
                    )
                }
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Status indicator bar inside console
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (uiState.error != null) errorColor else primaryColor,
                            modifier = Modifier.size(7.dp)
                        ) {}

                        Text(
                            text = if (uiState.mode == CalculatorMode.SCIENTIFIC) {
                                stringResource(R.string.mode_scientific).uppercase()
                            } else {
                                stringResource(R.string.mode_basic).uppercase()
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = labColors.mutedText
                        )

                        if (uiState.mode == CalculatorMode.SCIENTIFIC) {
                            Surface(
                                onClick = onToggleAngleUnit,
                                shape = RoundedCornerShape(8.dp),
                                color = labColors.keyOperatorContainer,
                                border = BorderStroke(1.dp, labColors.keyOperatorBorder),
                                modifier = Modifier.testTag("angle_unit_pill")
                            ) {
                                Text(
                                    text = if (uiState.isDegreeMode) {
                                        stringResource(R.string.angle_deg)
                                    } else {
                                        stringResource(R.string.angle_rad)
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = primaryColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            val textToCopy = if (uiState.livePreview.isNotEmpty() && !uiState.justEvaluated) {
                                uiState.livePreview
                            } else {
                                activeDisplayText
                            }
                            onCopyResult(textToCopy)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("copy_result_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = stringResource(R.string.cd_copy_result),
                            tint = labColors.mutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Previous evaluated expression trail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 22.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (uiState.lastEvaluatedExpression.isNotEmpty()) {
                        Text(
                            text = "${uiState.lastEvaluatedExpression} =",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = labColors.mutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("previous_expression_text")
                        )
                    }
                }

                // Primary expression / result readout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(expressionScroll)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val annotatedExpression = buildHighlightedExpression(
                        text = activeDisplayText,
                        defaultColor = MaterialTheme.colorScheme.onBackground,
                        operatorColor = primaryColor,
                        functionColor = labColors.keyScientificContent
                    )
                    Text(
                        text = annotatedExpression,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = mainFontSize,
                            lineHeight = (mainFontSize.value * 1.18f).sp
                        ),
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("main_display_text")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Live Preview or Error message line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 26.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    AnimatedContent(
                        targetState = Pair(uiState.error, uiState.livePreview),
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "preview_or_error"
                    ) { (error, preview) ->
                        when {
                            error != null -> {
                                Text(
                                    text = mathErrorText(error),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    color = errorColor,
                                    modifier = Modifier.testTag("display_error_text")
                                )
                            }
                            preview.isNotEmpty() -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "= ",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily
                                        ),
                                        color = primaryColor.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = preview,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = JetBrainsMonoFontFamily
                                        ),
                                        color = primaryColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.testTag("live_preview_text")
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = if (uiState.lastAnswer != "0") "ANS ${uiState.lastAnswer}" else stringResource(R.string.display_ready),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = labColors.mutedText.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun mathErrorText(error: MathError): String {
    return when (error) {
        MathError.DIVIDE_BY_ZERO -> stringResource(R.string.error_divide_by_zero)
        MathError.DOMAIN -> stringResource(R.string.error_domain)
        MathError.OVERFLOW -> stringResource(R.string.error_overflow)
        MathError.SYNTAX -> stringResource(R.string.error_syntax)
    }
}

private fun buildHighlightedExpression(
    text: String,
    defaultColor: Color,
    operatorColor: Color,
    functionColor: Color
) = buildAnnotatedString {
    val operators = setOf('+', '−', '-', '×', '÷', '^', '%', '²')
    var i = 0
    while (i < text.length) {
        val ch = text[i]
        when {
            ch in operators -> {
                withStyle(SpanStyle(color = operatorColor)) {
                    append(ch)
                }
                i++
            }
            ch == '√' || ch == 'π' || ch == '(' || ch == ')' || ch.isLetter() -> {
                withStyle(SpanStyle(color = functionColor)) {
                    append(ch)
                }
                i++
            }
            else -> {
                withStyle(SpanStyle(color = defaultColor)) {
                    append(ch)
                }
                i++
            }
        }
    }
}
