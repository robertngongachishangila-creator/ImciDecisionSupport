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
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground

@Composable
fun PatientInfoScreen(
    onBack: () -> Unit,
    onNext: (String) -> Unit
) {
    var ageInput by remember { mutableStateOf("") }
    var patientIdInput by remember { mutableStateOf("") }

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
                Text("Age (months)", fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Patient ID (Optional)", fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = patientIdInput,
                    onValueChange = { patientIdInput = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onNext(patientIdInput) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Next →", fontSize = 18.sp)
        }
    }
}