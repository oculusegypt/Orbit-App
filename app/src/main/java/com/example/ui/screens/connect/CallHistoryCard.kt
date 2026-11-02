package com.example.ui.screens.connect

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CallSessionEntity
import com.example.data.local.CallType
import com.example.data.local.UserAccount
import com.example.data.models.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallHistoryCard(call: CallSessionEntity) {
    val dateFormatted = remember(call.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale("ar"))
        sdf.format(Date(call.timestamp))
    }
    val durationFormatted = remember(call.durationSeconds) {
        val minutes = call.durationSeconds / 60
        val seconds = call.durationSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (call.status == "COMPLETED") OrbitEmerald.copy(alpha = 0.15f) else Color(0xFFFEE2E2)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (call.callType == "VIDEO_SCREEN") Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = null,
                    tint = if (call.status == "COMPLETED") OrbitEmerald else Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${call.callerName} ➔ ${call.receiverName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "$dateFormatted • ${if (call.hadScreenShare) "تضمنت مشاركة شاشة 🖥️" else "مكالمة عادية"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (call.status == "COMPLETED") OrbitEmerald.copy(alpha = 0.2f) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (call.status == "COMPLETED") durationFormatted else "مرفوضة / فائتة",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (call.status == "COMPLETED") OrbitEmerald else Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
