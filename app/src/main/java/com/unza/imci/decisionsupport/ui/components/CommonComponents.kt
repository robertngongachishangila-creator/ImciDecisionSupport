package com.unza.imci.decisionsupport.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unza.imci.decisionsupport.domain.decision_engine.Severity
import com.unza.imci.decisionsupport.ui.theme.IMCIMildGreen
import com.unza.imci.decisionsupport.ui.theme.IMCIMildText
import com.unza.imci.decisionsupport.ui.theme.IMCIModerateText
import com.unza.imci.decisionsupport.ui.theme.IMCIModerateYellow
import com.unza.imci.decisionsupport.ui.theme.IMCISeverePink
import com.unza.imci.decisionsupport.ui.theme.IMCISevereText

@Composable
fun CheckboxWithLabel(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 16.sp, modifier = Modifier.weight(1f))
    }
}

/** Colours, icon and action label for one IMCI severity row. */
data class SeverityStyle(val background: Color, val text: Color, val icon: String, val action: String)

fun severityStyle(severity: Severity): SeverityStyle = when (severity) {
    Severity.SEVERE -> SeverityStyle(IMCISeverePink, IMCISevereText, "🚑", "URGENT REFERRAL")
    Severity.MODERATE -> SeverityStyle(IMCIModerateYellow, IMCIModerateText, "💊", "TREAT")
    Severity.MILD -> SeverityStyle(IMCIMildGreen, IMCIMildText, "🏠", "HOME CARE")
}

/** Stored classifications keep severity as text; unknown values fall back to yellow. */
fun severityStyle(severityName: String): SeverityStyle =
    severityStyle(Severity.entries.firstOrNull { it.name == severityName } ?: Severity.MODERATE)

/**
 * One IMCI classification, colour coded like the Chart Booklet so its urgency can be
 * recognised at a glance without reading the text.
 */
@Composable
fun ClassificationCard(title: String, treatment: String, style: SeverityStyle, moduleLabel: String? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = style.background),
        border = BorderStroke(2.dp, style.text)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(style.icon, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(style.action, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = style.text)
                    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = style.text)
                    if (moduleLabel != null) {
                        Text(moduleLabel, fontSize = 13.sp, color = style.text)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(treatment, fontSize = 16.sp, color = Color.Black)
        }
    }
}

/** Clinical-safety disclaimer required by the proposal (Ethical and Legal Considerations). */
@Composable
fun DisclaimerNote(modifier: Modifier = Modifier) {
    Text(
        text = "ℹ️ This app supports, but does not replace, clinical judgment. Always follow your " +
            "IMCI training and your supervisor's guidance.",
        fontSize = 13.sp,
        color = Color.DarkGray,
        modifier = modifier
    )
}
