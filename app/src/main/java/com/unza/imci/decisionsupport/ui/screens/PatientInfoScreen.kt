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
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary

@Composable
fun PatientInfoScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var yearsInput by remember { mutableStateOf(if (viewModel.ageInMonths == 0) "" else (viewModel.ageInMonths / 12).toString()) }
    var monthsInput by remember { mutableStateOf(if (viewModel.ageInMonths == 0) "" else (viewModel.ageInMonths % 12).toString()) }

    fun updateAge() {
        val years = yearsInput.toIntOrNull() ?: 0
        val months = monthsInput.toIntOrNull() ?: 0
        viewModel.ageInMonths = (years * 12) + months
    }

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
            text = "Step 1: Patient Info",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Age", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Years", fontSize = 14.sp, color = Color.DarkGray)
                        OutlinedTextField(
                            value = yearsInput,
                            onValueChange = {
                                yearsInput = it
                                updateAge()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., 2") }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Months", fontSize = 14.sp, color = Color.DarkGray)
                        OutlinedTextField(
                            value = monthsInput,
                            onValueChange = {
                                monthsInput = it
                                updateAge()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("0–11") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Patient ID (Optional)", fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = viewModel.patientId,
                    onValueChange = { viewModel.patientId = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., P123") }
                )
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
