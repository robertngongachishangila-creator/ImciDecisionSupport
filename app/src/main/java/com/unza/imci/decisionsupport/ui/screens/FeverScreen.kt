package com.unza.imci.decisionsupport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun FeverScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var durationInput by remember { mutableStateOf(if (viewModel.feverDurationDays == 0) "" else viewModel.feverDurationDays.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IMCIBackground)
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Step 5: Fever Assessment",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                CheckboxWithLabel("Has fever (by history or feels hot)", viewModel.hasFever) { viewModel.hasFever = it }

                if (viewModel.hasFever) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Duration (days)", fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = durationInput,
                        onValueChange = {
                            durationInput = it
                            viewModel.feverDurationDays = it.toIntOrNull() ?: 0
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., 3") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    CheckboxWithLabel("Stiff neck", viewModel.stiffNeck) { viewModel.stiffNeck = it }
                    CheckboxWithLabel("Runny nose", viewModel.runnyNose) { viewModel.runnyNose = it }
                    CheckboxWithLabel("Measles now or in the last 3 months", viewModel.measles) { viewModel.measles = it }

                    if (viewModel.measles) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Measles complications", fontWeight = FontWeight.Medium)
                        CheckboxWithLabel("Clouding of cornea", viewModel.cloudingOfCornea) { viewModel.cloudingOfCornea = it }
                        CheckboxWithLabel("Deep or extensive mouth ulcers", viewModel.deepOrExtensiveMouthUlcers) { viewModel.deepOrExtensiveMouthUlcers = it }
                        CheckboxWithLabel("Mouth ulcers", viewModel.mouthUlcers) { viewModel.mouthUlcers = it }
                        CheckboxWithLabel("Pus draining from the eye", viewModel.pusDrainingFromEye) { viewModel.pusDrainingFromEye = it }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IMCIPrimary)
        ) {
            Text("Next →", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
