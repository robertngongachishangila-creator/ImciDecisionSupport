package com.unza.imci.decisionsupport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.unza.imci.decisionsupport.ui.screens.DangerSignsScreen
import com.unza.imci.decisionsupport.ui.screens.HomeScreen
import com.unza.imci.decisionsupport.ui.screens.PatientInfoScreen
import com.unza.imci.decisionsupport.ui.theme.ImciDecisionSupportTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImciDecisionSupportTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // State to track which screen is currently showing
                    var currentScreen by remember { mutableStateOf("home") }

                    when (currentScreen) {
                        // 1. Home Screen
                        "home" -> HomeScreen(
                            onNewAssessment = { currentScreen = "patient_info" },
                            onViewHistory = { /* TODO */ }
                        )

                        // 2. Patient Info Screen
                        "patient_info" -> PatientInfoScreen(
                            onBack = { currentScreen = "home" },
                            onNext = { id -> currentScreen = "danger_signs" }
                        )

                        // 3. Danger Signs Screen
                        "danger_signs" -> DangerSignsScreen(
                            onBack = { currentScreen = "patient_info" },
                            onNext = { signs ->
                                // We will build the next screen (Respiratory) in the next step!
                            }
                        )
                    }
                }
            }
        }
    }
}