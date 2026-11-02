package com.example.ui.components

import androidx.compose.ui.res.stringResource
import com.example.R

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
import com.example.ui.theme.OrbitIndigo
import com.example.ui.theme.OrbitRose
import com.example.ui.viewmodel.CheckoutPricing

/**
 * Real checkout funnel. Pricing, coupon validation (signature + expiry +
 * per-user quota) and receipts live in CheckoutController behind the
 * ViewModel; this composable only renders state and forwards intents.
 */
@Composable
fun CheckoutDialog(
    course: Course,
    pricing: CheckoutPricing?,
    couponFeedback: String?,
    onApplyCoupon: (String) -> Unit,
    onConfirm: (Course, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("vodafone") }
    var couponCode by remember { mutableStateOf("") }

    val basePriceMinor = pricing?.basePriceMinor ?: course.priceMinor
    val finalPriceMinor = pricing?.finalPriceMinor ?: basePriceMinor
    val discountPercent = pricing?.discountPercent ?: 0
    val hasDiscount = pricing?.hasDiscount == true

    val methods = listOf(
        Triple("vodafone", stringResource(R.string.ui_5d1b8c5), Icons.Default.PhoneAndroid),
        Triple("card", stringResource(R.string.ui_cd1171c), Icons.Default.CreditCard),
        Triple("fawry", stringResource(R.string.ui_d9f5d85), Icons.Default.Storefront)
    )

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
                        Text(stringResource(R.string.ui_0122c11), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(course.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ui_9932cca))
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // Price Summary Card — computed by the real pricing engine
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
                            Text(stringResource(R.string.ui_7f6ea07), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (hasDiscount) {
                                Text(
                                    "السعر قبل الخصم: ${basePriceMinor / 100} ${course.currency} (-$discountPercent%)",
                                    fontSize = 10.sp,
                                    color = OrbitEmerald
                                )
                            } else {
                                Text(stringResource(R.string.ui_7ae9ab1), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = "${finalPriceMinor / 100} ${course.currency}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrbitIndigo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Methods
                Text(stringResource(R.string.ui_16d24e5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

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

                // Coupon row — validated by the signed-campaign engine
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = { couponCode = it },
                        placeholder = { Text(stringResource(R.string.ui_71ae3ba), fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = { onApplyCoupon(couponCode) },
                        enabled = couponCode.isNotBlank() && !hasDiscount,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("apply_coupon_btn")
                    ) {
                        Text(if (hasDiscount) stringResource(R.string.ui_5019eb3) else stringResource(R.string.ui_abe3151), fontSize = 12.sp)
                    }
                }

                // Real validation feedback from the engine
                couponFeedback?.let { message ->
                    Text(
                        text = message,
                        fontSize = 11.sp,
                        color = if (hasDiscount) OrbitEmerald else OrbitRose,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .testTag("coupon_feedback_text")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onConfirm(course, selectedMethod) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_checkout_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ui_a0c450e), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Text(
                    stringResource(R.string.ui_1294f58),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
