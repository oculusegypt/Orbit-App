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
    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Signup, 2: Quick Switch
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
    var signupRole by remember { mutableStateOf(UserRole.LEARNER) }
    var signupBioOrGrade by remember { mutableStateOf("") }

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
                            text = when (selectedTab) {
                                0 -> stringResource(R.string.ui_cf06700)
                                1 -> stringResource(R.string.ui_6246c22)
                                else -> stringResource(R.string.ui_ac4aa45)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = stringResource(R.string.ui_8cacfdd),
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
                        text = { Text(stringResource(R.string.ui_269168c), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                            successMessage = null
                        },
                        text = { Text(stringResource(R.string.ui_31c5d1a), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            errorMessage = null
                            successMessage = null
                        },
                        text = { Text("الحسابات (${allRegisteredUsers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
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
                    when (selectedTab) {
                        0 -> {
                            // LOGIN TAB
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it },
                                    label = { Text(stringResource(R.string.ui_2436aac)) },
                                    placeholder = { Text("example@orbitlearn.edu") },
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
                                    placeholder = { Text("••••••••") },
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
                                            errorMessage = stringResource(R.string.ui_ccf9734)
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
                                    Text(stringResource(R.string.ui_83ed06b), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                Text(stringResource(R.string.ui_a2e8ac6), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                allRegisteredUsers.take(3).forEach { user ->
                                    ElevatedCard(
                                        onClick = {
                                            onSelectUser(user.toUser())
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = OrbitIndigo)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(user.role.labelAr + " • " + user.email, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // SIGN UP TAB (Real Accounts)
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(stringResource(R.string.ui_98fd1d4), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        UserRole.LEARNER to stringResource(R.string.ui_c1c4111),
                                        UserRole.INSTRUCTOR to stringResource(R.string.ui_80900fd),
                                        UserRole.ADMIN to stringResource(R.string.ui_f0976b3)
                                    ).forEach { (role, label) ->
                                        FilterChip(
                                            selected = signupRole == role,
                                            onClick = { signupRole = role },
                                            label = { Text(label, fontSize = 11.sp, fontWeight = if (signupRole == role) FontWeight.Bold else FontWeight.Normal) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = when (role) {
                                                    UserRole.INSTRUCTOR -> OrbitGold.copy(alpha = 0.3f)
                                                    UserRole.ADMIN -> Color(0xFFFEE2E2)
                                                    UserRole.LEARNER -> OrbitIndigo.copy(alpha = 0.2f)
                                                }
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = signupName,
                                    onValueChange = { signupName = it },
                                    label = { Text(stringResource(R.string.ui_a92ebc2)) },
                                    placeholder = { Text(stringResource(R.string.ui_445f3f1)) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_name")
                                )

                                OutlinedTextField(
                                    value = signupEmail,
                                    onValueChange = { signupEmail = it },
                                    label = { Text(stringResource(R.string.ui_b09228f)) },
                                    placeholder = { Text("ahmed@example.com") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_email")
                                )

                                OutlinedTextField(
                                    value = signupPhone,
                                    onValueChange = { signupPhone = it },
                                    label = { Text(stringResource(R.string.ui_18bd813)) },
                                    placeholder = { Text("+20 10 1234 5678") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_phone")
                                )

                                OutlinedTextField(
                                    value = signupPassword,
                                    onValueChange = { signupPassword = it },
                                    label = { Text(stringResource(R.string.ui_bcb75ee)) },
                                    placeholder = { Text(stringResource(R.string.ui_5c83dee)) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_password")
                                )

                                OutlinedTextField(
                                    value = signupBioOrGrade,
                                    onValueChange = { signupBioOrGrade = it },
                                    label = { Text(if (signupRole == UserRole.LEARNER) stringResource(R.string.ui_aad225d) else stringResource(R.string.ui_2c83545)) },
                                    placeholder = { Text(if (signupRole == UserRole.LEARNER) stringResource(R.string.ui_77bd4f8) else stringResource(R.string.ui_a231de9)) },
                                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_bio")
                                )

                                Button(
                                    onClick = {
                                        if (signupName.isBlank() || signupEmail.isBlank() || signupPassword.isBlank()) {
                                            errorMessage = stringResource(R.string.ui_10bc485)
                                            return@Button
                                        }
                                        onRegister(signupName, signupEmail, signupPassword, signupRole, signupPhone, signupBioOrGrade) { success, msg ->
                                            if (success) {
                                                successMessage = stringResource(R.string.ui_ae7b64b)
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
                                    Text(stringResource(R.string.ui_b597ead), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        2 -> {
                            // REGISTERED ACCOUNTS LIST
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.ui_526c07b), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                allRegisteredUsers.forEach { user ->
                                    val isSelected = currentUser?.id == user.id
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) OrbitIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, OrbitIndigo) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectUser(user.toUser())
                                                onDismiss()
                                            }
                                            .padding(vertical = 2.dp)
                                            .testTag("auth_account_${user.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (user.avatarUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = user.avatarUrl,
                                                    contentDescription = user.displayName,
                                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(OrbitIndigo),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = when (user.role) {
                                                            UserRole.INSTRUCTOR -> OrbitGold.copy(alpha = 0.2f)
                                                            UserRole.ADMIN -> Color(0xFFFEE2E2)
                                                            UserRole.LEARNER -> OrbitEmerald.copy(alpha = 0.2f)
                                                        },
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = user.role.labelAr,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (user.role) {
                                                                UserRole.INSTRUCTOR -> OrbitGold
                                                                UserRole.ADMIN -> Color(0xFFDC2626)
                                                                UserRole.LEARNER -> OrbitEmerald
                                                            },
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Text(user.email, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (user.bioOrGrade.isNotBlank()) {
                                                    Text(user.bioOrGrade, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }

                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OrbitIndigo)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        onSelectUser(null)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("auth_logout_btn")
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.ui_e8e2498), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
