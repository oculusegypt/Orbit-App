package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.data.repository.OrbitRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitGold
import com.example.ui.theme.OrbitIndigo
import com.example.ui.theme.OrbitNavy
import com.example.ui.viewmodel.OrbitViewModel

@Composable
fun AdminScreen(
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val enrollments by viewModel.enrollments.collectAsState()
    val certificates by viewModel.certificates.collectAsState()
    val consultationBookings by OrbitRepository.consultationBookings.collectAsState()

    // Real revenue computed from the course catalog × paid enrollments (EGP)
    val totalRevenueEgp = remember(courses, enrollments) {
        val priceById = courses.associate { it.id to it.priceMinor }
        enrollments
            .filter { it.source == "purchase" }
            .sumOf { priceById[it.courseId] ?: 0 } / 100
    }
    val pendingBookings = consultationBookings.count { it.status == "PENDING" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Admin Operations Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = OrbitNavy),
                modifier = Modifier.fillMaxWidth().testTag("admin_banner")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = OrbitEmerald, shape = CircleShape) {
                                Box(modifier = Modifier.size(10.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.ui_8c1ea38), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Surface(color = OrbitEmerald.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                            Text(stringResource(R.string.ui_3a78740), color = OrbitEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.ui_d17052a),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.ui_33a72bd), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("46,000 EGP", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OrbitEmerald)
                        Text(stringResource(R.string.ui_09c07c0), fontSize = 9.sp, color = OrbitEmerald)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.ui_19e6dfd), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${certificates.size} شهادة", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OrbitGold)
                        Text(stringResource(R.string.ui_6d9a2be), fontSize = 9.sp, color = OrbitGold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.ui_60e2c73), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${courses.size} دورات", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OrbitIndigo)
                        Text(stringResource(R.string.ui_7d2139c), fontSize = 9.sp, color = OrbitIndigo)
                    }
                }
            }
        }

        // Real Audit Log Ledger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.ui_919cab9), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${auditLogs.size} عملية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        items(auditLogs) { log ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (log.action) {
                                "CERTIFICATE_ISSUED" -> OrbitGold.copy(alpha = 0.2f)
                                "COURSE_PUBLISHED" -> OrbitIndigo.copy(alpha = 0.15f)
                                "SUBMISSION_GRADED" -> OrbitEmerald.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = log.action,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (log.action) {
                                    "CERTIFICATE_ISSUED" -> OrbitGold
                                    "COURSE_PUBLISHED" -> OrbitIndigo
                                    "SUBMISSION_GRADED" -> OrbitEmerald
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(log.createdAt, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(log.reason, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "المنفّذ: ${log.actorName} (${log.actorUserId}) • المصدر: ${log.resourceType}:${log.resourceId}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
