package com.example.ui.components

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.User
import com.example.ui.theme.OrbitGold
import com.example.ui.theme.OrbitIndigo
import com.example.ui.theme.OrbitNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrbitTopBar(
    currentUser: User?,
    onOpenAuth: () -> Unit,
    onOpenBooking: () -> Unit,
    onOpenVerifier: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenBooking() }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(OrbitNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = stringResource(R.string.ui_690541c),
                        tint = OrbitGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.ui_5e13683),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = OrbitGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "2026",
                                color = OrbitGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.ui_a0c9c29),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Actions & User Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Verify Certificate Button
                IconButton(
                    onClick = onOpenVerifier,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("verify_cert_top_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = stringResource(R.string.ui_bd9b06f),
                        tint = OrbitIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Booking Button
                FilledTonalButton(
                    onClick = onOpenBooking,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = OrbitGold.copy(alpha = 0.15f),
                        contentColor = OrbitGold
                    ),
                    modifier = Modifier.testTag("top_booking_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.ui_d6ba13e),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // User profile avatar / Login button
                FilledTonalButton(
                    onClick = onOpenAuth,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (currentUser != null) OrbitIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (currentUser != null) OrbitIndigo else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("user_profile_chip")
                ) {
                    Icon(
                        imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.Login,
                        contentDescription = stringResource(R.string.ui_0869f7a),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentUser?.let { "${it.displayName.take(12)} (${it.role.labelAr.take(5)})" } ?: "دخول / حساب",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
