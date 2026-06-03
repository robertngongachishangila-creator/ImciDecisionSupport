package com.unza.imci.decisionsupport

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import com.unza.imci.decisionsupport.ui.screens.*
import com.unza.imci.decisionsupport.ui.theme.ImciDecisionSupportTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
                    var selectedAssessment by remember { mutableStateOf<AssessmentEntity?>(null) }

                    val viewModel: AssessmentViewModel = viewModel()
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()

                    var allAssessments by remember { mutableStateOf(listOf<AssessmentEntity>()) }

                    LaunchedEffect(currentScreen) {
                        if (currentScreen == "history") {
                            scope.launch {
                                viewModel.getAllAssessments().collectLatest { assessments ->
                                    allAssessments = assessments
                                }
                            }
                        }
                    }

                    when (currentScreen) {
                        "home" -> HomeScreen(
                            onNewAssessment = {
                                viewModel.resetForm()
                                currentScreen = "patient_info"
                            },
                            onViewHistory = { currentScreen = "history" }
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
                                viewModel.saveAssessment {
                                    Toast.makeText(context, "✅ Assessment Saved!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onFinish = {
                                viewModel.resetForm()
                                currentScreen = "home"
                            }
                        )
                        "history" -> HistoryScreen(
                            assessments = allAssessments,
                            onBack = { currentScreen = "home" },
                            onItemClick = { assessment ->
                                selectedAssessment = assessment
                                currentScreen = "detail"
                            },
                            onDeleteAssessment = { assessment ->
                                scope.launch {
                                    viewModel.deleteAssessment(assessment)
                                    Toast.makeText(context, "Assessment deleted", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        "detail" -> selectedAssessment?.let { assessment ->
                            AssessmentDetailScreen(
                                assessment = assessment,
                                onBack = { currentScreen = "history" }
                            )
                        }
                    }
                }
            }
        }
    }
}