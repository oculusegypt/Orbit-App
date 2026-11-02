package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Course
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitGold
import com.example.ui.theme.OrbitIndigo

@Composable
fun CheckoutDialog(
    course: Course,
    onConfirm: (Course) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("vodafone") }
    var couponCode by remember { mutableStateOf("") }
    var discountApplied by remember { mutableStateOf(false) }

    val basePrice = course.priceMinor / 100
    val finalPrice = if (discountApplied) (basePrice * 0.85).toInt() else basePrice

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("checkout_dialog")
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
                    Column {
                        Text("إتمام الاشتراك الفوري", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(course.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // Price Summary Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("المبلغ الإجمالي للاشتراك", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (discountApplied) {
                                Text("تم تطبيق خصم 15% (كود SARA2026)", fontSize = 10.sp, color = OrbitEmerald)
                            }
                        }
                        Text(
                            text = "$finalPrice ${course.currency}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrbitIndigo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Methods
                Text("طريقة الدفع المعتمدة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                val methods = listOf(
                    Triple("vodafone", "فودافون كاش ومحافظ المحمول (مصر)", Icons.Default.PhoneAndroid),
                    Triple("card", "البطاقات البنكية (Visa / MasterCard / Mada)", Icons.Default.CreditCard),
                    Triple("fawry", "منافذ فوري وأمان الفورية", Icons.Default.Storefront)
                )

                methods.forEach { (key, label, icon) ->
                    val isSelected = selectedMethod == key
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) OrbitIndigo.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, OrbitIndigo) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = key }
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = if (isSelected) OrbitIndigo else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Coupon row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = { couponCode = it },
                        placeholder = { Text("كود الخصم (جرب SARA2026)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (couponCode.trim().uppercase() == "SARA2026") {
                                discountApplied = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("تطبيق", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onConfirm(course) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_checkout_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تأكيد الاشتراك وتفعيل الدروس فوراً ✓", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
