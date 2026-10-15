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
fun EarScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var durationInput by remember { mutableStateOf(if (viewModel.earDischargeDurationDays == 0) "" else viewModel.earDischargeDurationDays.toString()) }

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
            text = "👂 Step 6: Ear Problem Assessment",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                CheckboxWithLabel("Ear pain", viewModel.hasEarPain) { viewModel.hasEarPain = it }
                CheckboxWithLabel("Ear discharge (pus)", viewModel.earDischarge) { viewModel.earDischarge = it }

                if (viewModel.earDischarge) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Discharge duration (days)", fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = durationInput,
                        onValueChange = {
                            durationInput = it
                            viewModel.earDischargeDurationDays = it.toIntOrNull() ?: 0
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., 14") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                CheckboxWithLabel("Tender swelling behind the ear", viewModel.tenderSwellingBehindEar) { viewModel.tenderSwellingBehindEar = it }
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
