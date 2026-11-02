package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.ClassroomMessage
import com.example.data.repository.OrbitRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel

@Composable
fun ClassroomScreen(
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isMuted by viewModel.classroomMuted.collectAsState()
    val isVideoOn by viewModel.classroomVideoOn.collectAsState()
    val isHandRaised by viewModel.classroomHandRaised.collectAsState()
    val isScreenShared by viewModel.classroomScreenShared.collectAsState()
    val chatInput by viewModel.classroomChatText.collectAsState()
    val messages by viewModel.classroomMessages.collectAsState()
    val liveSessions by viewModel.liveSessions.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var isMeetingConnected by remember { mutableStateOf(true) }
    var screenShareMode by remember { mutableStateOf("abacus") } // "abacus" | "slides" | "students" | "camera"
    var activeTab by remember { mutableStateOf("chat") } // "chat" | "schedule" | "participants"
    var askAsQuestion by remember { mutableStateOf(false) }

    // Interactive 3-Rod Soroban Abacus state (مئات، عشرات، آحاد)
    var rod1Upper by remember { mutableStateOf(false) }
    var rod1Lower by remember { mutableStateOf(1) }
    var rod2Upper by remember { mutableStateOf(true) }
    var rod2Lower by remember { mutableStateOf(2) }
    var rod3Upper by remember { mutableStateOf(false) }
    var rod3Lower by remember { mutableStateOf(4) }
    val hundredsVal = (if (rod1Upper) 5 else 0) + rod1Lower
    val tensVal = (if (rod2Upper) 5 else 0) + rod2Lower
    val onesVal = (if (rod3Upper) 5 else 0) + rod3Lower
    val totalAbacusValue = hundredsVal * 100 + tensVal * 10 + onesVal

    val meetingCode = "SARA-SOROBAN-LIVE"
    val meetingDeepLink = "https://ais-dev-bioob4ko24mxi7yewjrudv-760922361558.europe-west2.run.app/?live=$meetingCode"

    val studentsList = listOf(
        Pair(stringResource(R.string.ui_f685e01), true), // hand raised
        Pair(stringResource(R.string.ui_8912465), false),
        Pair(stringResource(R.string.ui_80680b8), false),
        Pair(stringResource(R.string.ui_a1e2f3f), true),
        Pair(stringResource(R.string.ui_6b8e7a9), false),
        Pair(stringResource(R.string.ui_3227b46), false)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Meeting header
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = OrbitNavy,
                modifier = Modifier.fillMaxWidth().testTag("classroom_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (isMeetingConnected) OrbitEmerald else OrbitRose,
                            shape = CircleShape
                        ) {
                            Box(modifier = Modifier.size(10.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isMeetingConnected) "الفصل المباشر متصل: $meetingCode 🟢" else stringResource(R.string.ui_6b6a5bf),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = stringResource(R.string.ui_e35ce69),
                                color = OrbitGoldLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isHandRaised) {
                            Surface(
                                color = OrbitGold,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.ui_27f1406),
                                    color = OrbitNavyDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Invite Button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(stringResource(R.string.ui_6ebe7f1), meetingDeepLink)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, stringResource(R.string.ui_01d0d1d), Toast.LENGTH_SHORT).show()

                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "انضم الآن إلى حصة العداد الصيني المباشرة مع مس سارة العربي 🧮💡:\n$meetingDeepLink")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, stringResource(R.string.ui_8782f38)))
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .testTag("share_meeting_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.ui_0849cc9), tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Screen Share Mode Selector Tabs
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val modes = listOf(
                        Pair("abacus", stringResource(R.string.ui_57df5ed)),
                        Pair("slides", stringResource(R.string.ui_5ee8cfd)),
                        Pair("students", stringResource(R.string.ui_96fe532)),
                        Pair("camera", stringResource(R.string.ui_bfde3ef))
                    )

                    modes.forEach { (mode, label) ->
                        val isSelected = screenShareMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) OrbitIndigo else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    screenShareMode = mode
                                    if (!isScreenShared) viewModel.toggleClassroomScreenShare()
                                }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Virtual Stage
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Stage Screen (Interactive Abacus or Slides or Student Grid or Video)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isMeetingConnected) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CallEnd, contentDescription = null, tint = OrbitRose, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(R.string.ui_3a79635), color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { isMeetingConnected = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald)
                                ) {
                                    Text(stringResource(R.string.ui_6d82c46))
                                }
                            }
                        } else {
                            when (screenShareMode) {
                                "abacus" -> {
                                    // Interactive Virtual Soroban Abacus on the whiteboard!
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize().padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(color = OrbitEmerald, shape = CircleShape) {
                                                    Box(modifier = Modifier.size(8.dp))
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "مشاركة الشاشة: عداد السوروبان (الناتج: $totalAbacusValue)",
                                                    color = OrbitGoldLight,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Surface(
                                                color = Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.clickable {
                                                    rod1Upper = false; rod1Lower = 0
                                                    rod2Upper = false; rod2Lower = 0
                                                    rod3Upper = false; rod3Lower = 0
                                                }
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.ui_386c966),
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Soroban Abacus Frame (3 Rods)
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF334155),
                                            border = androidx.compose.foundation.BorderStroke(3.dp, OrbitGoldDark),
                                            modifier = Modifier.fillMaxWidth().height(145.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Rod 1: Hundreds
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(stringResource(R.string.ui_c39203f), fontSize = 9.sp, color = OrbitGoldLight)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 38.dp, height = 18.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(if (rod1Upper) OrbitGold else Color(0xFF64748B))
                                                            .clickable { rod1Upper = !rod1Upper },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("5", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(modifier = Modifier.width(44.dp).height(4.dp).background(Color.White))
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    (1..4).forEach { idx ->
                                                        val active = rod1Lower >= idx
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(vertical = 1.dp)
                                                                .size(width = 36.dp, height = 16.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (active) OrbitGold else Color(0xFF64748B))
                                                                .clickable { rod1Lower = if (rod1Lower == idx) idx - 1 else idx },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("1", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                        }
                                                    }
                                                    Text("$hundredsVal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Box(modifier = Modifier.width(2.dp).fillMaxHeight(0.8f).background(Color.White.copy(alpha = 0.2f)))

                                                // Rod 2: Tens
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(stringResource(R.string.ui_c93e60c), fontSize = 9.sp, color = OrbitGoldLight)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 38.dp, height = 18.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(if (rod2Upper) OrbitGold else Color(0xFF64748B))
                                                            .clickable { rod2Upper = !rod2Upper },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("5", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(modifier = Modifier.width(44.dp).height(4.dp).background(Color.White))
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    (1..4).forEach { idx ->
                                                        val active = rod2Lower >= idx
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(vertical = 1.dp)
                                                                .size(width = 36.dp, height = 16.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (active) OrbitGold else Color(0xFF64748B))
                                                                .clickable { rod2Lower = if (rod2Lower == idx) idx - 1 else idx },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("1", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                        }
                                                    }
                                                    Text("$tensVal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Box(modifier = Modifier.width(2.dp).fillMaxHeight(0.8f).background(Color.White.copy(alpha = 0.2f)))

                                                // Rod 3: Ones
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(stringResource(R.string.ui_0394c18), fontSize = 9.sp, color = OrbitGoldLight)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 38.dp, height = 18.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(if (rod3Upper) OrbitGold else Color(0xFF64748B))
                                                            .clickable { rod3Upper = !rod3Upper },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("5", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(modifier = Modifier.width(44.dp).height(4.dp).background(Color.White))
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    (1..4).forEach { idx ->
                                                        val active = rod3Lower >= idx
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(vertical = 1.dp)
                                                                .size(width = 36.dp, height = 16.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (active) OrbitGold else Color(0xFF64748B))
                                                                .clickable { rod3Lower = if (rod3Lower == idx) idx - 1 else idx },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("1", color = OrbitNavyDark, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                        }
                                                    }
                                                    Text("$onesVal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.ui_1fea341),
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                "slides" -> {
                                    // Presentation Slides
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Surface(color = OrbitIndigo, shape = RoundedCornerShape(8.dp)) {
                                            Text(stringResource(R.string.ui_fa36ae6), color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(stringResource(R.string.ui_2638928), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("+4 = +5 - 1\n+3 = +5 - 2\n+2 = +5 - 3\n+1 = +5 - 4", color = OrbitGoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(stringResource(R.string.ui_9165bab), color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                    }
                                }

                                "students" -> {
                                    // 6-student Camera Grid View
                                    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                        Text(stringResource(R.string.ui_01c41bc), color = OrbitGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val rows = studentsList.chunked(3)
                                        rows.forEach { row ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                row.forEach { (name, hand) ->
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color(0xFF334155),
                                                        modifier = Modifier.weight(1f).fillMaxHeight()
                                                    ) {
                                                        Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                                                            Column(
                                                                modifier = Modifier.align(Alignment.Center),
                                                                horizontalAlignment = Alignment.CenterHorizontally
                                                            ) {
                                                                Icon(Icons.Default.Face, contentDescription = null, tint = OrbitIndigoLight, modifier = Modifier.size(24.dp))
                                                                Text(name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                            if (hand) {
                                                                Text("✋", modifier = Modifier.align(Alignment.TopEnd), fontSize = 12.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                "camera" -> {
                                    // Teacher camera view
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        AsyncImage(
                                            model = OrbitRepository.INSTRUCTOR_SARA.avatarUrl,
                                            contentDescription = null,
                                            modifier = Modifier.size(72.dp).clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(stringResource(R.string.ui_dd37fd9), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(stringResource(R.string.ui_4e95506), color = OrbitGoldLight, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Controls Row (Audio, Video, Hand, Screen Share, End Call)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Toggle
                        IconButton(
                            onClick = { viewModel.toggleClassroomMute() },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) Color(0xFFEF4444) else Color(0xFF334155))
                                .testTag("classroom_mic_btn")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = stringResource(R.string.ui_072b93d),
                                tint = Color.White
                            )
                        }

                        // Video Toggle
                        IconButton(
                            onClick = { viewModel.toggleClassroomVideo() },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isVideoOn) Color(0xFF334155) else Color(0xFFEF4444))
                                .testTag("classroom_camera_btn")
                        ) {
                            Icon(
                                imageVector = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = stringResource(R.string.ui_1e8ec14),
                                tint = Color.White
                            )
                        }

                        // Raise Hand
                        IconButton(
                            onClick = { viewModel.toggleClassroomHandRaise() },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isHandRaised) OrbitGold else Color(0xFF334155))
                                .testTag("classroom_hand_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FrontHand,
                                contentDescription = stringResource(R.string.ui_95d6a9a),
                                tint = if (isHandRaised) OrbitNavyDark else Color.White
                            )
                        }

                        // Screen share toggle (CORRECTED to toggle screen share!)
                        IconButton(
                            onClick = {
                                viewModel.toggleClassroomScreenShare()
                                Toast.makeText(
                                    context,
                                    if (isScreenShared) stringResource(R.string.ui_c5dc7f0) else stringResource(R.string.ui_9e8640e),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isScreenShared) OrbitEmerald else Color(0xFF334155))
                                .testTag("classroom_screen_share_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenShare,
                                contentDescription = stringResource(R.string.ui_b0b5a2c),
                                tint = Color.White
                            )
                        }

                        // Leave / End Meeting
                        IconButton(
                            onClick = { isMeetingConnected = !isMeetingConnected },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626))
                                .testTag("classroom_call_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isMeetingConnected) Icons.Default.CallEnd else Icons.Default.Call,
                                contentDescription = stringResource(R.string.ui_9e960bd),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Sub Tabs: Live Chat vs Scheduled Sessions vs Participants
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = activeTab == "chat",
                    onClick = { activeTab = "chat" },
                    label = { Text("المحادثة والأسئلة (${messages.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrbitIndigo, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = activeTab == "participants",
                    onClick = { activeTab = "participants" },
                    label = { Text("الطلاب (${studentsList.size + 1})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrbitIndigo, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = activeTab == "schedule",
                    onClick = { activeTab = "schedule" },
                    label = { Text(stringResource(R.string.ui_81c9553), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrbitIndigo, selectedLabelColor = Color.White),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (activeTab) {
            "chat" -> {
                // Live Chat Messages
                items(messages) { msg ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.senderRole == "instructor") OrbitGold.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (msg.senderRole == "instructor") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(color = OrbitGold, shape = RoundedCornerShape(4.dp)) {
                                            Text(stringResource(R.string.ui_b5190a9), fontSize = 9.sp, color = OrbitNavyDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    if (msg.isQuestion) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(color = OrbitIndigo, shape = RoundedCornerShape(4.dp)) {
                                            Text(stringResource(R.string.ui_45c51e4), fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                }
                                Text(msg.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg.text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

                            if (msg.isQuestion) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { viewModel.upvoteClassroomQuestion(msg.id) }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = OrbitIndigo, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("أؤيد هذا السؤال (${msg.upvotes})", fontSize = 11.sp, color = OrbitIndigo, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Chat Input Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { viewModel.setClassroomChatText(it) },
                            placeholder = { Text(stringResource(R.string.ui_b989b59), fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f).testTag("classroom_chat_input"),
                            singleLine = true
                        )

                        IconButton(
                            onClick = { viewModel.sendClassroomMessage(isQuestion = askAsQuestion) },
                            enabled = chatInput.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (chatInput.isNotBlank()) OrbitIndigo else MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("classroom_send_msg_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = stringResource(R.string.ui_6dbd36c),
                                tint = if (chatInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            "participants" -> {
                // Teacher
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = OrbitRepository.INSTRUCTOR_SARA.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(OrbitRepository.INSTRUCTOR_SARA.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(stringResource(R.string.ui_8b9e2d3), fontSize = 11.sp, color = OrbitGoldDark)
                            }
                            Icon(Icons.Default.Mic, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Students
                items(studentsList) { (name, hand) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(34.dp).clip(CircleShape).background(OrbitIndigo.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = OrbitIndigo, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(stringResource(R.string.ui_6f73f74), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (hand) {
                                Text(stringResource(R.string.ui_1002eb6), fontSize = 11.sp, color = OrbitGoldDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            "schedule" -> {
                items(liveSessions) { session ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(session.courseTitle, fontSize = 11.sp, color = OrbitIndigo, fontWeight = FontWeight.Bold)
                                Surface(
                                    color = if (session.status == "live") Color(0xFFFEE2E2) else Color(0xFFEEF2FF),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (session.status == "live") stringResource(R.string.ui_6166f5f) else stringResource(R.string.ui_6003890),
                                        color = if (session.status == "live") OrbitRose else OrbitIndigo,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(session.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(session.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = OrbitGoldDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(session.scheduledAt, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("كود: ${session.meetingCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
