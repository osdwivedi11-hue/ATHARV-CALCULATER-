package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CalculationRepository
import com.example.data.local.LabCalculatorDatabase
import com.example.data.local.PreferencesRepository
import com.example.ui.calculator.CalculatorViewModel
import com.example.ui.calculator.LabCalculatorScreen
import com.example.ui.theme.LabCalculatorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels {
        val database = LabCalculatorDatabase.getInstance(applicationContext)
        val calculationRepository = CalculationRepository(database.calculationDao())
        val preferencesRepository = PreferencesRepository(applicationContext)
        CalculatorViewModel.Factory(calculationRepository, preferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isDarkMode) {
                val style = if (uiState.isDarkMode) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(
                    statusBarStyle = style,
                    navigationBarStyle = style
                )
            }

            LabCalculatorTheme(darkTheme = uiState.isDarkMode) {
                LabCalculatorScreen(viewModel = viewModel)
            }
        }
    }
}
