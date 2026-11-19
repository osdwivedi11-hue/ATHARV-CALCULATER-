package com.example.ui.calculator

import com.example.data.local.CalculationEntity
import com.example.domain.engine.MathError

enum class CalculatorMode(val storageKey: String) {
    BASIC("BASIC"),
    SCIENTIFIC("SCIENTIFIC"),
    PERCENT("PERCENT");

    companion object {
        fun fromKey(key: String): CalculatorMode =
            entries.firstOrNull { it.storageKey == key } ?: BASIC
    }
}

enum class PercentCalcType {
    PERCENT_OF,
    WHAT_PERCENT,
    PERCENT_CHANGE
}

data class PercentCalculatorState(
    val type: PercentCalcType = PercentCalcType.PERCENT_OF,
    val inputA: String = "",
    val inputB: String = "",
    val resultFormatted: String? = null,
    val resultNumeric: String? = null,
    val formulaSummary: String? = null,
    val error: MathError? = null
)

data class CalculatorUiState(
    val expression: String = "",
    val lastEvaluatedExpression: String = "",
    val livePreview: String = "",
    val displayResult: String = "0",
    val lastAnswer: String = "0",
    val justEvaluated: Boolean = false,
    val error: MathError? = null,
    val mode: CalculatorMode = CalculatorMode.BASIC,
    val isDarkMode: Boolean = true,
    val isDegreeMode: Boolean = true,
    val isHistoryVisible: Boolean = false,
    val showClearHistoryDialog: Boolean = false,
    val history: List<CalculationEntity> = emptyList(),
    val percentState: PercentCalculatorState = PercentCalculatorState()
)
