package com.unza.imci.decisionsupport.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unza.imci.decisionsupport.AssessmentViewModel
import com.unza.imci.decisionsupport.ui.components.CheckboxWithLabel
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIDanger
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary

@Composable
fun DangerSignsScreen(
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
            text = "Step 2: Danger Signs",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
            border = BorderStroke(1.dp, IMCIDanger)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚠️", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Check these FIRST. If ANY are present, refer immediately.",
                    color = IMCIDanger,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CheckboxWithLabel("Cannot drink or breastfeed", viewModel.cannotDrink) {
            viewModel.cannotDrink = it
        }
        CheckboxWithLabel("Vomits everything", viewModel.vomitsEverything) {
            viewModel.vomitsEverything = it
        }
        CheckboxWithLabel("Has convulsions", viewModel.convulsions) {
            viewModel.convulsions = it
        }
        CheckboxWithLabel("Lethargic or unconscious", viewModel.lethargic) {
            viewModel.lethargic = it
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
