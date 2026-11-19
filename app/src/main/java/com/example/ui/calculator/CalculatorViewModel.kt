package com.example.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CalculationEntity
import com.example.data.local.CalculationRepository
import com.example.data.local.PreferencesRepository
import com.example.domain.engine.CalculationResult
import com.example.domain.engine.CalculatorEngine
import com.example.domain.engine.MathError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class CalculatorViewModel(
    private val calculationRepository: CalculationRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val binaryOperators = setOf('+', '−', '×', '÷', '^')
    private val functionTokens = listOf("sin(", "cos(", "tan(", "log(", "ln(", "√(")

    init {
        observeHistory()
        observePreferences()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            calculationRepository.history.collect { entries ->
                _uiState.update { it.copy(history = entries) }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.update { state ->
                    val updated = state.copy(
                        isDarkMode = prefs.isDarkMode,
                        isDegreeMode = prefs.isDegreeMode,
                        mode = CalculatorMode.fromKey(prefs.lastMode)
                    )
                    updated.copy(
                        livePreview = computeLivePreview(updated.expression, updated.isDegreeMode)
                    )
                }
            }
        }
    }

    fun selectMode(mode: CalculatorMode) {
        _uiState.update { it.copy(mode = mode) }
        viewModelScope.launch {
            preferencesRepository.setLastMode(mode.storageKey)
        }
    }

    fun toggleDarkMode() {
        val next = !_uiState.value.isDarkMode
        _uiState.update { it.copy(isDarkMode = next) }
        viewModelScope.launch {
            preferencesRepository.setDarkMode(next)
        }
    }

    fun toggleAngleUnit() {
        val next = !_uiState.value.isDegreeMode
        _uiState.update { state ->
            state.copy(
                isDegreeMode = next,
                livePreview = computeLivePreview(state.expression, next)
            )
        }
        viewModelScope.launch {
            preferencesRepository.setDegreeMode(next)
        }
    }

    fun setHistoryVisible(visible: Boolean) {
        _uiState.update { it.copy(isHistoryVisible = visible) }
    }

    fun requestClearHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(showClearHistoryDialog = show) }
    }

    fun confirmClearHistory() {
        _uiState.update { it.copy(showClearHistoryDialog = false) }
        viewModelScope.launch {
            calculationRepository.clearHistory()
        }
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch {
            calculationRepository.deleteEntry(id)
        }
    }

    fun recallHistoryResult(entry: CalculationEntity) {
        val cleanResult = entry.result.trimEnd('%')
        _uiState.update { state ->
            val nextExpression = if (state.justEvaluated || state.expression.isEmpty()) {
                cleanResult
            } else {
                state.expression + cleanResult
            }
            val nextMode = if (state.mode == CalculatorMode.PERCENT) {
                CalculatorMode.BASIC
            } else {
                state.mode
            }
            state.copy(
                expression = nextExpression,
                justEvaluated = false,
                error = null,
                mode = nextMode,
                isHistoryVisible = false,
                livePreview = computeLivePreview(nextExpression, state.isDegreeMode)
            )
        }
    }

    fun recallHistoryExpression(entry: CalculationEntity) {
        val recalled = entry.expression
        val isScientificExpr = recalled.any { it in listOf('√', '^', '²', 'π') } ||
            recalled.contains("sin") || recalled.contains("cos") ||
            recalled.contains("tan") || recalled.contains("log") || recalled.contains("ln")
        val targetMode = if (isScientificExpr) CalculatorMode.SCIENTIFIC else CalculatorMode.BASIC

        _uiState.update { state ->
            state.copy(
                expression = recalled,
                lastEvaluatedExpression = "",
                justEvaluated = false,
                error = null,
                mode = targetMode,
                isHistoryVisible = false,
                livePreview = computeLivePreview(recalled, state.isDegreeMode)
            )
        }
    }

    fun onDigit(digit: String) {
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            val next = base + digit
            state.copy(
                expression = next,
                lastEvaluatedExpression = if (state.justEvaluated) "" else state.lastEvaluatedExpression,
                justEvaluated = false,
                error = null,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onDecimalPoint() {
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            val currentSegment = extractTrailingNumberSegment(base)
            if (currentSegment.contains('.')) {
                return@update state
            }
            val next = if (base.isEmpty() || !base.last().isDigit()) {
                base + "0."
            } else {
                "$base."
            }
            state.copy(
                expression = next,
                lastEvaluatedExpression = if (state.justEvaluated) "" else state.lastEvaluatedExpression,
                justEvaluated = false,
                error = null,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onOperator(op: String) {
        val opChar = op.firstOrNull() ?: return
        _uiState.update { state ->
            val base = when {
                state.justEvaluated -> state.displayResult
                state.expression.isEmpty() -> {
                    if (opChar == '−') "−" else "0$op"
                }
                else -> state.expression
            }

            val next = when {
                state.expression.isEmpty() && !state.justEvaluated -> base
                base.isNotEmpty() && base.last() in binaryOperators -> {
                    if (opChar == '−' && base.last() in setOf('×', '÷', '^')) {
                        base + op
                    } else if (base.length == 1 && base.last() == '−' && opChar != '−') {
                        base
                    } else {
                        base.dropLast(1) + op
                    }
                }
                base.endsWith("(") -> {
                    if (opChar == '−') "$base−" else base
                }
                else -> base + op
            }

            state.copy(
                expression = next,
                lastEvaluatedExpression = if (state.justEvaluated) "" else state.lastEvaluatedExpression,
                justEvaluated = false,
                error = null,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onPostfix(symbol: String) {
        _uiState.update { state ->
            val base = if (state.justEvaluated) state.displayResult else state.expression
            if (base.isEmpty()) return@update state
            val last = base.last()
            val canAttach = last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '²' || last == '%'
            if (!canAttach) return@update state

            val next = base + symbol
            state.copy(
                expression = next,
                lastEvaluatedExpression = if (state.justEvaluated) "" else state.lastEvaluatedExpression,
                justEvaluated = false,
                error = null,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onFunction(funcToken: String) {
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            val next = base + funcToken
            state.copy(
                expression = next,
                lastEvaluatedExpression = if (state.justEvaluated) "" else state.lastEvaluatedExpression,
                justEvaluated = false,
                error = null,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onConstant(symbol: String) {
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            val next = base + symbol
            state.copy(
                expression = next,
                lastEvaluatedExpression = if (state.justEvaluated) "" else state.lastEvaluatedExpression,
                justEvaluated = false,
                error = null,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onInsertAns() {
        val ans = _uiState.value.lastAnswer
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            val next = base + ans
            state.copy(
                expression = next,
                lastEvaluatedExpression = if (state.justEvaluated) "" else state.lastEvaluatedExpression,
                justEvaluated = false,
                error = null,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onParenthesis(paren: String) {
        _uiState.update { state ->
            val base = if (state.justEvaluated && paren == "(") "" else state.expression
            if (paren == ")") {
                val openCount = base.count { it == '(' }
                val closeCount = base.count { it == ')' }
                if (openCount <= closeCount || base.isEmpty() || base.endsWith("(")) {
                    return@update state
                }
            }
            val next = base + paren
            state.copy(
                expression = next,
                lastEvaluatedExpression = if (state.justEvaluated) "" else state.lastEvaluatedExpression,
                justEvaluated = false,
                error = null,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onToggleSign() {
        _uiState.update { state ->
            val current = if (state.justEvaluated) state.displayResult else state.expression
            if (current.isEmpty() || current == "0") {
                return@update state.copy(expression = "−", justEvaluated = false, error = null)
            }
            val next = when {
                current.startsWith("−") && current.drop(1).all { it.isDigit() || it == '.' } -> {
                    current.drop(1)
                }
                current.startsWith("-") && current.drop(1).all { it.isDigit() || it == '.' } -> {
                    current.drop(1)
                }
                current.all { it.isDigit() || it == '.' } -> {
                    "−$current"
                }
                current.endsWith(")") && current.startsWith("−(") -> {
                    current.removePrefix("−(").removeSuffix(")")
                }
                else -> "−($current)"
            }
            state.copy(
                expression = next,
                lastEvaluatedExpression = if (state.justEvaluated) "" else state.lastEvaluatedExpression,
                justEvaluated = false,
                error = null,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onDelete() {
        _uiState.update { state ->
            if (state.justEvaluated) {
                return@update state.copy(
                    expression = "",
                    lastEvaluatedExpression = "",
                    displayResult = "0",
                    livePreview = "",
                    justEvaluated = false,
                    error = null
                )
            }
            val current = state.expression
            if (current.isEmpty()) return@update state

            val matchedFunc = functionTokens.firstOrNull { current.endsWith(it) }
            val next = if (matchedFunc != null) {
                current.dropLast(matchedFunc.length)
            } else {
                current.dropLast(1)
            }

            state.copy(
                expression = next,
                error = null,
                displayResult = if (next.isEmpty()) "0" else state.displayResult,
                livePreview = computeLivePreview(next, state.isDegreeMode)
            )
        }
    }

    fun onClearAll() {
        _uiState.update { state ->
            state.copy(
                expression = "",
                lastEvaluatedExpression = "",
                displayResult = "0",
                livePreview = "",
                justEvaluated = false,
                error = null
            )
        }
    }

    fun onEvaluate() {
        val state = _uiState.value
        val expr = state.expression.trim()
        if (expr.isEmpty()) return

        when (val evalResult = CalculatorEngine.evaluate(expr, state.isDegreeMode)) {
            is CalculationResult.Success -> {
                val formatted = evalResult.formatted
                _uiState.update { current ->
                    current.copy(
                        lastEvaluatedExpression = expr,
                        expression = formatted,
                        displayResult = formatted,
                        lastAnswer = formatted,
                        livePreview = "",
                        justEvaluated = true,
                        error = null
                    )
                }
                val angleLabel = if (state.isDegreeMode) "DEG" else "RAD"
                viewModelScope.launch {
                    calculationRepository.saveCalculation(
                        expression = expr,
                        result = formatted,
                        mode = state.mode.storageKey,
                        angleUnit = angleLabel
                    )
                }
            }
            is CalculationResult.Error -> {
                _uiState.update { current ->
                    current.copy(error = evalResult.error)
                }
            }
        }
    }

    // --- Percentage Calculator Module ---

    fun selectPercentType(type: PercentCalcType) {
        _uiState.update { state ->
            val updated = state.percentState.copy(type = type)
            state.copy(percentState = recalculatePercent(updated))
        }
    }

    fun updatePercentInputA(value: String) {
        if (!isValidNumericInput(value)) return
        _uiState.update { state ->
            val updated = state.percentState.copy(inputA = value)
            state.copy(percentState = recalculatePercent(updated))
        }
    }

    fun updatePercentInputB(value: String) {
        if (!isValidNumericInput(value)) return
        _uiState.update { state ->
            val updated = state.percentState.copy(inputB = value)
            state.copy(percentState = recalculatePercent(updated))
        }
    }

    fun resetPercentInputs() {
        _uiState.update { state ->
            state.copy(
                percentState = PercentCalculatorState(type = state.percentState.type)
            )
        }
    }

    fun savePercentToHistory(): Boolean {
        val ps = _uiState.value.percentState
        val summary = ps.formulaSummary ?: return false
        val result = ps.resultFormatted ?: return false
        viewModelScope.launch {
            calculationRepository.saveCalculation(
                expression = summary,
                result = result,
                mode = CalculatorMode.PERCENT.storageKey,
                angleUnit = "%"
            )
        }
        return true
    }

    fun sendPercentResultToCalculator() {
        val numeric = _uiState.value.percentState.resultNumeric ?: return
        _uiState.update { state ->
            state.copy(
                mode = CalculatorMode.BASIC,
                expression = numeric,
                displayResult = numeric,
                lastAnswer = numeric,
                justEvaluated = false,
                error = null,
                livePreview = ""
            )
        }
        viewModelScope.launch {
            preferencesRepository.setLastMode(CalculatorMode.BASIC.storageKey)
        }
    }

    private fun recalculatePercent(state: PercentCalculatorState): PercentCalculatorState {
        val a = state.inputA.toDoubleOrNull()
        val b = state.inputB.toDoubleOrNull()
        if (a == null || b == null) {
            return state.copy(
                resultFormatted = null,
                resultNumeric = null,
                formulaSummary = null,
                error = null
            )
        }

        val aFmt = CalculatorEngine.formatNumber(a)
        val bFmt = CalculatorEngine.formatNumber(b)

        return when (state.type) {
            PercentCalcType.PERCENT_OF -> {
                val value = (a / 100.0) * b
                val formatted = CalculatorEngine.formatNumber(value)
                state.copy(
                    resultFormatted = formatted,
                    resultNumeric = formatted,
                    formulaSummary = "$aFmt% of $bFmt",
                    error = null
                )
            }
            PercentCalcType.WHAT_PERCENT -> {
                if (abs(b) == 0.0) {
                    state.copy(
                        resultFormatted = null,
                        resultNumeric = null,
                        formulaSummary = null,
                        error = MathError.DIVIDE_BY_ZERO
                    )
                } else {
                    val value = (a / b) * 100.0
                    val formatted = CalculatorEngine.formatNumber(value)
                    state.copy(
                        resultFormatted = "$formatted%",
                        resultNumeric = formatted,
                        formulaSummary = "$aFmt ÷ $bFmt × 100",
                        error = null
                    )
                }
            }
            PercentCalcType.PERCENT_CHANGE -> {
                if (abs(a) == 0.0) {
                    state.copy(
                        resultFormatted = null,
                        resultNumeric = null,
                        formulaSummary = null,
                        error = MathError.DIVIDE_BY_ZERO
                    )
                } else {
                    val value = ((b - a) / abs(a)) * 100.0
                    val formatted = CalculatorEngine.formatNumber(value)
                    val prefix = if (value > 0) "+" else ""
                    state.copy(
                        resultFormatted = "$prefix$formatted%",
                        resultNumeric = formatted,
                        formulaSummary = "Δ% ($aFmt → $bFmt)",
                        error = null
                    )
                }
            }
        }
    }

    private fun isValidNumericInput(text: String): Boolean {
        if (text.isEmpty()) return true
        return text.matches(Regex("^-?\\d*\\.?\\d*$")) && text.length <= 16
    }

    private fun extractTrailingNumberSegment(expression: String): String {
        var idx = expression.length - 1
        while (idx >= 0 && (expression[idx].isDigit() || expression[idx] == '.')) {
            idx--
        }
        return expression.substring(idx + 1)
    }

    private fun computeLivePreview(expression: String, isDegreeMode: Boolean): String {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return ""
        val hasCalculation = trimmed.any {
            it in binaryOperators || it == '%' || it == '²' || it == '√' || it == '(' || it == 'π' || it.isLetter()
        }
        if (!hasCalculation) return ""

        return when (val result = CalculatorEngine.evaluate(trimmed, isDegreeMode)) {
            is CalculationResult.Success -> {
                if (result.formatted == trimmed) "" else result.formatted
            }
            is CalculationResult.Error -> ""
        }
    }

    class Factory(
        private val calculationRepository: CalculationRepository,
        private val preferencesRepository: PreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
                return CalculatorViewModel(calculationRepository, preferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
