package com.unza.imci.decisionsupport

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unza.imci.decisionsupport.ui.screens.ClassificationScreen
import com.unza.imci.decisionsupport.ui.screens.DangerSignsScreen
import com.unza.imci.decisionsupport.ui.screens.DiarrhoeaScreen
import com.unza.imci.decisionsupport.ui.screens.HomeScreen
import com.unza.imci.decisionsupport.ui.screens.PatientInfoScreen
import com.unza.imci.decisionsupport.ui.screens.RespiratoryScreen
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
                    var currentScreen by remember { mutableStateOf("home") }
                    val viewModel: AssessmentViewModel = viewModel()
                    val context = LocalContext.current

                    when (currentScreen) {
                        "home" -> HomeScreen(
                            onNewAssessment = { currentScreen = "patient_info" },
                            onViewHistory = { }
                        )

                        "patient_info" -> PatientInfoScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "home" },
                            onNext = { currentScreen = "danger_signs" }
                        )

                        "danger_signs" -> DangerSignsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "patient_info" },
                            onNext = { currentScreen = "respiratory" }
                        )

                        "respiratory" -> RespiratoryScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "danger_signs" },
                            onNext = { currentScreen = "diarrhoea" }
                        )

                        "diarrhoea" -> DiarrhoeaScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "respiratory" },
                            onNext = {
                                viewModel.calculateResults()
                                currentScreen = "classification"
                            }
                        )

                        "classification" -> ClassificationScreen(
                            viewModel = viewModel,
                            onSave = {
                                Toast.makeText(context, "Assessment Saved!", Toast.LENGTH_SHORT).show()
                            },
                            onFinish = { currentScreen = "home" }
                        )
                    }
                }
            }
        }
    }
}
