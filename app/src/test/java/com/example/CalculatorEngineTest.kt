package com.example

import com.example.domain.engine.CalculationResult
import com.example.domain.engine.CalculatorEngine
import com.example.domain.engine.MathError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CalculatorEngineTest {

    private fun assertResult(expected: String, expression: String, isDegree: Boolean = true) {
        val result = CalculatorEngine.evaluate(expression, isDegreeMode = isDegree)
        assertTrue("Expected Success for '$expression' but got $result", result is CalculationResult.Success)
        assertEquals(expected, (result as CalculationResult.Success).formatted)
    }

    private fun assertApprox(expected: Double, expression: String, isDegree: Boolean = true, tol: Double = 1e-6) {
        val result = CalculatorEngine.evaluate(expression, isDegreeMode = isDegree)
        assertTrue("Expected Success for '$expression' but got $result", result is CalculationResult.Success)
        val actual = (result as CalculationResult.Success).value
        assertTrue("Expected ~$expected for '$expression', got $actual", abs(expected - actual) < tol)
    }

    private fun assertError(expectedError: MathError, expression: String, isDegree: Boolean = true) {
        val result = CalculatorEngine.evaluate(expression, isDegreeMode = isDegree)
        assertEquals(CalculationResult.Error(expectedError), result)
    }

    @Test
    fun basicArithmeticAndOrderOfOperations() {
        assertResult("14", "2+3×4")
        assertResult("20", "(2+3)×4")
        assertResult("0.3", "0.1+0.2")
        assertResult("-5", "3−8")
        assertResult("2.5", "10÷4")
    }

    @Test
    fun percentageOperations() {
        assertResult("220", "200+10%")
        assertResult("170", "200−15%")
        assertResult("20", "200×10%")
        assertResult("0.5", "50%")
    }

    @Test
    fun scientificFunctionsAndPowers() {
        assertResult("25", "5²")
        assertResult("25", "(2+3)²")
        assertResult("9", "√(81)")
        assertResult("3", "√9")
        assertResult("1024", "2^10")
        assertResult("512", "2^3^2")
        assertResult("2", "log(100)")
        assertResult("1", "ln(e)")
    }

    @Test
    fun trigonometryDegreesAndRadians() {
        assertResult("0.5", "sin(30)", isDegree = true)
        assertResult("0", "cos(90)", isDegree = true)
        assertResult("1", "tan(45)", isDegree = true)
        assertError(MathError.DOMAIN, "tan(90)", isDegree = true)

        assertApprox(1.0, "sin(π÷2)", isDegree = false)
        assertApprox(-1.0, "cos(π)", isDegree = false)
    }

    @Test
    fun implicitMultiplicationAndAutoCloseParens() {
        assertResult("14", "2(3+4)")
        assertResult("9", "3√(9)")
        assertResult("0.5", "sin(30", isDegree = true)
    }

    @Test
    fun errorHandling() {
        assertError(MathError.DIVIDE_BY_ZERO, "10÷0")
        assertError(MathError.DOMAIN, "√(−9)")
        assertError(MathError.DOMAIN, "log(0)")
        assertError(MathError.DOMAIN, "ln(−2)")
    }
}
