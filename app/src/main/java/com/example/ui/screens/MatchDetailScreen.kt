package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MatchStatus
import com.example.data.model.RoomDetails
import com.example.data.model.Tournament
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.TournamentBannerImage
import com.example.ui.theme.CardLavenderBorder
import com.example.ui.theme.DarkInk
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.EsportsYellow
import com.example.ui.theme.MutedPurple
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StatusOnlineGreen

@Composable
fun MatchDetailScreen(
    tournament: Tournament,
    profile: UserProfile,
    userRole: UserRole,
    isJoined: Boolean,
    roomDetails: RoomDetails?,
    isBusy: Boolean,
    onLoadRoomDetails: () -> Unit,
    onJoinNow: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val canViewRoom = isJoined || userRole == UserRole.ADMIN || userRole == UserRole.HOST

    LaunchedEffect(tournament.id, canViewRoom) {
        if (canViewRoom) {
            onLoadRoomDetails()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        // Deep purple top app bar
        Surface(color = DeepPurple) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("match_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite
                    )
                }
                Text(
                    text = if (isJoined) "Match Details" else "Join Match",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Match Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                ) {
                    TournamentBannerImage(
                        tournament = tournament,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        DeepPurple.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EsportsYellow
                        ) {
                            Text(
                                text = tournament.modeEnum.shortTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkInk,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DeepPurple.copy(alpha = 0.88f)
                        ) {
                            Text(
                                text = tournament.statusEnum.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = tournament.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = PureWhite
                        )
                        Text(
                            text = tournament.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EsportsYellow
                        )
                    }
                }
            }

            DetailInfoRowCard(
                icon = Icons.Default.CalendarToday,
                label = "Date & Time",
                value = tournament.scheduleText
            )

            DetailInfoRowCard(
                icon = Icons.Default.ConfirmationNumber,
                label = "Entry Information",
                value = tournament.entryLabel
            )

            DetailInfoRowCard(
                icon = Icons.Default.EmojiEvents,
                label = "Tournament Reward",
                value = tournament.prizeLabel
            )

            // Slots progress card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CardLavenderBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = RoyalPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Slots (${tournament.availableSlots} available)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedPurple
                            )
                            Text(
                                text = "${tournament.joinedSlots} / ${tournament.totalSlots}",
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkInk
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val progress = if (tournament.totalSlots > 0) {
                        (tournament.joinedSlots.toFloat() / tournament.totalSlots.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = RoyalPurple,
                        trackColor = SoftLavender
                    )
                }
            }

            // Room ID & Room Password Card (Protected by Firestore rules!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CardLavenderBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (canViewRoom) Icons.Default.VpnKey else Icons.Default.Lock,
                            contentDescription = null,
                            tint = RoyalPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Match Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkInk
                        )
                    }

                    if (!canViewRoom) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftLavender
                        ) {
                            Text(
                                text = "Join this match to unlock the Free Fire Custom Room ID & Password.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RoyalPurple,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        val rId = roomDetails?.roomId?.ifBlank { "Released 15m before start" }
                            ?: "Released 15m before start"
                        val rPass = roomDetails?.roomPassword?.ifBlank { "Pending" } ?: "Pending"

                        CredentialCopyPill(
                            label = "Room ID",
                            value = rId,
                            canCopy = !roomDetails?.roomId.isNullOrBlank(),
                            onCopy = {
                                clipboard.setText(AnnotatedString(rId))
                                Toast.makeText(context, "Room ID copied", Toast.LENGTH_SHORT).show()
                            }
                        )
                        CredentialCopyPill(
                            label = "Password",
                            value = rPass,
                            canCopy = !roomDetails?.roomPassword.isNullOrBlank(),
                            onCopy = {
                                clipboard.setText(AnnotatedString(rPass))
                                Toast.makeText(context, "Password copied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Match Rules Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CardLavenderBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = RoyalPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Match Rules",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkInk
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tournament.rules.ifBlank {
                            "1. Register with your exact Free Fire UID.\n2. Join custom room 10 minutes before start.\n3. Teaming, emulators (unless allowed), or scripts result in immediate block."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedPurple
                    )
                }
            }
        }

        // Bottom Join CTA
        Surface(
            color = PureWhite,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val isFull = tournament.joinedSlots >= tournament.totalSlots
                val isClosed = tournament.statusEnum != MatchStatus.UPCOMING
                val canJoin = !isJoined && !isFull && !isClosed && !isBusy && !profile.isBlocked

                Button(
                    onClick = onJoinNow,
                    enabled = canJoin,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isJoined) StatusOnlineGreen else EsportsYellow,
                        contentColor = if (isJoined) PureWhite else DarkInk,
                        disabledContainerColor = if (isJoined) StatusOnlineGreen else SoftLavender,
                        disabledContentColor = if (isJoined) PureWhite else MutedPurple
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("join_match_cta_button")
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = DarkInk,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = when {
                                isJoined -> "Joined • Slot Confirmed"
                                isFull -> "All Slots Full"
                                isClosed -> "Registration Closed (${tournament.statusEnum.label})"
                                else -> "Join Now"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRowCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, CardLavenderBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SoftLavender),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = RoyalPurple)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = MutedPurple)
                Text(text = value, style = MaterialTheme.typography.titleMedium, color = DarkInk)
            }
        }
    }
}

@Composable
private fun CredentialCopyPill(
    label: String,
    value: String,
    canCopy: Boolean,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SoftLavender
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$label : ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedPurple
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkInk,
                    fontWeight = FontWeight.Bold
                )
            }
            if (canCopy) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        tint = RoyalPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
