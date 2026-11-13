package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Tournament
import com.example.ui.theme.CardLavenderBorder
import com.example.ui.theme.DarkInk
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EsportsYellow
import com.example.ui.theme.MutedPurple
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftYellowBg
import com.example.util.ImageUtils

@Composable
fun MascotAvatar(
    avatarBase64: String?,
    size: Dp = 48.dp,
    borderColor: Color = EsportsYellow,
    modifier: Modifier = Modifier
) {
    val decodedBitmap = remember(avatarBase64) { ImageUtils.decodeBase64ImageBitmap(avatarBase64) }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(DeepPurple)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (decodedBitmap != null) {
            Image(
                bitmap = decodedBitmap,
                contentDescription = "Player avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_mascot_cat),
                contentDescription = "Crowned gaming cat mascot",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TournamentBannerImage(
    tournament: Tournament,
    modifier: Modifier = Modifier
) {
    val customBitmap = remember(tournament.bannerBase64) {
        ImageUtils.decodeBase64ImageBitmap(tournament.bannerBase64)
    }
    if (customBitmap != null) {
        Image(
            bitmap = customBitmap,
            contentDescription = "${tournament.title} banner",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = tournament.modeEnum.defaultBannerRes),
            contentDescription = "${tournament.title} banner",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun TournamentMatchCard(
    tournament: Tournament,
    isJoined: Boolean,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onActionClick)
            .testTag("match_card_${tournament.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, CardLavenderBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 108.dp, height = 84.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                TournamentBannerImage(
                    tournament = tournament,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = DeepPurple.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = tournament.modeEnum.shortTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = EsportsYellow,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tournament.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = RoyalPurple,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tournament.scheduleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedPurple,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = RoyalPurple,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tournament.entryLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedPurple,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${tournament.joinedSlots}/${tournament.totalSlots}",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkInk
                        )
                    }

                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isJoined) SoftLavender else EsportsYellow,
                            contentColor = if (isJoined) RoyalPurple else DarkInk
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("match_action_${tournament.id}")
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BrandFooterBanner(
    headline: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(DeepPurple, RoyalPurple, ElectricPurple)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    MascotAvatar(avatarBase64 = null, size = 44.dp, borderColor = EsportsYellow)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.titleMedium,
                            color = EsportsYellow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = PureWhite
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Crown trophy",
                    tint = EsportsYellow,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun CustomerSupportRow(
    modifier: Modifier = Modifier,
    darkText: Boolean = true
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small circular WhatsApp support icon (9525052870)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (darkText) SoftLavender else PureWhite.copy(alpha = 0.12f))
                .clickable { openWhatsAppSupport(context) }
                .padding(10.dp)
                .testTag("whatsapp_support_button")
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF25D366))
                    .border(2.dp, PureWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "WhatsApp Support 9525052870",
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "WhatsApp",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (darkText) DarkInk else PureWhite
                )
                Text(
                    text = "9525052870",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (darkText) MutedPurple else EsportsYellow,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Small circular YouTube channel icon (Official Gamer)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (darkText) SoftLavender else PureWhite.copy(alpha = 0.12f))
                .clickable { openYouTubeChannel(context) }
                .padding(10.dp)
                .testTag("youtube_channel_button")
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF0000))
                    .border(2.dp, PureWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Official Gamer YouTube Channel",
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "YouTube",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (darkText) DarkInk else PureWhite
                )
                Text(
                    text = "Official Gamer",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (darkText) MutedPurple else EsportsYellow,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun openWhatsAppSupport(context: Context) {
    val waUri = Uri.parse("https://wa.me/919525052870")
    val waIntent = Intent(Intent.ACTION_VIEW, waUri)
    runCatching {
        context.startActivity(waIntent)
    }.onFailure {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9525052870"))
        runCatching { context.startActivity(dialIntent) }.onFailure {
            Toast.makeText(context, "WhatsApp Support: 9525052870", Toast.LENGTH_LONG).show()
        }
    }
}

fun openYouTubeChannel(context: Context) {
    val ytUri = Uri.parse("https://youtube.com/@officialgamer-qf7rd?si=9djVYcsS6uQu91tC")
    val intent = Intent(Intent.ACTION_VIEW, ytUri)
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "YouTube: @officialgamer-qf7rd", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, CardLavenderBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SoftYellowBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = RoyalPurple,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DarkInk,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedPurple,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingStateBox(
    label: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = RoyalPurple)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MutedPurple)
        }
    }
}
