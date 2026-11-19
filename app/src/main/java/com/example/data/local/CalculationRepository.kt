package com.example.data.local

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val calculationDao: CalculationDao) {
    val history: Flow<List<CalculationEntity>> = calculationDao.getAllHistory()

    suspend fun saveCalculation(
        expression: String,
        result: String,
        mode: String,
        angleUnit: String
    ) {
        calculationDao.insertCalculation(
            CalculationEntity(
                expression = expression,
                result = result,
                mode = mode,
                angleUnit = angleUnit,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteEntry(id: Long) {
        calculationDao.deleteById(id)
    }

    suspend fun clearHistory() {
        calculationDao.clearAllHistory()
    }
}
