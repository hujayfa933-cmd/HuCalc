package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.ui.CalculatorScreen
import com.example.calculator.ui.CalculatorViewModel
import com.example.calculator.ui.components.HuCalcSplashScreen
import com.example.ui.theme.LocalCalculatorColors
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var showSplash by rememberSaveable { mutableStateOf(true) }

            MyApplicationTheme(themeMode = uiState.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LocalCalculatorColors.current.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CalculatorScreen(viewModel = viewModel)

                        if (showSplash) {
                            HuCalcSplashScreen(
                                onDismiss = { showSplash = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
