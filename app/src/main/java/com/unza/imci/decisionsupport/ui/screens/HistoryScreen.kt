package com.unza.imci.decisionsupport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    assessments: List<AssessmentEntity>,
    onBack: () -> Unit,
    onItemClick: (AssessmentEntity) -> Unit, // NEW: Click handler
    onDeleteAssessment: (AssessmentEntity) -> Unit
) {
    val sortedAssessments = remember(assessments) {
        assessments.sortedByDescending { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IMCIBackground)
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to Home", fontSize = 18.sp, color = IMCIPrimary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Assessment History",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "${assessments.size} saved assessment(s)",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (sortedAssessments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No assessments yet", color = Color.Gray, fontSize = 18.sp)
                    Text("Complete an assessment to see it here", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedAssessments, key = { it.id }) { assessment ->
                    AssessmentHistoryItem(
                        assessment = assessment,
                        onItemClick = { onItemClick(assessment) }, // NEW: Pass click
                        onDelete = { onDeleteAssessment(assessment) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssessmentHistoryItem(
    assessment: AssessmentEntity,
    onItemClick: () -> Unit, // NEW: Click handler
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(assessment.timestamp) { dateFormat.format(Date(assessment.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onItemClick, // NEW: Make entire card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    assessment.patientId.ifEmpty { "No ID" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(formattedDate, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                assessment.classification,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = IMCIPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Delete button (only show on the right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    // Prevent opening detail screen when deleting
                    onDelete()
                }) {
                    Text("🗑️ Delete", color = Color.Red, fontSize = 12.sp)
                }
            }
        }
    }
}