package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CalculationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LabCalculatorDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao

    companion object {
        @Volatile
        private var INSTANCE: LabCalculatorDatabase? = null

        fun getInstance(context: Context): LabCalculatorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LabCalculatorDatabase::class.java,
                    "atharv_lab_calculator.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
