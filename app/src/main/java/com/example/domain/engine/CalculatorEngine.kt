package com.example.domain.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class MathError {
    DIVIDE_BY_ZERO,
    DOMAIN,
    OVERFLOW,
    SYNTAX
}

sealed interface CalculationResult {
    data class Success(val value: Double, val formatted: String) : CalculationResult
    data class Error(val error: MathError) : CalculationResult
}

object CalculatorEngine {

    private sealed interface Token {
        data class Number(val value: Double) : Token
        data class Operator(val symbol: Char) : Token // +, -, *, /, ^
        data class Function(val name: String) : Token // sin, cos, tan, log, ln, sqrt
        data object LeftParen : Token
        data object RightParen : Token
        data object Percent : Token
        data object Square : Token
    }

    fun evaluate(rawExpression: String, isDegreeMode: Boolean = true): CalculationResult {
        val cleaned = rawExpression.trim()
        if (cleaned.isEmpty()) {
            return CalculationResult.Error(MathError.SYNTAX)
        }

        val tokens = tokenize(cleaned) ?: return CalculationResult.Error(MathError.SYNTAX)
        if (tokens.isEmpty()) return CalculationResult.Error(MathError.SYNTAX)

        val balancedTokens = autoBalanceAndInsertImplicitMultiplication(tokens)
        return try {
            val parser = RecursiveDescentParser(balancedTokens, isDegreeMode)
            val result = parser.parse()
            if (result.isNaN()) {
                CalculationResult.Error(MathError.DOMAIN)
            } else if (result.isInfinite()) {
                CalculationResult.Error(MathError.OVERFLOW)
            } else {
                CalculationResult.Success(
                    value = if (result == 0.0) 0.0 else result,
                    formatted = formatNumber(result)
                )
            }
        } catch (e: EvalException) {
            CalculationResult.Error(e.error)
        } catch (_: Exception) {
            CalculationResult.Error(MathError.SYNTAX)
        }
    }

    fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val normalized = if (abs(value) < 1e-14) 0.0 else value
        if (normalized == 0.0) return "0"

