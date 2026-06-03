package com.unza.imci.decisionsupport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssessmentDetailScreen(
    assessment: AssessmentEntity,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(assessment.timestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IMCIBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Allows scrolling if text is long
    ) {
        // Back Button
        TextButton(onClick = onBack) {
            Text("← Back to History", fontSize = 18.sp, color = IMCIPrimary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Header Info
        Text(
            text = "Patient Assessment Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ID: ${assessment.patientId.ifEmpty { "Anonymous" }}", fontSize = 16.sp, color = Color.DarkGray)
            Text(formattedDate, fontSize = 14.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Classification Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)) // Light Orange
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Diagnosis", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = assessment.classification,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100) // Dark Orange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Treatment Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)) // Light Green
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Treatment Plan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = assessment.treatmentRecommendation,
                    fontSize = 16.sp,
                    color = Color(0xFF1B5E20) // Dark Green
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Raw Data / Symptoms Checked
        Text(
            text = "Clinical Data Recorded",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Display the JSON string nicely formatted in a code-like font
                Text(
                    text = assessment.assessmentJson,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontFamily = FontFamily.Monospace // Makes it look like structured data
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp)) // Extra space at the bottom for scrolling
    }
}