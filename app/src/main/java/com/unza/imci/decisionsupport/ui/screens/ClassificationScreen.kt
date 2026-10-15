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
import com.unza.imci.decisionsupport.ui.components.ClassificationCard
import com.unza.imci.decisionsupport.ui.components.DisclaimerNote
import com.unza.imci.decisionsupport.ui.components.severityStyle
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary
import com.unza.imci.decisionsupport.ui.theme.IMCISevereText

@Composable
fun ClassificationScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onFinish: () -> Unit
) {
    val result = viewModel.result

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
            text = "📋 Step 8: Classification",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (result == null) {
                Text("NO CLASSIFICATION", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            } else {
                if (result.needsUrgentReferral) {
                    Text(
                        text = "🚑 This child needs URGENT REFERRAL.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IMCISevereText
                    )
                }
                result.classifications.forEach {
                    ClassificationCard(
                        title = it.title,
                        treatment = it.treatment,
                        style = severityStyle(it.severity),
                        moduleLabel = it.module.label
                    )
                }
            }

            DisclaimerNote()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSave,
            enabled = result != null && !viewModel.isSaved,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IMCIPrimary)
        ) {
            Text(
                if (viewModel.isSaved) "✅ Saved" else "Save Assessment",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
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
