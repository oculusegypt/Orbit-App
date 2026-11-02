package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.ConsultationBooking
import com.example.data.repository.OrbitRepository
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitGold
import com.example.ui.theme.OrbitIndigo

@Composable
fun BookingConsultationDialog(
    currentUserId: String = "guest",
    currentUserName: String = "زائر",
    onBookingSaved: (ConsultationBooking) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var parentName by remember { mutableStateOf("") }
    var childName by remember { mutableStateOf("") }
    var childAge by remember { mutableStateOf("7") }
    var phone by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf("دبلوم الحساب الذهني والسوروبان") }
    var bookedSuccess by remember { mutableStateOf(false) }
    var savedBooking by remember { mutableStateOf<ConsultationBooking?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("booking_consultation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(OrbitGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = OrbitGold, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("حجز مقعد واستشارة مجانية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("مع أ / سارة العربي بالمنصورة وأونلاين", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                if (bookedSuccess) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 24.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("تم تسجيل طلب الحجز بنجاح! 🌟", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        savedBooking?.let { bk ->
                            Text(
                                text = "رقم مرجع الحجز: ${bk.id}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrbitIndigo,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Text(
                            text = "شكراً يا أ/ $parentName، ستتواصل مس سارة معك على الهاتف $phone لتحديد موعد اختبار تحديد المستوى للبطل $childName.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val url = "https://wa.me/201028340042?text=" + Uri.encode("مرحباً مس سارة العربي، أنا ولي أمر الطالب $childName وعمره $childAge سنة، حابب أستفسر عن $selectedService")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("متابعة المحادثة عبر واتساب مباشرة")
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { parentName = it },
                        label = { Text("اسم ولي الأمر") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = childName,
                            onValueChange = { childName = it },
                            label = { Text("اسم الطفل") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(2f).padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = childAge,
                            onValueChange = { childAge = it },
                            label = { Text("العمر") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف / واتساب") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("البرنامج المطلوب:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val services = listOf(
                        "دبلوم الحساب الذهني والسوروبان",
                        "تأسيس اللغة العربية ونور البيان",
                        "الرياضيات المدرسية الذكية",
                        "دبلوم إعداد المدربين (TOT)"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        services.forEach { s ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedService = s }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(selected = selectedService == s, onClick = { selectedService = s })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(s, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    validationError?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Button(
                        onClick = {
                            // Real validation: parent name, child name, and an
                            // Egyptian mobile number (11 digits starting with 01).
                            val trimmedPhone = phone.filter { it.isDigit() }
                            when {
                                parentName.isBlank() ->
                                    validationError = "من فضلك اكتب اسم ولي الأمر"
                                childName.isBlank() ->
                                    validationError = "من فضلك اكتب اسم الطفل"
                                trimmedPhone.length < 11 || !trimmedPhone.startsWith("01") ->
                                    validationError = "رقم الهاتف غير صحيح — مثال: 01028340042"
                                else -> {
                                    validationError = null
                                    // Persist the booking through the repository so it
                                    // reaches the instructor/admin dashboards + audit log.
                                    val booking = OrbitRepository.bookConsultation(
                                        parentName = parentName,
                                        childName = childName,
                                        childAge = childAge,
                                        phone = trimmedPhone,
                                        service = selectedService,
                                        actorUserId = currentUserId,
                                        actorName = currentUserName
                                    )
                                    savedBooking = booking
                                    bookedSuccess = true
                                    onBookingSaved(booking)
                                }
                            }
                        },
                        enabled = parentName.isNotBlank() && childName.isNotBlank() && phone.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("تأكيد حجز الاستشارة 💡", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
