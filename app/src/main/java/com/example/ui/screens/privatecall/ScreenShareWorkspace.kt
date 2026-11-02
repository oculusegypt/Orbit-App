package com.example.ui.screens.privatecall

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CallStatus
import com.example.data.local.CallType
import com.example.data.local.ScreenShareContent
import com.example.data.models.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveCallState
import com.example.ui.viewmodel.OrbitViewModel

@Composable
fun ParticipantVideoSurface(user: com.example.data.local.UserAccount, isCameraOff: Boolean, isMuted: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        if (!isCameraOff && user.avatarUrl.isNotBlank()) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = user.displayName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp))
            )
            // Gradient Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(OrbitIndigoDark)
                        .border(3.dp, OrbitGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(70.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(user.displayName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.ui_221c540), color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }

        // Live Speaking Wave Indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 90.dp, start = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.GraphicEq,
                contentDescription = null,
                tint = if (isMuted) Color(0xFFEF4444) else OrbitEmerald,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isMuted) stringResource(R.string.ui_cbd2635) else stringResource(R.string.ui_2dde952),
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ScreenShareWorkspace(
    content: ScreenShareContent,
    otherUserName: String,
    sorobanUpper: Boolean,
    sorobanLowerCount: Int,
    onToggleUpper: () -> Unit,
    onSetLowerCount: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp, bottom = 90.dp)
            .background(Color(0xFF020617))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Live Screen Banner
            Surface(
                color = OrbitIndigoDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrbitGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔴 مشاركة حية: ${content.labelAr}",
                            color = OrbitGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "مع $otherUserName (1080p 60fps)",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (content) {
                ScreenShareContent.SOROBAN_BOARD -> {
                    LiveSorobanShareCanvas(
                        upperActive = sorobanUpper,
                        lowerCount = sorobanLowerCount,
                        onToggleUpper = onToggleUpper,
                        onSetLowerCount = onSetLowerCount
                    )
                }
                ScreenShareContent.MATH_WORKSHEET -> {
                    LiveWorksheetShareCanvas()
                }
                ScreenShareContent.WHITEBOARD -> {
                    LiveWhiteboardCanvas()
                }
                ScreenShareContent.DEVICE_SCREEN -> {
                    LiveDeviceScreenMirror()
                }
            }
        }
    }
}

@Composable
fun LiveSorobanShareCanvas(
    upperActive: Boolean,
    lowerCount: Int,
    onToggleUpper: () -> Unit,
    onSetLowerCount: (Int) -> Unit
) {
    val totalValue = (if (upperActive) 5 else 0) + lowerCount

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, OrbitGold),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.ui_890e688),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = stringResource(R.string.ui_9d409be),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Value Display
            Surface(
                color = OrbitGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrbitGold)
            ) {
                Text(
                    text = "القيمة الحالية على العمود = $totalValue",
                    color = OrbitGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Column Abacus Frame
            Surface(
                color = Color(0xFF312E81),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFB45309)),
                modifier = Modifier
                    .width(180.dp)
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Upper Bead (Value = 5)
                    Text(stringResource(R.string.ui_5125e46), color = OrbitGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onToggleUpper,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (upperActive) OrbitGold else Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_soroban_upper_bead")
                    ) {
                        Text(
                            text = if (upperActive) stringResource(R.string.ui_71d0c72) else stringResource(R.string.ui_c090dc3),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (upperActive) OrbitNavyDark else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dividing Beam
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFD97706))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lower Beads (Values = 1 each, 0 to 4)
                    Text(stringResource(R.string.ui_e5ddb9a), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (0..4).forEach { count ->
                            FilterChip(
                                selected = lowerCount == count,
                                onClick = { onSetLowerCount(count) },
                                label = { Text("$count", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrbitEmerald,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Practice Example
                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ui_61e4762),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveWorksheetShareCanvas() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.ui_9eddbc2), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(stringResource(R.string.ui_ddbb8de), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(16.dp))

            val exercises = listOf(
                Triple("12 + 25 - 6", "31", stringResource(R.string.ui_6d3cea5)),
                Triple("48 + 15 + 7", "70", stringResource(R.string.ui_e4b7909)),
                Triple("95 - 43 + 12", "64", stringResource(R.string.ui_34c343e))
            )

            exercises.forEach { (equation, answer, status) ->
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrbitIndigo.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(equation, color = OrbitGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("الناتج: $answer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(status, color = OrbitEmerald, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveWhiteboardCanvas() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, OrbitEmerald),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.ui_80ee6c6), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(stringResource(R.string.ui_50e8995), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Draw, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.ui_f0d570c), color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(R.string.ui_d88c2ea), color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveDeviceScreenMirror() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, OrbitIndigo),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Smartphone, contentDescription = null, tint = OrbitGold, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.ui_b672fe9), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(stringResource(R.string.ui_79fa089), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

