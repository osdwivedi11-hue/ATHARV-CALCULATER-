package com.example.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalLabColors
import com.example.ui.theme.SpaceGroteskFontFamily

enum class KeyTone {
    DIGIT,
    OPERATOR,
    SCIENTIFIC,
    DESTRUCTIVE,
    EQUALS
}

@Composable
fun CalculatorKeypadSection(
    isScientificMode: Boolean,
    isDegreeMode: Boolean,
    compactHeight: Boolean,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onPostfix: (String) -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onParenthesis: (String) -> Unit,
    onToggleSign: () -> Unit,
    onToggleAngleUnit: () -> Unit,
    onInsertAns: () -> Unit,
    onDelete: () -> Unit,
    onClearAll: () -> Unit,
    onEvaluate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = if (compactHeight) 8.dp else 10.dp
    val basicKeyHeight = when {
        isScientificMode && compactHeight -> 50.dp
        isScientificMode -> 56.dp
        compactHeight -> 58.dp
        else -> 66.dp
    }
    val sciKeyHeight = if (compactHeight) 44.dp else 48.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Scientific expansion bank with smooth spring transition
        AnimatedVisibility(
            visible = isScientificMode,
            enter = expandVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)) + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: sin, cos, tan, log, ln
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabKeyButton(
                        label = "sin",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 15.sp,
                        testTag = "key_sin",
                        onClick = { onFunction("sin(") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = "cos",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 15.sp,
                        testTag = "key_cos",
                        onClick = { onFunction("cos(") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = "tan",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 15.sp,
                        testTag = "key_tan",
                        onClick = { onFunction("tan(") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = "log",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 15.sp,
                        testTag = "key_log",
                        onClick = { onFunction("log(") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = "ln",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 15.sp,
                        testTag = "key_ln",
                        onClick = { onFunction("ln(") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: x², √, xʸ, (, )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabKeyButton(
                        label = "x²",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 16.sp,
                        testTag = "key_square",
                        onClick = { onPostfix("²") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = "√",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 18.sp,
                        testTag = "key_sqrt",
                        onClick = { onFunction("√(") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = "xʸ",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 16.sp,
                        testTag = "key_power",
                        onClick = { onOperator("^") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = "(",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 17.sp,
                        testTag = "key_lparen",
                        onClick = { onParenthesis("(") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = ")",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 17.sp,
                        testTag = "key_rparen",
                        onClick = { onParenthesis(")") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: DEG/RAD, π, e, ANS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabKeyButton(
                        label = if (isDegreeMode) stringResource(R.string.angle_deg) else stringResource(R.string.angle_rad),
                        tone = KeyTone.OPERATOR,
                        height = sciKeyHeight,
                        fontSize = 13.sp,
                        testTag = "key_angle_toggle",
                        accessibilityLabel = stringResource(R.string.cd_toggle_angle_unit),
                        onClick = onToggleAngleUnit,
                        modifier = Modifier.weight(1.25f)
                    )
                    LabKeyButton(
                        label = "π",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 17.sp,
                        testTag = "key_pi",
                        onClick = { onConstant("π") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = "e",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 17.sp,
                        testTag = "key_euler",
                        onClick = { onConstant("e") },
                        modifier = Modifier.weight(1f)
                    )
                    LabKeyButton(
                        label = "ANS",
                        tone = KeyTone.SCIENTIFIC,
                        height = sciKeyHeight,
                        fontSize = 13.sp,
                        testTag = "key_ans",
                        onClick = onInsertAns,
                        modifier = Modifier.weight(1.25f)
                    )
                }
            }
        }

        // Standard 4-column Laboratory Keypad
        // Row 1: AC, ⌫, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            LabKeyButton(
                label = "AC",
                tone = KeyTone.DESTRUCTIVE,
                height = basicKeyHeight,
                fontSize = 19.sp,
                testTag = "key_clear_all",
                accessibilityLabel = stringResource(R.string.cd_clear_all),
                onClick = onClearAll,
                modifier = Modifier.weight(1f)
            )
            LabKeyButton(
                icon = Icons.AutoMirrored.Outlined.Backspace,
                tone = KeyTone.DESTRUCTIVE,
                height = basicKeyHeight,
                testTag = "key_delete",
                accessibilityLabel = stringResource(R.string.cd_delete_char),
                onClick = onDelete,
                modifier = Modifier.weight(1f)
            )
            LabKeyButton(
                label = "%",
                tone = KeyTone.OPERATOR,
                height = basicKeyHeight,
                fontSize = 22.sp,
                testTag = "key_percent",
                onClick = { onPostfix("%") },
                modifier = Modifier.weight(1f)
            )
            LabKeyButton(
                label = "÷",
                tone = KeyTone.OPERATOR,
                height = basicKeyHeight,
                fontSize = 26.sp,
                testTag = "key_divide",
                onClick = { onOperator("÷") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            LabKeyButton("7", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_7", onClick = { onDigit("7") }, modifier = Modifier.weight(1f))
            LabKeyButton("8", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_8", onClick = { onDigit("8") }, modifier = Modifier.weight(1f))
            LabKeyButton("9", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_9", onClick = { onDigit("9") }, modifier = Modifier.weight(1f))
            LabKeyButton("×", KeyTone.OPERATOR, basicKeyHeight, 26.sp, "key_multiply", onClick = { onOperator("×") }, modifier = Modifier.weight(1f))
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            LabKeyButton("4", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_4", onClick = { onDigit("4") }, modifier = Modifier.weight(1f))
            LabKeyButton("5", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_5", onClick = { onDigit("5") }, modifier = Modifier.weight(1f))
            LabKeyButton("6", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_6", onClick = { onDigit("6") }, modifier = Modifier.weight(1f))
            LabKeyButton("−", KeyTone.OPERATOR, basicKeyHeight, 26.sp, "key_subtract", onClick = { onOperator("−") }, modifier = Modifier.weight(1f))
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            LabKeyButton("1", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_1", onClick = { onDigit("1") }, modifier = Modifier.weight(1f))
            LabKeyButton("2", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_2", onClick = { onDigit("2") }, modifier = Modifier.weight(1f))
            LabKeyButton("3", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_3", onClick = { onDigit("3") }, modifier = Modifier.weight(1f))
            LabKeyButton("+", KeyTone.OPERATOR, basicKeyHeight, 26.sp, "key_add", onClick = { onOperator("+") }, modifier = Modifier.weight(1f))
        }

        // Row 5: ±, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            LabKeyButton("±", KeyTone.DIGIT, basicKeyHeight, 21.sp, "key_toggle_sign", onClick = onToggleSign, modifier = Modifier.weight(1f))
            LabKeyButton("0", KeyTone.DIGIT, basicKeyHeight, 23.sp, "key_0", onClick = { onDigit("0") }, modifier = Modifier.weight(1f))
            LabKeyButton(".", KeyTone.DIGIT, basicKeyHeight, 26.sp, "key_decimal", onClick = onDecimal, modifier = Modifier.weight(1f))
            LabKeyButton("=", KeyTone.EQUALS, basicKeyHeight, 28.sp, "key_equals", onClick = onEvaluate, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun LabKeyButton(
    label: String? = null,
    tone: KeyTone,
    height: Dp,
    fontSize: TextUnit = 22.sp,
    testTag: String,
    accessibilityLabel: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labColors = LocalLabColors.current
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 700f),
        label = "key_scale"
    )

    val containerColor: Color
    val borderColor: Color?
    val contentColor: Color

    when (tone) {
        KeyTone.DIGIT -> {
            containerColor = labColors.keyDigitContainer
            borderColor = labColors.keyDigitBorder
            contentColor = labColors.keyDigitContent
        }
        KeyTone.OPERATOR -> {
            containerColor = labColors.keyOperatorContainer
            borderColor = labColors.keyOperatorBorder
            contentColor = labColors.keyOperatorContent
        }
        KeyTone.SCIENTIFIC -> {
            containerColor = labColors.keyScientificContainer
            borderColor = labColors.keyScientificBorder
            contentColor = labColors.keyScientificContent
        }
        KeyTone.DESTRUCTIVE -> {
            containerColor = labColors.keyDestructiveContainer
            borderColor = labColors.keyDestructiveBorder
            contentColor = labColors.keyDestructiveContent
        }
        KeyTone.EQUALS -> {
            containerColor = labColors.keyEqualsContainer
            borderColor = null
            contentColor = labColors.keyEqualsContent
        }
    }

    val shape = if (tone == KeyTone.SCIENTIFIC) {
        RoundedCornerShape(14.dp)
    } else {
        RoundedCornerShape(18.dp)
    }

    Surface(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        interactionSource = interactionSource,
        shape = shape,
        color = containerColor,
        border = borderColor?.let { BorderStroke(1.dp, it) },
        tonalElevation = if (tone == KeyTone.EQUALS) 4.dp else 0.dp,
        modifier = modifier
            .heightIn(min = 48.dp)
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag(testTag)
            .semantics {
                if (accessibilityLabel != null) {
                    contentDescription = accessibilityLabel
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = accessibilityLabel,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            } else if (label != null) {
                Text(
                    text = label,
                    fontFamily = if (tone == KeyTone.DIGIT) JetBrainsMonoFontFamily else SpaceGroteskFontFamily,
                    fontWeight = if (tone == KeyTone.EQUALS || tone == KeyTone.OPERATOR) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = fontSize,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}
