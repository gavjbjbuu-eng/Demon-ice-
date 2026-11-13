package com.example.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paragliding
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MatchStatus
import com.example.data.model.Tournament
import com.example.data.model.TournamentMode
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LoadingStateBox
import com.example.ui.components.MascotAvatar
import com.example.ui.components.TournamentMatchCard
import com.example.ui.theme.AlertBlockedRed
import com.example.ui.theme.CardLavenderBorder
import com.example.ui.theme.DarkInk
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EsportsYellow
import com.example.ui.theme.MutedPurple
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SplashDarkPurple
import com.example.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    profile: UserProfile,
    tournamentsState: UiState<List<Tournament>>,
    joinedTournamentIds: Set<String>,
    selectedMode: TournamentMode?,
    unreadNotificationCount: Int,
    onSelectMode: (TournamentMode) -> Unit,
    onOpenMenu: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenCredits: () -> Unit,
    onOpenMatch: (Tournament) -> Unit,
    onSeeAllMatches: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Deep purple top header + overlapping Tournament Credits card
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(SplashDarkPurple, DeepPurple, RoyalPurple)),
                            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                        )
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 52.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onOpenMenu,
                            modifier = Modifier.testTag("home_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open menu",
                                tint = PureWhite
                            )
                        }
                        Text(
                            text = "ICE ESPORTS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontStyle = FontStyle.Italic,
                                letterSpacing = 1.sp
                            ),
                            color = EsportsYellow
                        )
                        Box {
                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier.testTag("home_notifications_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = PureWhite
                                )
                            }
                            if (unreadNotificationCount > 0) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 6.dp, end = 6.dp),
                                    shape = CircleShape,
                                    color = AlertBlockedRed
                                ) {
                                    Text(
                                        text = unreadNotificationCount.coerceAtMost(99).toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MascotAvatar(
                            avatarBase64 = profile.avatarBase64,
                            size = 52.dp,
                            borderColor = EsportsYellow
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hi, ${profile.name.ifBlank { "Player" }} 👋",
                                style = MaterialTheme.typography.titleLarge,
                                color = PureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = buildString {
                                    append("ID: ${profile.iceId}")
                                    if (profile.freeFireName.isNotBlank()) {
                                        append(" • FF: ${profile.freeFireName}")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = EsportsYellow,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Overlapping White Credits Card (non-cash credits per Requirement 9)
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 32.dp)
                        .padding(horizontal = 16.dp)
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .clickable(onClick = onOpenCredits)
                        .testTag("home_credits_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CardLavenderBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EsportsYellow.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Tournament Credits",
                                    tint = RoyalPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tournament Credits",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedPurple
                                )
                                Text(
                                    text = "${profile.creditsBalance} Credits",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = DarkInk,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Button(
                            onClick = onOpenCredits,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalPurple,
                                contentColor = PureWhite
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("home_credits_history_button")
                        ) {
                            Text(
                                text = "Credits History",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(44.dp))
        }

        // Hero Banner: FREE FIRE CS & BR TOURNAMENTS with crowned cat
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepPurple),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(SplashDarkPurple, RoyalPurple, ElectricPurple)
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, EsportsYellow, RoundedCornerShape(16.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_mascot_cat),
                                contentDescription = "Tournament mascot",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EsportsYellow
                            ) {
                                Text(
                                    text = "SEASON ARENA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkInk,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "FREE FIRE CS & BR\nTOURNAMENTS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 22.sp
                                ),
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Daily free custom rooms • Non-cash credits",
                                style = MaterialTheme.typography.bodySmall,
                                color = EsportsYellow
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4 Tournament Categories: CS, BR, Night Mode, Survival
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModeCategoryCard(
                    mode = TournamentMode.CS,
                    icon = Icons.Default.SportsEsports,
                    selected = selectedMode == TournamentMode.CS,
                    onClick = { onSelectMode(TournamentMode.CS) },
                    modifier = Modifier.weight(1f)
                )
                ModeCategoryCard(
                    mode = TournamentMode.BR,
                    icon = Icons.Default.Paragliding,
                    selected = selectedMode == TournamentMode.BR,
                    onClick = { onSelectMode(TournamentMode.BR) },
                    modifier = Modifier.weight(1f)
                )
                ModeCategoryCard(
                    mode = TournamentMode.NIGHT_MODE,
                    icon = Icons.Default.NightsStay,
                    selected = selectedMode == TournamentMode.NIGHT_MODE,
                    onClick = { onSelectMode(TournamentMode.NIGHT_MODE) },
                    modifier = Modifier.weight(1f)
                )
                ModeCategoryCard(
                    mode = TournamentMode.SURVIVAL,
                    icon = Icons.Default.Security,
                    selected = selectedMode == TournamentMode.SURVIVAL,
                    onClick = { onSelectMode(TournamentMode.SURVIVAL) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section Header: Upcoming Matches + See All
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedMode != null) {
                        "${selectedMode.shortTitle} Matches"
                    } else {
                        "Upcoming Matches"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkInk
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onSeeAllMatches)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("home_see_all_button")
                ) {
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalPurple
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = RoyalPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        when (tournamentsState) {
            is UiState.Loading -> {
                item { LoadingStateBox("Loading live tournaments...") }
            }
            is UiState.Error -> {
                item {
                    EmptyStateCard(
                        title = "Unable to load matches",
                        subtitle = tournamentsState.message,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            is UiState.Success -> {
                val filtered = tournamentsState.data.filter { match ->
                    (selectedMode == null || match.modeEnum == selectedMode) &&
                        match.statusEnum != MatchStatus.COMPLETED
                }
                if (filtered.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No live matches in this category",
                            subtitle = "Check back soon or switch categories above. Admins & Hosts publish new rooms daily!",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    items(filtered, key = { it.id }) { match ->
                        val joined = match.id in joinedTournamentIds
                        TournamentMatchCard(
                            tournament = match,
                            isJoined = joined,
                            actionLabel = if (joined) "View Room" else "Join Now",
                            onActionClick = { onOpenMatch(match) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeCategoryCard(
    mode: TournamentMode,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("mode_card_${mode.code}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) RoyalPurple else PureWhite
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) EsportsYellow else CardLavenderBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 5.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) EsportsYellow else SoftLavender),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = mode.fullLabel,
                    tint = if (selected) DarkInk else RoyalPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mode.shortTitle,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) PureWhite else DarkInk,
                maxLines = 1
            )
            Text(
                text = mode.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) EsportsYellow else MutedPurple,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
