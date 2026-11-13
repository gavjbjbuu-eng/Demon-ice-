package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppNotification
import com.example.data.model.MatchResult
import com.example.data.model.MatchStatus
import com.example.data.model.Registration
import com.example.data.model.Tournament
import com.example.ui.components.BrandFooterBanner
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LoadingStateBox
import com.example.ui.components.MascotAvatar
import com.example.ui.components.TournamentMatchCard
import com.example.ui.theme.CardLavenderBorder
import com.example.ui.theme.DarkInk
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.EsportsYellow
import com.example.ui.theme.MutedPurple
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftYellowBg
import com.example.ui.theme.SubtleDivider
import com.example.ui.theme.WarmGold
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyMatchesScreen(
    tournamentsState: UiState<List<Tournament>>,
    registrations: List<Registration>,
    showAllPublicFallback: Boolean = false,
    onOpenMatch: (Tournament) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf(MatchStatus.UPCOMING) }
    var onlyJoined by remember { mutableStateOf(!showAllPublicFallback) }
    val joinedIds = remember(registrations) { registrations.map { it.tournamentId }.toSet() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Surface(color = DeepPurple) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (onlyJoined) "My Matches" else "All Matches",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RoyalPurple,
                        border = BorderStroke(1.dp, EsportsYellow),
                        modifier = Modifier
                            .clickable { onlyJoined = !onlyJoined }
                            .testTag("toggle_joined_all_matches")
                    ) {
                        Text(
                            text = if (onlyJoined) "Joined (${joinedIds.size})" else "All Arena",
                            style = MaterialTheme.typography.labelMedium,
                            color = EsportsYellow,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Segmented pill tabs: Upcoming | Ongoing | Completed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MatchStatus.entries.forEach { status ->
                val active = selectedStatus == status
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedStatus = status }
                        .testTag("matches_tab_${status.code}"),
                    shape = RoundedCornerShape(16.dp),
                    color = if (active) RoyalPurple else PureWhite,
                    border = BorderStroke(1.dp, if (active) RoyalPurple else CardLavenderBorder)
                ) {
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) PureWhite else MutedPurple,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (tournamentsState) {
                is UiState.Loading -> {
                    item { LoadingStateBox("Loading matches...") }
                }
                is UiState.Error -> {
                    item {
                        EmptyStateCard(
                            title = "Could not load matches",
                            subtitle = tournamentsState.message
                        )
                    }
                }
                is UiState.Success -> {
                    val filtered = tournamentsState.data.filter { match ->
                        match.statusEnum == selectedStatus && (!onlyJoined || match.id in joinedIds)
                    }
                    if (filtered.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = if (onlyJoined) {
                                    "No ${selectedStatus.label.lowercase()} joined matches"
                                } else {
                                    "No ${selectedStatus.label.lowercase()} tournaments"
                                },
                                subtitle = if (onlyJoined) {
                                    "Join any CS, BR, Night Mode, or Survival tournament from Home or tap 'Joined' above to browse all matches."
                                } else {
                                    "New tournaments will appear here as soon as an Admin or Host schedules them."
                                }
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { match ->
                            val isJoined = match.id in joinedIds
                            TournamentMatchCard(
                                tournament = match,
                                isJoined = isJoined,
                                actionLabel = if (isJoined) "View" else "Join Now",
                                onActionClick = { onOpenMatch(match) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                BrandFooterBanner(
                    headline = "ICE ESPORTS",
                    subtitle = "Play Like a Pro • Custom Free Fire Rooms"
                )
            }
        }
    }
}

@Composable
fun LeaderboardScreen(
    leaderboardState: UiState<List<MatchResult>>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "ALL" to "All",
        "CS" to "CS",
        "BR" to "BR",
        "NIGHT_MODE" to "Night",
        "SURVIVAL" to "Survival"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Surface(color = DeepPurple) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = EsportsYellow,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Leaderboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PureWhite
                )
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (code, label) ->
                val active = selectedFilter == code
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (active) RoyalPurple else PureWhite,
                    border = BorderStroke(1.dp, if (active) RoyalPurple else CardLavenderBorder),
                    modifier = Modifier
                        .clickable { onSelectFilter(code) }
                        .testTag("leaderboard_filter_$code")
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) PureWhite else DarkInk,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (leaderboardState) {
                is UiState.Loading -> {
                    item { LoadingStateBox("Loading standings...") }
                }
                is UiState.Error -> {
                    item {
                        EmptyStateCard(
                            title = "Leaderboard unavailable",
                            subtitle = leaderboardState.message
                        )
                    }
                }
                is UiState.Success -> {
                    val entries = leaderboardState.data
                    if (entries.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No published results yet",
                                subtitle = "Official standings appear here once the Admin publishes verified match results."
                            )
                        }
                    } else {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, CardLavenderBorder)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SoftLavender)
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#", style = MaterialTheme.typography.labelMedium, color = MutedPurple, modifier = Modifier.width(30.dp))
                                        Text("Player Name", style = MaterialTheme.typography.labelMedium, color = MutedPurple, modifier = Modifier.weight(1f))
                                        Text("Wins", style = MaterialTheme.typography.labelMedium, color = MutedPurple)
                                    }

                                    entries.forEachIndexed { index, entry ->
                                        LeaderboardRow(rank = index + 1, entry = entry)
                                        if (index < entries.lastIndex) {
                                            HorizontalDivider(color = SubtleDivider)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                BrandFooterBanner(
                    headline = "Play Hard",
                    subtitle = "Be The Best • Admin-Verified Standings"
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: MatchResult) {
    val crownTint = when (rank) {
        1 -> WarmGold
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFD97706)
        else -> null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.titleMedium,
            color = if (rank <= 3) RoyalPurple else MutedPurple,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp)
        )
        if (crownTint != null) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "Rank $rank",
                tint = crownTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        MascotAvatar(
            avatarBase64 = null,
            size = 32.dp,
            borderColor = crownTint ?: CardLavenderBorder
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.freeFireName.ifBlank { entry.playerName },
                style = MaterialTheme.typography.titleSmall,
                color = DarkInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${entry.tournamentTitle} • ${entry.points} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MutedPurple,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (rank == 1) SoftYellowBg else SoftLavender
        ) {
            Text(
                text = "${entry.wins}",
                style = MaterialTheme.typography.titleMedium,
                color = DarkInk,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun NotificationsScreen(
    notificationsState: UiState<List<AppNotification>>,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Surface(color = DeepPurple) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = EsportsYellow,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PureWhite
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (notificationsState) {
                is UiState.Loading -> {
                    item { LoadingStateBox("Loading announcements...") }
                }
                is UiState.Error -> {
                    item {
                        EmptyStateCard(
                            title = "Notifications unavailable",
                            subtitle = notificationsState.message
                        )
                    }
                }
                is UiState.Success -> {
                    val itemsList = notificationsState.data
                    if (itemsList.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "You're all caught up!",
                                subtitle = "Match room updates, winner announcements, and system alerts appear here."
                            )
                        }
                    } else {
                        items(itemsList, key = { it.id }) { notif ->
                            val icon = when (notif.type) {
                                "MATCH" -> Icons.Default.SportsEsports
                                "RESULT" -> Icons.Default.EmojiEvents
                                "SECURITY" -> Icons.Default.Security
                                else -> Icons.Default.Notifications
                            }
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, CardLavenderBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(SoftLavender),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = notif.type,
                                            tint = RoyalPurple
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = notif.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = DarkInk,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = SoftYellowBg
                                            ) {
                                                Text(
                                                    text = notif.type,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = DarkInk,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notif.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedPurple
                                        )
                                        notif.createdAt?.toDate()?.let { date ->
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = formatter.format(date),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MutedPurple.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
