package com.example.ui.screens.classroom

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.ClassroomMessage
import com.example.data.repository.OrbitRepository
import com.example.ui.screens.classroom.LiveAudioMonitor
import com.example.ui.screens.classroom.LiveCameraPreview
import com.example.ui.screens.classroom.LiveWebRTCMeetingRoom
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.ClassroomMessage
import com.example.data.repository.OrbitRepository
import com.example.ui.screens.classroom.LiveAudioMonitor
import com.example.ui.screens.classroom.LiveCameraPreview
import com.example.ui.screens.classroom.LiveWebRTCMeetingRoom
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel

data class BreakoutGroupSession(
    val id: String,
    val name: String,
    val topic: String,
    val activeStudents: Int,
    val isPrimary: Boolean = false
)

data class DrawPoint(
    val offset: Offset,
    val color: Color
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
                Text("الغرف التفاعلية ومجموعات العمل 👥", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("اختر الغرفة المناسبة لمستوى طفلك للتدريب التفاعلي المباشر", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("إغلاق")
                }
            }
        }
    }
}
