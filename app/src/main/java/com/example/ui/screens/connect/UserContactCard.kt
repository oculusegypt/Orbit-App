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
fun UserContactCard(
    user: UserAccount,
    isCurrentUser: Boolean,
    onVideoCall: () -> Unit,
    onAudioCall: () -> Unit,
    onSwitchToUser: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isCurrentUser) androidx.compose.foundation.BorderStroke(2.dp, OrbitIndigo) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contact_card_${user.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with presence
                Box {
                    if (user.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.displayName,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    when (user.role) {
                                        UserRole.INSTRUCTOR -> OrbitGoldDark
                                        UserRole.ADMIN -> Color(0xFFDC2626)
                                        UserRole.LEARNER -> OrbitIndigo
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                    }

                    // Online indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (user.isOnline) OrbitEmerald else Color.Gray)
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = OrbitEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("أنت (الحالي)", color = OrbitEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = when (user.role) {
                                UserRole.INSTRUCTOR -> OrbitGold.copy(alpha = 0.2f)
                                UserRole.ADMIN -> Color(0xFFFEE2E2)
                                UserRole.LEARNER -> OrbitIndigo.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = user.role.labelAr,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (user.role) {
                                    UserRole.INSTRUCTOR -> OrbitGoldDark
                                    UserRole.ADMIN -> Color(0xFFDC2626)
                                    UserRole.LEARNER -> OrbitIndigo
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (user.phone.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(user.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (user.bioOrGrade.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(user.bioOrGrade, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isCurrentUser) {
                    Button(
                        onClick = onVideoCall,
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("btn_call_video_${user.id}")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فيديو وشاشة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = onAudioCall,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_call_audio_${user.id}")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("صوت", fontSize = 11.sp)
                    }
                }

                // Switch account button
                OutlinedButton(
                    onClick = onSwitchToUser,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_switch_to_${user.id}")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isCurrentUser) "مُحدد" else "دخول بحسابه", fontSize = 11.sp)
                }
            }
        }
    }
}

