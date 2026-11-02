package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.Course
import com.example.data.repository.OrbitRepository
import com.example.ui.components.MentalMathGame
import com.example.ui.theme.*
import com.example.ui.viewmodel.OrbitViewModel
import com.example.ui.viewmodel.PortalTab

@Composable
fun HomeScreen(
    viewModel: OrbitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val courses by viewModel.courses.collectAsState()
    val mentalMathDifficulty by viewModel.mentalMathDifficulty.collectAsState()
    val mentalMathGameState by viewModel.mentalMathGameState.collectAsState()
    val mentalMathCurrentNumber by viewModel.mentalMathCurrentNumber.collectAsState()
    val mentalMathSequence by viewModel.mentalMathSequence.collectAsState()
    val mentalMathUserAnswer by viewModel.mentalMathUserAnswer.collectAsState()
    val mentalMathIsCorrect by viewModel.mentalMathIsCorrect.collectAsState()
    val mentalMathStreak by viewModel.mentalMathStreak.collectAsState()
    val mentalMathXp by viewModel.mentalMathXp.collectAsState()

    val instructor = OrbitRepository.INSTRUCTOR_SARA

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Hero Banner
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    OrbitNavy,
                                    Color(0xFF312E81)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = OrbitGold.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "💡 المنصة المعتمدة لعام 2026",
                                    color = OrbitGoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = OrbitEmerald.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "المنصورة • أونلاين",
                                    color = OrbitEmeraldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "أكاديمية أ/ سارة العربي\nتأسيس أطفال . حساب ذهني",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "طريقة ممتعة وسهلة للأطفال لتنمية الذكاء الخارق وسرعة التركيز بالسوروبان والقرائية بالفهم مش الحفظ 🌟",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("4.98 ★", fontWeight = FontWeight.ExtraBold, color = OrbitGoldLight, fontSize = 16.sp)
                                Text("184+ تقييم معتمد", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("4 برامج", fontWeight = FontWeight.ExtraBold, color = OrbitGoldLight, fontSize = 16.sp)
                                Text("تأسيس وحساب ذهني", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("100%", fontWeight = FontWeight.ExtraBold, color = OrbitGoldLight, fontSize = 16.sp)
                                Text("شهادات بالباركود", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hero Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.setPortal(PortalTab.COURSES) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitGold, contentColor = OrbitNavyDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_explore_courses_btn")
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("استكشاف الدورات", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            FilledTonalButton(
                                onClick = { viewModel.setShowBookingDialog(true) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_book_seat_btn")
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حجز مقعد / استشارة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.setPortal(PortalTab.CONNECT) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrbitEmerald,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hero_connect_private_call_btn")
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اتصال خاص صوت وصورة ومشاركة شاشة 📞", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Offline Cache & Room Database Quick Action Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("offline_and_fcm_quick_bar")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(OrbitEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("التعلم متاح أوفلاين (Room DB)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("استأنف دروسك وتقدمك بدون اتصال بالإنترنت", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    courses.firstOrNull()?.let { firstCourse ->
                        Button(
                            onClick = { viewModel.resumeOfflineCourse(firstCourse) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("home_quick_resume_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استئناف ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Miss Sara Instructor Profile Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = instructor.avatarUrl,
                            contentDescription = instructor.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = instructor.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = OrbitIndigo, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = instructor.title,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = instructor.bio,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val url = "https://wa.me/201028340042?text=" + Uri.encode("مرحباً مس سارة العربي، حابب أستفسر عن دورات الأكاديمية للأطفال")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("واتساب مباشر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:01028340042"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اتصال: 01028340042", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Mental Math Interactive Game
        item {
            MentalMathGame(
                difficulty = mentalMathDifficulty,
                gameState = mentalMathGameState,
                currentNumber = mentalMathCurrentNumber,
                sequence = mentalMathSequence,
                userAnswer = mentalMathUserAnswer,
                isCorrect = mentalMathIsCorrect,
                streak = mentalMathStreak,
                xpScore = mentalMathXp,
                onSelectDifficulty = { viewModel.setMentalMathDifficulty(it) },
                onStartGame = { viewModel.startMentalMathChallenge() },
                onSubmitAnswer = { viewModel.submitMentalMathAnswer(it) }
            )
        }

        // Featured Courses Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أقوى البرامج التدريبية المعتمدة 🎓",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                TextButton(onClick = { viewModel.setPortal(PortalTab.COURSES) }) {
                    Text("عرض الكل", color = OrbitIndigo, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Course Cards Horizontal / List
        items(courses) { course ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openCourseDetail(course) }
                    .testTag("home_course_card_${course.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = course.thumbnailUrl,
                        contentDescription = course.title,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = OrbitIndigo.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = course.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrbitIndigo,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = course.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⭐ ${course.rating} (${course.reviewsCount})",
                                fontSize = 11.sp,
                                color = OrbitGoldDark,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = course.priceFormatted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrbitIndigo
                            )
                        }
                    }
                }
            }
        }

        // Live Classroom CTA Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setPortal(PortalTab.CLASSROOM) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = OrbitRose,
                                shape = CircleShape
                            ) {
                                Box(modifier = Modifier.size(8.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الفصل الافتراضي التفاعلي الحي", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrbitNavyDark)
                        }
                        Text(
                            text = "بث مباشر وشاشة عداد تفاعلية مع مس سارة والطلاب",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }

                    Button(
                        onClick = { viewModel.setPortal(PortalTab.CLASSROOM) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitNavy, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("دخول الفصل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
