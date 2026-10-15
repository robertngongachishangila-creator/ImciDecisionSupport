package com.unza.imci.decisionsupport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unza.imci.decisionsupport.AssessmentViewModel
import com.unza.imci.decisionsupport.ui.components.CheckboxWithLabel
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary

@Composable
fun DiarrhoeaScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var durationInput by remember { mutableStateOf(if (viewModel.diarrhoeaDurationDays == 0) "" else viewModel.diarrhoeaDurationDays.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IMCIBackground)
            .padding(16.dp)
    ) {
        // Back Button
        TextButton(onClick = onBack) {
            Text("← Back", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            text = "💧 Step 4: Diarrhoea Assessment",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Inputs (scrollable: the dehydration checklist is long on small screens)
        Box(modifier = Modifier.weight(1f)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    CheckboxWithLabel("Has diarrhoea?", viewModel.hasDiarrhoea) { viewModel.hasDiarrhoea = it }

                    if (viewModel.hasDiarrhoea) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Duration (days)", fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = durationInput,
                            onValueChange = {
                                durationInput = it
                                viewModel.diarrhoeaDurationDays = it.toIntOrNull() ?: 0
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., 5") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        CheckboxWithLabel("Blood in stool", viewModel.bloodInStool) { viewModel.bloodInStool = it }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Look and feel for dehydration", fontWeight = FontWeight.Medium)
                        Text(
                            "Lethargic / unconscious and not able to drink are taken from the danger signs step.",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        CheckboxWithLabel("Restless or irritable", viewModel.restlessOrIrritable) { viewModel.restlessOrIrritable = it }
                        CheckboxWithLabel("Sunken eyes", viewModel.sunkenEyes) { viewModel.sunkenEyes = it }
                        CheckboxWithLabel("Drinking poorly", viewModel.drinkingPoorly) { viewModel.drinkingPoorly = it }
                        CheckboxWithLabel("Drinks eagerly, thirsty", viewModel.drinksEagerlyThirsty) { viewModel.drinksEagerlyThirsty = it }
                        // Pinch results are mutually exclusive: ticking one clears the other
                        CheckboxWithLabel("Skin pinch goes back slowly", viewModel.skinPinchSlowly) {
                            viewModel.skinPinchSlowly = it
                            if (it) viewModel.skinPinchVerySlowly = false
                        }
                        CheckboxWithLabel("Skin pinch goes back VERY slowly (longer than 2 seconds)", viewModel.skinPinchVerySlowly) {
                            viewModel.skinPinchVerySlowly = it
                            if (it) viewModel.skinPinchSlowly = false
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next Button
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IMCIPrimary)
        ) {
            Text("Next →", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
