package com.unza.imci.decisionsupport.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unza.imci.decisionsupport.ui.theme.IMCIBackground
import com.unza.imci.decisionsupport.ui.theme.IMCIDanger
import com.unza.imci.decisionsupport.ui.theme.IMCIPrimary

// 1. Main Screen Composable
@Composable
fun DangerSignsScreen(
    onBack: () -> Unit,
    onNext: (List<String>) -> Unit
) {
    // State for checkboxes
    var cannotDrink by remember { mutableStateOf(false) }
    var vomitsEverything by remember { mutableStateOf(false) }
    var convulsions by remember { mutableStateOf(false) }
    var lethargic by remember { mutableStateOf(false) }

    // Collect selected signs into a list
    val selectedSigns = listOfNotNull(
        "Cannot drink or breastfeed".takeIf { cannotDrink },
        "Vomits everything".takeIf { vomitsEverything },
        "Has convulsions".takeIf { convulsions },
        "Lethargic or unconscious".takeIf { lethargic }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IMCIBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Back Button
        TextButton(onClick = onBack) {
            Text("← Back", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            text = "Step 2: Danger Signs",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Warning Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
            border = BorderStroke(1.dp, IMCIDanger)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("️", fontSize = 24.sp)
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

        // Checkboxes List
        CheckboxWithLabel("Cannot drink or breastfeed", cannotDrink) { cannotDrink = it }
        CheckboxWithLabel("Vomits everything", vomitsEverything) { vomitsEverything = it }
        CheckboxWithLabel("Has convulsions", convulsions) { convulsions = it }
        CheckboxWithLabel("Lethargic or unconscious", lethargic) { lethargic = it }

        Spacer(modifier = Modifier.weight(1f))

        // Next Button
        Button(
            onClick = { onNext(selectedSigns) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IMCIPrimary)
        ) {
            Text("Next →", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// 3. Helper Component for Checkbox with Label
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