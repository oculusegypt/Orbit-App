package com.example.ui.components

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.UserAccount
import com.example.data.local.toUser
import com.example.data.models.User
import com.example.data.models.UserRole
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitGold
import com.example.ui.theme.OrbitIndigo

@Composable
fun AuthDialog(
    currentUser: User?,
    allRegisteredUsers: List<UserAccount>,
    onSelectUser: (User?) -> Unit,
    onLogin: (email: String, pass: String, callback: (Boolean, String) -> Unit) -> Unit,
    onRegister: (name: String, email: String, pass: String, role: UserRole, phone: String, bio: String, callback: (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(if (currentUser != null) 0 else 0) } // 0: Login, 1: Signup
    val context = androidx.compose.ui.platform.LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Login state
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Signup state
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPhone by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupRole by remember { mutableStateOf(UserRole.PARENT) }
    var signupCountry by remember { mutableStateOf("مصر (EGP)") }
    var signupExtraInfo by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedTab == 0) "تسجيل الدخول الفعلي" else "إنشاء حساب جديد بالمنصة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "منظومة سارة العربي للتعليم الإلكتروني — حسابات حقيقية وفصل للأدوار",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ui_9932cca))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = OrbitIndigo,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                            successMessage = null
                        },
                        text = { Text("تسجيل الدخول", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                            successMessage = null
                        },
                        text = { Text("إنشاء حساب", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Error / Success banners
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFDC2626),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = successMessage!!,
                            color = Color(0xFF16A34A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (currentUser != null) {
                        // Current Active Session banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = OrbitIndigo.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OrbitIndigo.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).clip(CircleShape).background(OrbitIndigo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("أنت مسجل حالياً باسم:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currentUser.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${currentUser.role.labelAr} • ${currentUser.email}", fontSize = 10.sp, color = OrbitIndigo, fontWeight = FontWeight.SemiBold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        onSelectUser(null)
                                        successMessage = "تم تسجيل الخروج بنجاح"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("auth_logout_btn")
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تسجيل خروج", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    when (selectedTab) {
                        0 -> {
                            // REAL CREDENTIALS LOGIN TAB
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "أدخل بيانات حسابك المعتمد:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it },
                                    label = { Text("البريد الإلكتروني") },
                                    placeholder = { Text("Saraelarby96@gmail.com أو بريدك المسجل") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_login_email")
                                )

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    label = { Text(stringResource(R.string.ui_bcb75ee)) },
                                    placeholder = { Text("كلمة المرور المشفرة") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_login_password")
                                )

                                Button(
                                    onClick = {
                                        if (loginEmail.isBlank() || loginPassword.isBlank()) {
                                            errorMessage = "يرجى كتابة البريد الإلكتروني وكلمة المرور للتحقق"
                                            return@Button
                                        }
                                        onLogin(loginEmail, loginPassword) { success, msg ->
                                            if (success) {
                                                onDismiss()
                                            } else {
                                                errorMessage = msg
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_submit_login")
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تسجيل الدخول والتحقق", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "معلومات الحسابات الرسمية بالمنظومة:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "• حساب المعلمة سارة العربي: Saraelarby96@gmail.com\n• أولياء الأمور والطلاب: يتم التسجيل من تبويب (إنشاء حساب) لحفظ بيانات حقيقية.",
                                            fontSize = 10.sp,
                                            lineHeight = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        1 -> {
                            // REAL USER REGISTRATION TAB
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("اختر نوع الحساب المطلوب إنشاؤه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        UserRole.PARENT to "ولي أمر 👨‍👩‍👧",
                                        UserRole.STUDENT to "طالب 🧒",
                                        UserRole.INSTRUCTOR to "معلمة 👩‍🏫"
                                    ).forEach { (role, label) ->
                                        FilterChip(
                                            selected = signupRole == role,
                                            onClick = { signupRole = role },
                                            label = { Text(label, fontSize = 11.sp, fontWeight = if (signupRole == role) FontWeight.Bold else FontWeight.Normal) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = when (role) {
                                                    UserRole.INSTRUCTOR -> OrbitGold.copy(alpha = 0.3f)
                                                    UserRole.PARENT -> OrbitEmerald.copy(alpha = 0.25f)
                                                    UserRole.STUDENT -> OrbitIndigo.copy(alpha = 0.25f)
                                                    else -> OrbitIndigo.copy(alpha = 0.2f)
                                                }
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = signupName,
                                    onValueChange = { signupName = it },
                                    label = { Text(if (signupRole == UserRole.PARENT) "اسم ولي الأمر كاملاً" else "الاسم الكامل") },
                                    placeholder = { Text(if (signupRole == UserRole.PARENT) "أحمد محمد محمود" else "ياسين أحمد") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_name")
                                )

                                OutlinedTextField(
                                    value = signupEmail,
                                    onValueChange = { signupEmail = it },
                                    label = { Text("البريد الإلكتروني الحقيقي") },
                                    placeholder = { Text("example@gmail.com") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_email")
                                )

                                OutlinedTextField(
                                    value = signupPhone,
                                    onValueChange = { signupPhone = it },
                                    label = { Text("رقم الهاتف / واتساب") },
                                    placeholder = { Text("+20 10 2834 0042") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_phone")
                                )

                                OutlinedTextField(
                                    value = signupPassword,
                                    onValueChange = { signupPassword = it },
                                    label = { Text("كلمة المرور (تُحفظ مشفرة في SQLite)") },
                                    placeholder = { Text("6 أحرف أو أرقام على الأقل") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_password")
                                )

                                OutlinedTextField(
                                    value = signupExtraInfo,
                                    onValueChange = { signupExtraInfo = it },
                                    label = {
                                        Text(
                                            when (signupRole) {
                                                UserRole.PARENT -> "الدولة والمدينة (مثال: مصر - المنصورة)"
                                                UserRole.STUDENT -> "العمر أو المرحلة الدراسية (مثال: 8 سنوات - أبطال سارة)"
                                                else -> "المؤهل والتخصص"
                                            }
                                        )
                                    },
                                    placeholder = { Text("بيانات لتحديد البرنامج والعملة المناسبة") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_bio")
                                )

                                Button(
                                    onClick = {
                                        if (signupName.isBlank() || signupEmail.isBlank() || signupPassword.isBlank() || signupPhone.isBlank()) {
                                            errorMessage = "يرجى تعبئة كافة الحقول الأساسية للتسجيل الفعلي"
                                            return@Button
                                        }
                                        if (signupPassword.length < 5) {
                                            errorMessage = "كلمة المرور يجب ألا تقل عن 5 خانات"
                                            return@Button
                                        }
                                        onRegister(signupName, signupEmail, signupPassword, signupRole, signupPhone, signupExtraInfo) { success, msg ->
                                            if (success) {
                                                successMessage = "تم إنشاء الحساب وحفظه في قاعدة البيانات بنجاح"
                                                onDismiss()
                                            } else {
                                                errorMessage = msg
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_submit_signup")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("إنشاء الحساب وحفظه", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
