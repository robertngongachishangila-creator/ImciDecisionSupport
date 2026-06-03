package com.unza.imci.decisionsupport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unza.imci.decisionsupport.AssessmentViewModel
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary
import com.unza.imci.decisionsupport.ui.theme.IMCIWarning

@Composable
fun ClassificationScreen(
    viewModel: AssessmentViewModel,
    onSave: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IMCIBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Step 5: Classification",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IMCIWarning)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = viewModel.classificationTitle.ifEmpty { "NO CLASSIFICATION" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Recommended Treatment", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(viewModel.treatmentText.ifEmpty { "No treatment recommended" }, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IMCIPrimary)
        ) {
            Text("Save Assessment", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Return to Home", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
