package com.unza.imci.decisionsupport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun MalnutritionScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
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
            text = "📏 Step 7: Malnutrition & Anaemia",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.weight(1f)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Malnutrition", fontWeight = FontWeight.Medium)
                    CheckboxWithLabel("Visible severe wasting", viewModel.visibleSevereWasting) { viewModel.visibleSevereWasting = it }
                    CheckboxWithLabel("Oedema of both feet", viewModel.bilateralPittingOedema) { viewModel.bilateralPittingOedema = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    // MUAC is only valid for children aged 6 months and older
                    if (viewModel.ageInMonths >= 6) {
                        Text("MUAC (cm)", fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = viewModel.muacCm,
                            onValueChange = { viewModel.muacCm = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., 12.5") }
                        )
                    } else {
                        Text(
                            "MUAC is not measured for children under 6 months.",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Anaemia (look at the palms)", fontWeight = FontWeight.Medium)
                    // Pallor levels are mutually exclusive: ticking one clears the other
                    CheckboxWithLabel("Some palmar pallor", viewModel.somePalmarPallor) {
                        viewModel.somePalmarPallor = it
                        if (it) viewModel.severePalmarPallor = false
                    }
                    CheckboxWithLabel("Severe palmar pallor", viewModel.severePalmarPallor) {
                        viewModel.severePalmarPallor = it
                        if (it) viewModel.somePalmarPallor = false
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IMCIPrimary)
        ) {
            Text("Next →", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
