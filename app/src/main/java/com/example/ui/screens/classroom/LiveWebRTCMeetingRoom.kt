package com.example.ui.screens.classroom

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.OrbitEmerald
import com.example.ui.theme.OrbitGold
import com.example.ui.theme.OrbitIndigo
import com.example.ui.theme.OrbitRose

@Composable
fun LiveWebRTCMeetingRoom(
    roomName: String = "OrbitLearn2026_SaraElarby_Classroom",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasRendererCrashed by remember { mutableStateOf(false) }
    var retryKey by remember { mutableIntStateOf(0) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher to request native camera & mic permissions safely
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
        // If permissions were just granted, reload meeting to let WebRTC attach media tracks
        if (hasCameraPermission || hasAudioPermission) {
            webViewInstance?.reload()
        }
    }

    // Proactively request permissions once on enter if missing
    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    // Real meeting room URL supporting WebRTC video conference and screen sharing
    // Use safe startup parameters: muted by default to prevent audio device collision on startup
    val meetingUrl = remember(roomName) {
        "https://meet.jit.si/$roomName#config.prejoinPageEnabled=false&config.disableDeepLinking=true&config.resolution=360&config.startWithAudioMuted=true&config.startWithVideoMuted=true"
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewInstance?.stopLoading()
                (webViewInstance?.parent as? ViewGroup)?.removeView(webViewInstance)
                webViewInstance?.destroy()
                webViewInstance = null
            } catch (_: Throwable) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A))
    ) {
        if (hasRendererCrashed) {
            // Elegant recovery UI when WebView renderer hits low memory or GPU limits
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = OrbitEmerald,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "غرفة المؤتمر المرئي المباشر (WebRTC)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "لتوفير أعلى أداء لمشاركة الشاشات والكاميرات بجودة HD، يُفضّل الانضمام مباشرة عبر المتصفح أو تطبيق Jitsi Meet بدون قيود ذاكرة العرض المدمجة.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(meetingUrl))
                            context.startActivity(browserIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("launch_external_meeting_btn")
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الانضمام عبر المتصفح 🌐", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            hasRendererCrashed = false
                            isLoading = true
                            retryKey++
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("retry_embedded_webview_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة المحاولة", fontSize = 12.sp)
                    }
                }
            }
        } else {
            key(retryKey) {
                AndroidView(
                    modifier = Modifier.fillMaxSize().testTag("webrtc_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                allowFileAccess = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                databaseEnabled = true
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                }

                                // Handling onRenderProcessGone prevents OS from killing the host app
                                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                    hasRendererCrashed = true
                                    try {
                                        view?.stopLoading()
                                        (view?.parent as? ViewGroup)?.removeView(view)
                                        view?.destroy()
                                    } catch (_: Throwable) {}
                                    webViewInstance = null
                                    return true // Handled: Host app will NOT be terminated!
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    if (request == null) return

                                    // CRITICAL: Check Android runtime permissions before granting to WebView
                                    // Calling request.grant() when Android app lacks CAMERA/RECORD_AUDIO permission
                                    // causes CameraManager.openCamera to throw SecurityException and crash Chromium!
                                    val camOk = ContextCompat.checkSelfPermission(
                                        ctx,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED

                                    val audioOk = ContextCompat.checkSelfPermission(
                                        ctx,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    val resourcesToGrant = mutableListOf<String>()
                                    for (resource in request.resources) {
                                        when (resource) {
                                            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                                                if (camOk) resourcesToGrant.add(resource)
                                            }
                                            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                                                if (audioOk) resourcesToGrant.add(resource)
                                            }
                                        }
                                    }

                                    if (resourcesToGrant.isNotEmpty()) {
                                        request.grant(resourcesToGrant.toTypedArray())
                                    } else {
                                        // Safely deny to prevent SecurityException crash in Chromium!
                                        request.deny()
                                    }
                                }
                            }

                            loadUrl(meetingUrl)
                            webViewInstance = this
                        }
                    },
                    update = {
                        webViewInstance = it
                    }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = OrbitIndigo)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "جاري الاتصال بغرفة المؤتمر المرئي المباشر...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Top control & status bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ScreenShare, contentDescription = null, tint = OrbitEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ميتينج تفاعلي حي • يدعم مشاركة الشاشات الحقيقية",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { webViewInstance?.reload() },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(meetingUrl))
                                context.startActivity(browserIntent)
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(OrbitIndigo)
                                .testTag("open_webrtc_external_btn")
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = "فتح في المتصفح", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Permission rationale bar if permissions are not granted yet
                if (!hasCameraPermission || !hasAudioPermission) {
                    Surface(
                        color = Color(0xFF1E1B4B).copy(alpha = 0.95f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = OrbitGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تفعيل الكاميرا والمايك لمشاركة الصوت والصورة في الميتينج",
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OrbitEmerald),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("grant_webrtc_permissions_btn")
                            ) {
                                Text("تفعيل الآن", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
