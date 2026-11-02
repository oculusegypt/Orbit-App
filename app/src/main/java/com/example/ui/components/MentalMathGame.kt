package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MentalMathDifficulty
import com.example.ui.viewmodel.MentalMathGameState

@Composable
fun MentalMathGame(
    difficulty: MentalMathDifficulty,
    gameState: MentalMathGameState,
    currentNumber: Int?,
    sequence: List<Int>,
    userAnswer: String,
    isCorrect: Boolean?,
    streak: Int,
    xpScore: Int,
    onSelectDifficulty: (MentalMathDifficulty) -> Unit,
    onStartGame: () -> Unit,
    onSubmitAnswer: (String) -> Unit
) {
    var inputBuffer by remember(gameState) { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mental_math_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Title & Badges
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
                            .background(OrbitGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = OrbitGoldDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "تحدي الحساب الذهني التفاعلي 🧮",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "سرعة البديهة والخرزات السحرية بدون آلة حاسبة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stats: Streak & XP
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = OrbitGoldDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("$streak", fontWeight = FontWeight.Bold, color = OrbitGoldDark, fontSize = 12.sp)
                        }
                    }

                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = OrbitIndigo, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("$xpScore XP", fontWeight = FontWeight.Bold, color = OrbitIndigo, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Difficulty Chips (Single choice)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MentalMathDifficulty.values().forEach { diff ->
                    val isSelected = difficulty == diff
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectDifficulty(diff) },
                        label = {
                            Text(
                                text = diff.labelAr,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrbitIndigo,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Interactive Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                OrbitNavy,
                                Color(0xFF1E293B)
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (gameState) {
                    MentalMathGameState.IDLE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = OrbitGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ستظهر أرقام سريعة بالترتيب.. احسب مجموعها بذهنك!",
                                color = Color.White,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onStartGame,
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitGold, contentColor = OrbitNavyDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("start_math_challenge_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ابدأ التحدي الآن", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    MentalMathGameState.PLAYING -> {
                        AnimatedContent(
                            targetState = currentNumber,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(150)).togetherWith(fadeOut(animationSpec = tween(150)))
                            },
                            label = "NumberFlash"
                        ) { num ->
                            if (num != null) {
                                Text(
                                    text = if (num > 0) "+$num" else "$num",
                                    color = if (num > 0) OrbitGoldLight else OrbitRose,
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "...",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    MentalMathGameState.ANSWERING -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ما هو الناتج النهائي؟",
                                color = OrbitGoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.width(160.dp)
                            ) {
                                Text(
                                    text = if (inputBuffer.isEmpty()) "؟" else inputBuffer,
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    MentalMathGameState.RESULT -> {
                        val correct = isCorrect == true
                        val expectedSum = sequence.sum()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (correct) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (correct) OrbitEmerald else OrbitRose,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (correct) "إجابة عبقرية صحيحة! 🎉" else "محاولة جيدة يا بطل!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "المسألة: ${sequence.joinToString(" ")} = $expectedSum",
                                color = OrbitGoldLight,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onStartGame,
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitGold, contentColor = OrbitNavyDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("play_again_math_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تحدي جديد")
                            }
                        }
                    }
                }
            }

            // Keypad for children in Answering Mode
            if (gameState == MentalMathGameState.ANSWERING) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3", "4", "5"),
                        listOf("6", "7", "8", "9", "0")
                    )
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { digit ->
                                Button(
                                    onClick = { inputBuffer += digit },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("keypad_btn_$digit")
                                ) {
                                    Text(digit, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Bottom action row: Minus, Clear, Backspace, Submit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { if (inputBuffer.isEmpty()) inputBuffer = "-" },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { if (inputBuffer.isNotEmpty()) inputBuffer = inputBuffer.dropLast(1) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Backspace, contentDescription = "مسح خطوة", modifier = Modifier.size(18.dp))
                        }

                        Button(
                            onClick = { onSubmitAnswer(inputBuffer) },
                            enabled = inputBuffer.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrbitEmerald,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(2f).height(44.dp).testTag("submit_math_answer_btn")
                        ) {
                            Text("تأكيد الحل ✓", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
