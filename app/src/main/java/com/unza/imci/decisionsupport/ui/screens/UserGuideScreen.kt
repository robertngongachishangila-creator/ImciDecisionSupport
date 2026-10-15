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
import com.unza.imci.decisionsupport.domain.decision_engine.Severity
import com.unza.imci.decisionsupport.ui.components.ClassificationCard
import com.unza.imci.decisionsupport.ui.components.DisclaimerNote
import com.unza.imci.decisionsupport.ui.components.severityStyle
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary

/**
 * In-app user guide for CHWs (proposal Expected Outcomes: "a simple user guide ...
 * available within the app"). The same content is in docs/USER_GUIDE.md for printing.
 */
@Composable
fun UserGuideScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IMCIBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to Home", fontSize = 18.sp, color = IMCIPrimary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("📖 User Guide", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        GuideSection(
            "🩺 Doing an assessment",
            "Tap New Assessment. Answer each step in order, then tap Next:\n" +
                "1. 👶 Patient info: age and an ID (use a code, not the child's name)\n" +
                "2. ⚠️ Danger signs: check these first\n" +
                "3. 🌬️ Cough or difficult breathing: count breaths for one full minute\n" +
                "4. 💧 Diarrhoea: look and feel for dehydration\n" +
                "5. 🌡️ Fever: record the malaria test result if one was done\n" +
                "6. 👂 Ear problem\n" +
                "7. 📏 Malnutrition and anaemia: measure MUAC (6 months and older), look at the palms\n" +
                "Tap ← Back at any time to change an answer."
        )

        GuideSection(
            "📋 Reading the results",
            "The child gets one result for every illness found. Results are coloured like the " +
                "IMCI chart booklet. The most urgent are shown first:"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ClassificationCard("Pink", "Refer the child urgently to hospital.", severityStyle(Severity.SEVERE))
            ClassificationCard("Yellow", "Give the specific treatment shown.", severityStyle(Severity.MODERATE))
            ClassificationCard("Green", "Advise the mother on home care.", severityStyle(Severity.MILD))
        }

        GuideSection(
            "💾 Saving and history",
            "Tap Save Assessment on the results screen. Saved assessments are kept only on this " +
                "phone and work without internet. Open View History to review or delete them."
        )

        GuideSection(
            "🔒 PIN lock",
            "Enter your PIN each time the app opens. If you forget it, choose Forgot PIN: this " +
                "deletes all saved assessments so patient data stays private, and you set a new PIN."
        )

        Spacer(modifier = Modifier.height(24.dp))
        DisclaimerNote()
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun GuideSection(title: String, body: String) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    Spacer(modifier = Modifier.height(8.dp))
    Text(body, fontSize = 16.sp, color = Color.Black)
    Spacer(modifier = Modifier.height(8.dp))
}
