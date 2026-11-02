package com.example.ui.screens.classroom

import androidx.compose.ui.res.stringResource
import com.example.R

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitGold
import com.example.ui.theme.OrbitIndigo
import com.example.ui.theme.OrbitRose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.log10
import kotlin.math.sqrt

@Composable
fun LiveAudioMonitor(
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    pauseRecording: Boolean = false,
    onPermissionGranted: () -> Unit = {}
) {
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var audioLevel by remember { mutableStateOf(0f) } // 0f to 1f
    var decibelValue by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) onPermissionGranted()
    }

    // Real audio sampling from device microphone
    LaunchedEffect(hasAudioPermission, isMuted, pauseRecording) {
        if (!hasAudioPermission || isMuted || pauseRecording) {
            audioLevel = 0f
            decibelValue = 0
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            if (bufferSize <= 0) return@withContext

            var audioRecord: AudioRecord? = null
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        bufferSize
                    )
                    audioRecord.startRecording()

                    val buffer = ShortArray(bufferSize / 2)
                    while (isActive && !isMuted) {
                        val read = audioRecord.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            var sum = 0.0
                            for (i in 0 until read) {
                                sum += buffer[i] * buffer[i]
                            }
                            val rms = sqrt(sum / read)
                            val db = if (rms > 0) {
                                (20 * log10(rms / 32767.0) + 90).coerceIn(0.0, 100.0)
                            } else 0.0

                            decibelValue = db.toInt()
                            val normalized = (db / 90.0).toFloat().coerceIn(0f, 1f)
                            audioLevel = normalized
                        }
                        kotlinx.coroutines.delay(60)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val animatedLevel by animateFloatAsState(
        targetValue = if (isMuted) 0f else audioLevel,
        animationSpec = spring(),
        label = "audio_level"
    )

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!hasAudioPermission) {
                TextButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.MicOff, contentDescription = null, tint = OrbitRose, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.ui_8ffc402), fontSize = 10.sp, color = OrbitRose, fontWeight = FontWeight.Bold)
                }
            } else if (pauseRecording) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.ui_4a17c4b), fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
            } else if (isMuted) {
                Icon(Icons.Default.MicOff, contentDescription = null, tint = OrbitRose, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.ui_689e0dd), fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
            } else {
                Icon(Icons.Default.Mic, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$decibelValue dB",
                    fontSize = 10.sp,
                    color = OrbitEmerald,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Real animated equalizer bars driven by device audio
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(16.dp)
                ) {
                    val bars = 5
                    for (i in 0 until bars) {
                        val factor = when (i) {
                            0, 4 -> 0.6f
                            1, 3 -> 0.85f
                            else -> 1f
                        }
                        val heightFraction = (animatedLevel * factor).coerceIn(0.15f, 1f)
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight(heightFraction)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (animatedLevel > 0.5f) OrbitGold else OrbitEmerald)
                        )
                    }
                }
            }
        }
    }
}