        val absVal = abs(normalized)
        return try {
            val bd = BigDecimal.valueOf(normalized).round(MathContext(12, RoundingMode.HALF_UP))
            val stripped = bd.stripTrailingZeros()
            if (absVal >= 1e12 || absVal < 1e-6) {
                val sci = BigDecimal.valueOf(normalized).round(MathContext(8, RoundingMode.HALF_UP))
                    .stripTrailingZeros()
                sci.toString()
            } else {
                stripped.toPlainString()
            }
        } catch (_: Exception) {
            normalized.toString()
        }
    }

    private fun tokenize(input: String): List<Token>? {
        val tokens = mutableListOf<Token>()
        var i = 0
        val len = input.length

        while (i < len) {
            val c = input[i]
            when {
                c.isWhitespace() || c == ',' -> {
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val start = i
                    var dotCount = 0
                    while (i < len && (input[i].isDigit() || input[i] == '.')) {
                        if (input[i] == '.') dotCount++
                        if (dotCount > 1) return null
                        i++
                    }
                    // Handle scientific notation like 1.5E+10 or 2e-5
                    if (i < len && (input[i] == 'E' || input[i] == 'e')) {
                        val nextIdx = i + 1
                        if (nextIdx < len && (input[nextIdx].isDigit() || input[nextIdx] == '+' || input[nextIdx] == '-')) {
                            val expStart = i
                            i++ // consume E/e
                            if (i < len && (input[i] == '+' || input[i] == '-')) i++
                            val digitsStart = i
                            while (i < len && input[i].isDigit()) i++
                            if (i == digitsStart) {
                                // Not an exponent, rewind so 'e' is parsed as Euler's constant
                                i = expStart
                            }
                        }
                    }
                    val numStr = input.substring(start, i)
                    val value = numStr.toDoubleOrNull() ?: return null
                    tokens.add(Token.Number(value))
                }
                c == '+' -> {
                    tokens.add(Token.Operator('+'))
                    i++
                }
                c == '-' || c == '−' -> {
                    tokens.add(Token.Operator('-'))
                    i++
                }
                c == '*' || c == '×' -> {
                    tokens.add(Token.Operator('*'))
                    i++
                }
                c == '/' || c == '÷' -> {
                    tokens.add(Token.Operator('/'))
                    i++
                }
                c == '^' -> {
                    tokens.add(Token.Operator('^'))
                    i++
                }
                c == '(' -> {
                    tokens.add(Token.LeftParen)
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.RightParen)
                    i++
                }
                c == '%' -> {
                    tokens.add(Token.Percent)
                    i++
                }
                c == '²' -> {
                    tokens.add(Token.Square)
                    i++
                }
                c == '√' -> {
                    tokens.add(Token.Function("sqrt"))
                    i++
                }
                c == 'π' -> {
                    tokens.add(Token.Number(PI))
                    i++
                }
                c.isLetter() -> {
                    val start = i
                    while (i < len && input[i].isLetter()) i++
                    val word = input.substring(start, i).lowercase()
                    when (word) {
                        "sin", "cos", "tan", "log", "ln", "sqrt" -> tokens.add(Token.Function(word))
                        "pi" -> tokens.add(Token.Number(PI))
                        "e" -> tokens.add(Token.Number(E))
                        else -> return null
                    }
                }
                else -> return null
            }
        }
        return tokens
    }

    private fun autoBalanceAndInsertImplicitMultiplication(rawTokens: List<Token>): List<Token> {
        val result = mutableListOf<Token>()
        var openParens = 0

        for (token in rawTokens) {
            if (result.isNotEmpty()) {
                val prev = result.last()
                val prevEndsValue = prev is Token.Number ||
                    prev is Token.RightParen ||
                    prev is Token.Percent ||
                    prev is Token.Square
                val currStartsValue = token is Token.Number ||
                    token is Token.LeftParen ||
                    token is Token.Function

                if (prevEndsValue && currStartsValue) {
                    result.add(Token.Operator('*'))
                }
            }
            if (token is Token.LeftParen) openParens++
            if (token is Token.RightParen) openParens--
            result.add(token)
        }

        // Auto-close any trailing unclosed parentheses for ergonomic calculator use
        while (openParens > 0) {
            result.add(Token.RightParen)
            openParens--
        }
        return result
    }

    private class EvalException(val error: MathError) : RuntimeException()

    private class RecursiveDescentParser(
        private val tokens: List<Token>,
        private val isDegreeMode: Boolean
    ) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun consume(): Token = tokens[pos++]

        fun parse(): Double {
            val value = parseAddSub()
            if (pos < tokens.size) {
                throw EvalException(MathError.SYNTAX)
            }
            return value
        }

        private fun parseAddSub(): Double {
            var left = parseMulDiv().first
            while (true) {
                val next = peek()
                if (next is Token.Operator && (next.symbol == '+' || next.symbol == '-')) {
                    consume()
                    val (rightVal, isDirectPercent) = parseMulDiv()
                    val adjustedRight = if (isDirectPercent) left * rightVal else rightVal
                    left = if (next.symbol == '+') left + adjustedRight else left - adjustedRight
                } else {
                    break
                }
            }
            return left
        }

        // Returns (value, isSingleDirectPercentTerm) so A + B% evaluates as A + (A * B / 100)
        private fun parseMulDiv(): Pair<Double, Boolean> {
            val firstFactor = parseUnary()
            var value = firstFactor.first
            var isDirectPercent = firstFactor.second

            while (true) {
                val next = peek()
                if (next is Token.Operator && (next.symbol == '*' || next.symbol == '/')) {
                    consume()
                    val right = parseUnary().first
                    isDirectPercent = false
                    value = if (next.symbol == '*') {
                        value * right
                    } else {
                        if (abs(right) == 0.0) throw EvalException(MathError.DIVIDE_BY_ZERO)
                        value / right
                    }
                } else {
                    break
                }
            }
            return Pair(value, isDirectPercent)
        }

        private fun parseUnary(): Pair<Double, Boolean> {
            val next = peek()
            if (next is Token.Operator && next.symbol == '+') {
                consume()
                return parseUnary()
            }
            if (next is Token.Operator && next.symbol == '-') {
                consume()
                val inner = parseUnary()
                return Pair(-inner.first, inner.second)
            }
            return parsePower()
        }

        private fun parsePower(): Pair<Double, Boolean> {
            val baseResult = parsePostfix()
            var base = baseResult.first
            var isDirectPercent = baseResult.second

            val next = peek()
            if (next is Token.Operator && next.symbol == '^') {
                consume()
                val exponent = parseUnary().first
                isDirectPercent = false
                if (base < 0.0 && exponent != exponent.toLong().toDouble()) {
                    throw EvalException(MathError.DOMAIN)
                }
                base = base.pow(exponent)
            }
            return Pair(base, isDirectPercent)
        }

        private fun parsePostfix(): Pair<Double, Boolean> {
            var value = parsePrimary()
            var endedWithPercent = false
            while (true) {
                when (peek()) {
                    Token.Percent -> {
                        consume()
                        value /= 100.0
                        endedWithPercent = true
                    }
                    Token.Square -> {
                        consume()
                        value *= value
                        endedWithPercent = false
                    }
                    else -> break
                }
            }
            return Pair(value, endedWithPercent)
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw EvalException(MathError.SYNTAX)
            return when (token) {
                is Token.Number -> {
                    consume()
                    token.value
                }
                is Token.LeftParen -> {
                    consume()
                    val inner = parseAddSub()
                    if (peek() != Token.RightParen) {
                        throw EvalException(MathError.SYNTAX)
                    }
                    consume()
                    inner
                }
                is Token.Function -> {
                    consume()
                    val arg = if (peek() == Token.LeftParen) {
                        consume()
                        val inner = parseAddSub()
                        if (peek() != Token.RightParen) {
                            throw EvalException(MathError.SYNTAX)
                        }
                        consume()
                        inner
                    } else {
                        // Support √9 or sin30 shorthand cleanly
                        parseUnary().first
                    }
                    evaluateFunction(token.name, arg)
                }
                else -> throw EvalException(MathError.SYNTAX)
            }
        }

        private fun evaluateFunction(name: String, arg: Double): Double {
            return when (name) {
                "sqrt" -> {
                    if (arg < 0.0) throw EvalException(MathError.DOMAIN)
                    sqrt(arg)
                }
                "log" -> {
                    if (arg <= 0.0) throw EvalException(MathError.DOMAIN)
                    log10(arg)
                }
                "ln" -> {
                    if (arg <= 0.0) throw EvalException(MathError.DOMAIN)
                    ln(arg)
                }
                "sin" -> {
                    if (isDegreeMode) {
                        val mod = ((arg % 360.0) + 360.0) % 360.0
                        if (abs(mod) < 1e-12 || abs(mod - 180.0) < 1e-12) 0.0
                        else sin(Math.toRadians(arg))
                    } else {
                        sin(arg)
                    }
                }
                "cos" -> {
                    if (isDegreeMode) {
                        val mod = ((arg % 360.0) + 360.0) % 360.0
                        if (abs(mod - 90.0) < 1e-12 || abs(mod - 270.0) < 1e-12) 0.0
                        else cos(Math.toRadians(arg))
                    } else {
                        cos(arg)
                    }
                }
                "tan" -> {
                    if (isDegreeMode) {
                        val mod = ((arg % 180.0) + 180.0) % 180.0
                        if (abs(mod - 90.0) < 1e-12) throw EvalException(MathError.DOMAIN)
                        if (abs(mod) < 1e-12) 0.0
                        else tan(Math.toRadians(arg))
                    } else {
                        if (abs(cos(arg)) < 1e-14) throw EvalException(MathError.DOMAIN)
                        tan(arg)
                    }
                }
                else -> throw EvalException(MathError.SYNTAX)
            }
        }
    }
}
