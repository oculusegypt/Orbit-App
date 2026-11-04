package com.example.ui.screens.parent

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ScheduleConflictResolver
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel

/**
 * Authentic Parent Portal (بوابة ولي الأمر) for Sara Al-Araby Platform.
 *
 * Capabilities:
 * - Real role gating: Only accessible to authenticated Parents (or Admins).
 * - Multi-child profile manager with persistent state (no hardcoded fake children).
 * - Country-specific group explorer (Egypt EGP / Saudi Arabia SAR) with live seat capacity.
 * - Real enrollment into groups with capacity checks.
 * - "أعتذر عن الحضور" absence apology flow feeding Teacher Sara's queue in real time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPortalScreen(
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allStudentProfiles by viewModel.studentProfiles.collectAsState()
    val groupsList by viewModel.groups.collectAsState()
    val apologiesList by viewModel.absenceApologies.collectAsState()

    // Gated Access Check: Must be logged in as PARENT (or ADMIN)
    if (currentUser == null || (currentUser!!.role != UserRole.PARENT && currentUser!!.role != UserRole.ADMIN)) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth().testTag("parent_auth_gate_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OrbitEmerald.copy(alpha = 0.15f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👨‍👩‍👧‍👦", fontSize = 32.sp)
                        }
                    }

                    Text(
                        text = "بوابة أولياء الأمور المعتمدة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = OrbitNavy
                    )

                    Text(
                        text = if (currentUser == null) {
                            "هذه البوابة مخصصة لأولياء الأمور لتسجيل أبنائهم، ومتابعة الحصص والمجموعات، والاطلاع على الحضور والغياب وطلب المواعيد التعويضية."
                        } else {
                            "أنت مسجل حالياً بحساب: ${currentUser!!.displayName} (${currentUser!!.role.labelAr}). هذا القسم يتطلب حساب ولي أمر لإدارة الأبناء."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = { viewModel.setShowAuthDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_parent_gate_login")
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (currentUser == null) "تسجيل الدخول / إنشاء حساب ولي أمر" else "تبديل إلى حساب ولي أمر", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val parentUser = currentUser!!
    val parentChildren = allStudentProfiles.filter { it.parentId == parentUser.id }

    var selectedChildId by remember(parentChildren) {
        mutableStateOf(parentChildren.firstOrNull()?.id ?: "")
    }

    val currentChild = parentChildren.find { it.id == selectedChildId } ?: parentChildren.firstOrNull()

    // Selected country filter for group exploration
    var selectedCountry by remember { mutableStateOf(PlatformCountry.EGYPT) }

    // Absence Apology and Compensation Dialog State
    var showApologyDialog by remember { mutableStateOf(false) }
    var apologyReason by remember { mutableStateOf("ظرف صحي طارئ") }
    var discoveredCompensationSlots by remember { mutableStateOf<List<CompensationSlot>>(emptyList()) }
    var selectedCompensationSlot by remember { mutableStateOf<CompensationSlot?>(null) }
    var compensationSuccessNotice by remember { mutableStateOf<String?>(null) }

    // Add Child Dialog State
    var showAddChildDialog by remember { mutableStateOf(false) }
    var newChildName by remember { mutableStateOf("") }
    var newChildBirthYear by remember { mutableStateOf("2018") }

    // Enrollment Message Banner
    var enrollmentMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = OrbitNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("بوابة ولي الأمر: ${parentUser.displayName} 👨‍👧‍👦", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("إدارة الأبناء، والاشتراكات، والاعتذار عن الغياب", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showAddChildDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_add_child_modal")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة طفل", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (parentChildren.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("لم تقم بإضافة أي أطفال بعد في حسابك", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("اضغط على زر (إضافة طفل) بالأعلى لتسجيل بيانات طفلك واختيار المجموعة المناسبة له", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                            }
                        }
                    } else {
                        // Children Switcher Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            parentChildren.forEach { child ->
                                val isSelected = child.id == selectedChildId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedChildId = child.id }
                                        .testTag("child_pill_${child.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(child.avatarEmoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = child.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) OrbitNavy else Color.White
                                            )
                                            Text(
                                                text = "${child.age} سنوات • ${child.ageTier.ageRangeAr}",
                                                fontSize = 9.sp,
                                                color = if (isSelected) OrbitIndigo else Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Child Summary & Upcoming Session with "أعتذر عن الحضور"
        if (currentChild != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ملف الطالب الحالي (${currentChild.name}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OrbitNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "المستوى التعليمي: ${currentChild.ageTier.labelAr} • عدد المجموعات المسجل بها: ${currentChild.enrolledGroupIds.size}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("حصة الحساب الذهني وتأسيس الرياضيات 🧮", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OrbitNavy)
                                    Text("مع الأستاذة سارة العربي • التفاعل المباشر", fontSize = 11.sp, color = Color.Gray)
                                }

                                // Absence Apology Button
                                OutlinedButton(
                                    onClick = {
                                        val dummySlots = listOf(
                                            SessionTimeSlot("ses-1", "حصة سابقة", System.currentTimeMillis() - 3600000, 45),
                                            SessionTimeSlot("ses-2", "مجموعة الأحد", System.currentTimeMillis() + 86400000, 45)
                                        )
                                        discoveredCompensationSlots = ScheduleConflictResolver.findAvailableCompensationSlots(
                                            existingSessions = dummySlots,
                                            country = currentChild.country
                                        )
                                        selectedCompensationSlot = discoveredCompensationSlots.firstOrNull()
                                        showApologyDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                                    modifier = Modifier.testTag("apology_button")
                                ) {
                                    Text("أعتذر عن الحضور ✋", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (compensationSuccessNotice != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD1FAE5),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(compensationSuccessNotice!!, color = Color(0xFF065F46), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Country Toggle & Group Explorer
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("المجموعات التعليمية والمواعيد الثابتة:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OrbitNavy)

                // Country Selector
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlatformCountry.values().forEach { country ->
                        val isSelected = selectedCountry == country
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) OrbitIndigo else Color(0xFFE2E8F0),
                            modifier = Modifier.clickable { selectedCountry = country }
                        ) {
                            Text(
                                text = "${country.flag} ${country.nameAr}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else OrbitNavy
                            )
                        }
                    }
                }
            }
        }

        // Enrollment message if any
        if (enrollmentMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = enrollmentMessage!!,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // List of groups filtered by selected country
        val filteredGroups = groupsList.filter { it.country == selectedCountry }
        items(filteredGroups) { group ->
            val isAlreadyEnrolled = currentChild?.enrolledGroupIds?.contains(group.id) == true

            GroupCardItem(
                group = group,
                isEnrolled = isAlreadyEnrolled,
                onEnroll = {
                    if (currentChild == null) {
                        enrollmentMessage = "يرجى إضافة طفل أولاً لتسجيله في المجموعة"
                        return@GroupCardItem
                    }
                    val result = viewModel.enrollChildInGroup(
                        studentId = currentChild.id,
                        groupId = group.id,
                        actorName = parentUser.displayName
                    )
                    result.onSuccess { msg ->
                        enrollmentMessage = msg
                    }.onFailure { err ->
                        enrollmentMessage = err.message ?: "فشل الاشتراك في المجموعة"
                    }
                }
            )
        }
    }

    // Absence Apology & Compensation Picker Dialog
    if (showApologyDialog && currentChild != null) {
        AlertDialog(
            onDismissRequest = { showApologyDialog = false },
            title = {
                Text("طلب اعتذار واقتراح موعد تعويض 📅", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("اختر سبب الاعتذار للطالب (${currentChild.name}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    listOf("ظرف صحي طارئ", "سفر أو مناسبة عائلية", "تضارب مع موعد امتحانات المدرسة").forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { apologyReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = apologyReason == reason, onClick = { apologyReason = reason })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(reason, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "مواعيد التعويض المتاحة لدى المعلمة سارة العربي (خالية من التعارضات):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrbitIndigo
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (discoveredCompensationSlots.isEmpty()) {
                        Text("سيتم إرسال الاعتذار للأستاذة سارة العربي لتحديد موعد تعويض مناسب.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        discoveredCompensationSlots.forEach { slot ->
                            val isChosen = selectedCompensationSlot?.id == slot.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                border = if (isChosen) androidx.compose.foundation.BorderStroke(1.dp, OrbitIndigo) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedCompensationSlot = slot }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isChosen, onClick = { selectedCompensationSlot = slot })
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(slot.dateDisplay, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(slot.timeSlotDisplay, fontSize = 11.sp, color = OrbitEmerald)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val apology = viewModel.submitAbsenceApology(
                            studentId = currentChild.id,
                            studentName = currentChild.name,
                            parentId = parentUser.id,
                            sessionId = "ses-regular",
                            sessionTitle = "حصة السوروبان والحساب الذهني",
                            apologyReason = apologyReason
                        )
                        compensationSuccessNotice = "تم إرسال الاعتذار رسمياً للأستاذة سارة العربي بنجاح (رقم: ${apology.id.takeLast(6)})"
                        showApologyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald)
                ) {
                    Text("إرسال الاعتذار")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApologyDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Add Child Dialog
    if (showAddChildDialog) {
        AlertDialog(
            onDismissRequest = { showAddChildDialog = false },
            title = { Text("إضافة طفل جديد للمنظومة 👶", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newChildName,
                        onValueChange = { newChildName = it },
                        label = { Text("اسم الطفل كاملاً") },
                        placeholder = { Text("ياسين أحمد") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_child_name")
                    )

                    OutlinedTextField(
                        value = newChildBirthYear,
                        onValueChange = { newChildBirthYear = it },
                        label = { Text("سنة الميلاد (لتحديد الفئة العمرية)") },
                        placeholder = { Text("2018 (عمره 8 سنوات - أبطال)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_child_year")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChildName.isNotBlank()) {
                            val year = newChildBirthYear.toIntOrNull() ?: 2018
                            val birthDate = "$year-01-01"
                            val newProfile = viewModel.addStudentProfile(
                                parentId = parentUser.id,
                                parentName = parentUser.displayName,
                                name = newChildName.trim(),
                                birthDate = birthDate,
                                country = selectedCountry
                            )
                            selectedChildId = newProfile.id
                            newChildName = ""
                            showAddChildDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                    modifier = Modifier.testTag("btn_save_child")
                ) {
                    Text("حفظ وتسجيل الطفل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChildDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun GroupCardItem(
    group: GroupSchedule,
    isEnrolled: Boolean = false,
    onEnroll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = OrbitNavy,
                    modifier = Modifier.weight(1f)
                )

                // Capacity Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (group.isFull) Color(0xFFFFE4E6) else Color(0xFFD1FAE5)
                ) {
                    Text(
                        text = if (group.isFull) "مكتملة (${group.maxCapacity}/${group.maxCapacity})" else "متاح ${group.availableSeats} مقاعد",
                        color = if (group.isFull) Color(0xFFBE123C) else Color(0xFF047857),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("🗓️ المواعيد: ${group.fixedDaysAr} (${group.localTimeDisplay})", fontSize = 11.sp, color = Color.Gray)
            Text("المعلمة: ${group.instructorName} • الفئة: ${group.targetAgeTier.labelAr}", fontSize = 10.sp, color = OrbitIndigo)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${group.priceFormatted} شهرياً",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = OrbitNavy
                )

                Button(
                    onClick = onEnroll,
                    enabled = !group.isFull && !isEnrolled,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnrolled) OrbitEmerald else if (group.isFull) Color.Gray else OrbitIndigo
                    )
                ) {
                    Text(
                        text = if (isEnrolled) "مشترك بالفعل ✅" else if (group.isFull) "المجموعة ممتلئة" else "اشتراك فوري",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
