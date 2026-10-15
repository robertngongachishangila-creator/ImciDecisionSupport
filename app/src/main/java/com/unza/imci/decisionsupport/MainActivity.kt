package com.unza.imci.decisionsupport

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import com.unza.imci.decisionsupport.data.model.ClassificationEntity
import com.unza.imci.decisionsupport.data.security.PinManager
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
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()

                    var currentScreen by remember {
                        mutableStateOf(if (PinManager.isPinSet(context)) "pin_entry" else "pin_setup")
                    }
                    var selectedAssessment by remember { mutableStateOf<AssessmentEntity?>(null) }
                    var selectedClassifications by remember { mutableStateOf(listOf<ClassificationEntity>()) }

                    val viewModel: AssessmentViewModel = viewModel()

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

                    // Single source of truth for "back" behaviour, shared by every screen's
                    // in-app back button AND the system back gesture/button below, so they
                    // can never drift out of sync.
                    fun navigateBack() {
                        currentScreen = when (currentScreen) {
                            "patient_info" -> "home"
                            "danger_signs" -> "patient_info"
                            "respiratory" -> "danger_signs"
                            "diarrhoea" -> "respiratory"
                            "fever" -> "diarrhoea"
                            "ear" -> "fever"
                            "malnutrition" -> "ear"
                            "classification" -> "malnutrition"
                            "history" -> "home"
                            "guide" -> "home"
                            "detail" -> "history"
                            else -> currentScreen // home, pin_setup, pin_entry: handled below
                        }
                    }

                    // System back button/gesture mirrors the in-app back buttons everywhere
                    // except the PIN screens (where back must not bypass the lock — it falls
                    // through to the default system behaviour, which exits the app) and Home
                    // (the root screen, where back should also exit the app as usual).
                    BackHandler(enabled = currentScreen !in setOf("pin_setup", "pin_entry", "home")) {
                        navigateBack()
                    }

                    when (currentScreen) {
                        "pin_setup" -> PinSetupScreen(
                            onPinCreated = { pin ->
                                PinManager.setPin(context, pin)
                                currentScreen = "home"
                            }
                        )
                        "pin_entry" -> PinEntryScreen(
                            onPinVerified = { pin ->
                                val success = PinManager.verifyPin(context, pin)
                                if (success) currentScreen = "home"
                                success
                            },
                            onForgotPin = {
                                scope.launch {
                                    viewModel.deleteAllAssessments()
                                    PinManager.clearPin(context)
                                    currentScreen = "pin_setup"
                                }
                            }
                        )
                        "home" -> HomeScreen(
                            onNewAssessment = {
                                viewModel.resetForm()
                                currentScreen = "patient_info"
                            },
                            onViewHistory = { currentScreen = "history" },
                            onOpenGuide = { currentScreen = "guide" }
                        )
                        "patient_info" -> PatientInfoScreen(
                            viewModel = viewModel,
                            onBack = { navigateBack() },
                            onNext = { currentScreen = "danger_signs" }
                        )
                        "danger_signs" -> DangerSignsScreen(
                            viewModel = viewModel,
                            onBack = { navigateBack() },
                            onNext = { currentScreen = "respiratory" }
                        )
                        "respiratory" -> RespiratoryScreen(
                            viewModel = viewModel,
                            onBack = { navigateBack() },
                            onNext = { currentScreen = "diarrhoea" }
                        )
                        "diarrhoea" -> DiarrhoeaScreen(
                            viewModel = viewModel,
                            onBack = { navigateBack() },
                            onNext = { currentScreen = "fever" }
                        )
                        "fever" -> FeverScreen(
                            viewModel = viewModel,
                            onBack = { navigateBack() },
                            onNext = { currentScreen = "ear" }
                        )
                        "ear" -> EarScreen(
                            viewModel = viewModel,
                            onBack = { navigateBack() },
                            onNext = { currentScreen = "malnutrition" }
                        )
                        "malnutrition" -> MalnutritionScreen(
                            viewModel = viewModel,
                            onBack = { navigateBack() },
                            onNext = {
                                viewModel.calculateResults()
                                currentScreen = "classification"
                            }
                        )
                        "classification" -> ClassificationScreen(
                            viewModel = viewModel,
                            onBack = { navigateBack() },
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
                            onBack = { navigateBack() },
                            onItemClick = { assessment ->
                                scope.launch {
                                    selectedAssessment = assessment
                                    selectedClassifications = viewModel.getClassifications(assessment.id)
                                    currentScreen = "detail"
                                }
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
                                classifications = selectedClassifications,
                                onBack = { navigateBack() }
                            )
                        }
                        "guide" -> UserGuideScreen(onBack = { navigateBack() })
                    }
                }
            }
        }
    }
}