package com.example.ui.screens.courses

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.*
import com.example.ui.components.CertificateCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CourseSubView
import com.example.ui.viewmodel.OrbitViewModel

@Composable
fun VerifierView(
    viewModel: OrbitViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verifierCode by viewModel.verifierCode.collectAsState()
    val verifiedCert by viewModel.verifiedCertificate.collectAsState()
    var inputCode by remember(verifierCode) { mutableStateOf(verifierCode) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                }
                Text("نظام التحقق من الشهادات المعتمدة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("أدخل كود التحقق الرقمي:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("يمكنك تجربة كود مس سارة الجاهز: CERT-2026-SOROBAN-001", fontSize = 11.sp, color = OrbitIndigo)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it },
                            placeholder = { Text("مثال: CERT-2026-...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = { viewModel.verifyCertificateInput(inputCode) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("تحقق")
                        }
                    }
                }
            }
        }

        if (verifiedCert != null) {
            item {
                Surface(
                    color = OrbitEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("شهادة أصلية معتمدة وموثقة رسمياً ✓", fontWeight = FontWeight.Bold, color = OrbitEmerald, fontSize = 14.sp)
                            Text("مسجلة في سجل الخريجين لعام 2026 لأكاديمية مس سارة العربي", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                CertificateCard(certificate = verifiedCert!!)
            }
        } else if (inputCode.isNotBlank()) {
            item {
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("لم يتم العثور على شهادة بهذا الكود. يرجى التحقق من الأحرف والأرقام.", fontSize = 12.sp, color = Color(0xFF991B1B))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
