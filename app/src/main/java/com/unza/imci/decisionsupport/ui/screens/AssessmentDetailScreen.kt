package com.unza.imci.decisionsupport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import com.unza.imci.decisionsupport.data.model.ClassificationEntity
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.ui.components.ClassificationCard
import com.unza.imci.decisionsupport.ui.components.severityStyle
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssessmentDetailScreen(
    assessment: AssessmentEntity,
    classifications: List<ClassificationEntity>,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(assessment.timestamp))
    val findings = remember(assessment.assessmentJson) { readableFindings(assessment.assessmentJson) }

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

        // Classifications, colour coded by severity
        Text("Classifications", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))

        if (classifications.isEmpty()) {
            // Records saved before classifications were stored separately
            ClassificationCard(
                title = assessment.classification,
                treatment = assessment.treatmentRecommendation,
                style = severityStyle("")
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                classifications.forEach {
                    ClassificationCard(
                        title = it.title,
                        treatment = it.treatment,
                        style = severityStyle(it.severity),
                        moduleLabel = ImciModule.entries.firstOrNull { m -> m.name == it.module }?.label
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Symptoms and measurements recorded
        Text(
            text = "Clinical Data Recorded",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (findings.isEmpty()) {
                    Text("No signs recorded.", fontSize = 16.sp, color = Color.DarkGray)
                } else {
                    findings.forEach { Text("• $it", fontSize = 16.sp, color = Color.DarkGray) }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp)) // Extra space at the bottom for scrolling
    }
}

/**
 * Turns the stored assessment JSON into plain-language lines: every sign that was present,
 * plus each measurement that was entered. Keys are camelCase field names, e.g.
 * "chestIndrawing" -> "Chest indrawing".
 */
private fun readableFindings(json: String): List<String> {
    val obj = try {
        JSONObject(json)
    } catch (e: Exception) {
        return emptyList()
    }
    return obj.keys().asSequence().mapNotNull { key ->
        val label = key.replace(Regex("([A-Z])"), " $1").lowercase().replaceFirstChar { it.uppercase() }
        when (val value = obj.opt(key)) {
            true -> label
            is Number -> if (value.toDouble() != 0.0) "$label: $value" else null
            is String -> "$label: ${value.lowercase().replace('_', ' ')}"
            else -> null // false, null
        }
    }.toList()
}
