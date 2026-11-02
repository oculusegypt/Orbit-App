package com.example.ui.screens.classroom

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.OrbitIndigo
import com.example.ui.theme.OrbitNavy

/**
 * A named breakout/group-work room attached to a live classroom session.
 */
data class BreakoutGroupSession(
    val id: String,
    val name: String,
    val topic: String,
    val activeStudents: Int,
    val isPrimary: Boolean = false
)

/**
 * Breakout Session Picker Dialog — extracted from VirtualClassroom to keep the
 * screen host lean and make group-switching reusable across classroom surfaces.
 */
@Composable
fun BreakoutRoomPickerDialog(
    sessions: List<BreakoutGroupSession>,
    currentGroup: BreakoutGroupSession,
    onJoin: (BreakoutGroupSession) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(stringResource(R.string.ui_630c211), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(stringResource(R.string.ui_86de27b), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Divider(modifier = Modifier.padding(vertical = 10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sessions.forEach { session ->
                        val isCurrent = session.id == currentGroup.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) OrbitIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onJoin(session)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(session.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(session.topic, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isCurrent) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OrbitIndigo)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.ui_9932cca))
                }
            }
        }
    }
}
