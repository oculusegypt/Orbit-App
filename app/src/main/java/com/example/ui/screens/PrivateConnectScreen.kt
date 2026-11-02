package com.example.ui.screens

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
import com.example.ui.screens.connect.*

@Composable
fun PrivateConnectScreen(viewModel: OrbitViewModel) {
    val users by viewModel.allRegisteredUsers.collectAsState()
    val currentAccount by viewModel.currentAccount.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Users Directory, 1: Call History

    val filteredUsers = remember(users, searchQuery, selectedRoleFilter) {
        users.filter { user ->
            val matchesQuery = searchQuery.isBlank() ||
                    user.displayName.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true) ||
                    user.phone.contains(searchQuery, ignoreCase = true) ||
                    user.bioOrGrade.contains(searchQuery, ignoreCase = true)

            val matchesRole = selectedRoleFilter == null || user.role == selectedRoleFilter
            matchesQuery && matchesRole
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("private_connect_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(OrbitIndigoDark, OrbitIndigo, Color(0xFF312E81))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = OrbitGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🔒 غرف الاتصال الخاص 1:1",
                                color = OrbitGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Real signaling status chip — incoming calls arrive only
                        // through genuine FCM push messages (no simulation).
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PhoneCallback,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = OrbitGold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "المكالمات الواردة تصل عبر إشعارات FCM الحقيقية فقط",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "الاتصال المباشر الخاص ومشاركة الشاشة",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تواصل صوت وصورة مع أي شخص بالاسم ومشاركة سبورة السوربان والشيتات التعليمية في الوقت الفعلي.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Fast VIP Banner for Miss Sara Al-Arabi
        val saraAccount = users.firstOrNull { it.role == UserRole.INSTRUCTOR }
        if (saraAccount != null && saraAccount.id != currentAccount?.id) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(2.dp, OrbitGold),
                    modifier = Modifier.fillMaxWidth().testTag("vip_sara_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            if (saraAccount.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = saraAccount.avatarUrl,
                                    contentDescription = saraAccount.displayName,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, OrbitGold, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(OrbitIndigo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                            }
                            // Real availability badge driven by the presence registry
                            val saraPresence = viewModel.presenceOf(saraAccount.id)
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (saraPresence) {
                                            com.example.core.UserPresence.ONLINE -> OrbitEmerald
                                            com.example.core.UserPresence.RECENTLY_ACTIVE -> OrbitGold
                                            com.example.core.UserPresence.OFFLINE -> Color.Gray
                                        }
                                    )
                                    .border(2.dp, Color.White, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(saraAccount.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⭐ المعلمة الأولى", fontSize = 10.sp, color = OrbitGold, fontWeight = FontWeight.Bold)
                            }
                            Text("مدربة السوربان والحساب الذهني - جاهزة للاستفسار والشرح", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.startPrivateCall(saraAccount, CallType.VIDEO_SCREEN) },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrbitGold, contentColor = OrbitNavyDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("btn_call_sara_video")
                                ) {
                                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اتصال فيديو وشاشة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.startPrivateCall(saraAccount, CallType.AUDIO) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("btn_call_sara_audio")
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اتصال صوتي", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab Selector (دليل المستخدمين الحقيقيين / سجل المكالمات)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = OrbitIndigo,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("connect_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("دليل الحسابات الحقيقية (${users.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("سجل المكالمات (${callHistory.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // Search Bar & Role Filter
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث بالاسم، البريد، أو رقم الهاتف...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_users")
                    )

                    // Role Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedRoleFilter == null,
                            onClick = { selectedRoleFilter = null },
                            label = { Text("الكل", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedRoleFilter == UserRole.INSTRUCTOR,
                            onClick = { selectedRoleFilter = UserRole.INSTRUCTOR },
                            label = { Text("المعلمات والمدربون 👩‍🏫", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedRoleFilter == UserRole.LEARNER,
                            onClick = { selectedRoleFilter = UserRole.LEARNER },
                            label = { Text("الطلاب وأولياء الأمور 🎒", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedRoleFilter == UserRole.ADMIN,
                            onClick = { selectedRoleFilter = UserRole.ADMIN },
                            label = { Text("الإدارة 🛡️", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Real User Account Cards
            items(filteredUsers, key = { it.id }) { user ->
                UserContactCard(
                    user = user,
                    isCurrentUser = user.id == currentAccount?.id,
                    onVideoCall = { viewModel.startPrivateCall(user, CallType.VIDEO_SCREEN) },
                    onAudioCall = { viewModel.startPrivateCall(user, CallType.AUDIO) },
                    onSwitchToUser = { viewModel.switchUserAccount(user) }
                )
            }

            if (filteredUsers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا يوجد مستخدمون مطابقون لبحثك", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            // Call History Tab
            if (callHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhoneMissed, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("لا توجد مكالمات سابقة حتى الآن", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ابدأ أول اتصال خاص صوت وصورة مع المعلمة أو زملائك وسيتم تسجيله هنا تلقائياً", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            } else {
                items(callHistory, key = { it.callId }) { call ->
                    CallHistoryCard(call = call)
                }
            }
        }
    }
}

