package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MatchStatus
import com.example.data.model.Registration
import com.example.data.model.RoomDetails
import com.example.data.model.Tournament
import com.example.data.model.TournamentMode
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateCard
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
import com.example.ui.theme.StatusOnlineGreen

@Composable
fun HostPanelScreen(
    profile: UserProfile,
    hostBlocked: Pair<Boolean, String?>,
    allTournaments: List<Tournament>,
    registrationsForSelected: List<Registration>,
    roomDetailsCache: Map<String, RoomDetails?>,
    onSelectMatchRegistrations: (String?) -> Unit,
    onLoadRoomDetails: (String) -> Unit,
    onSaveTournament: (
        existing: Tournament?,
        title: String,
        subtitle: String,
        mode: TournamentMode,
        status: MatchStatus,
        bannerKey: String,
        bannerBase64: String?,
        entryLabel: String,
        prizeLabel: String,
        totalSlots: Long,
        scheduleText: String,
        rules: String,
        roomId: String,
        roomPassword: String
    ) -> Unit,
    onUpdateRoomCreds: (tournamentId: String, roomId: String, roomPassword: String) -> Unit,
    onBack: () -> Unit
) {
    if (hostBlocked.first) {
        BlockedScreen(
            reason = hostBlocked.second ?: "Host access suspended by Admin",
            onSignOut = onBack
        )
        return
    }

    val myHostedMatches = remember(allTournaments, profile.userId) {
        allTournaments.filter { it.createdBy == profile.userId }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingTournament by remember { mutableStateOf<Tournament?>(null) }
    var roomEditTournament by remember { mutableStateOf<Tournament?>(null) }
    var registrationsTournament by remember { mutableStateOf<Tournament?>(null) }

    Scaffold(
        containerColor = OffWhite,
        topBar = {
            Surface(color = DeepPurple) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("host_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EsportsYellow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Host Panel",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = EsportsYellow,
                contentColor = DarkInk,
                modifier = Modifier.testTag("host_create_match_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create authorized match")
            }
        },
        bottomBar = {
            Surface(color = DeepPurple) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StatusOnlineGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Host : ${profile.name} • Online",
                        style = MaterialTheme.typography.titleSmall,
                        color = PureWhite
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftYellowBg),
                    border = BorderStroke(1.dp, EsportsYellow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Host Authorization Scope",
                            style = MaterialTheme.typography.titleSmall,
                            color = DarkInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You can create matches, manage slots, upload Room ID & Password, and view registered players. Winning record approval & Admin Panel remain restricted to Administrators.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkInk.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Your Authorized Matches (${myHostedMatches.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkInk
                )
            }

            if (myHostedMatches.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No hosted matches yet",
                        subtitle = "Tap the + button to create your first authorized tournament room."
                    )
                }
            } else {
                items(myHostedMatches, key = { it.id }) { match ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, CardLavenderBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = match.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DarkInk
                                    )
                                    Text(
                                        text = "${match.modeEnum.shortTitle} • ${match.statusEnum.label} • ${match.joinedSlots}/${match.totalSlots} slots",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedPurple
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoftLavender
                                ) {
                                    Text(
                                        text = match.scheduleText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RoyalPurple,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onLoadRoomDetails(match.id)
                                        editingTournament = match
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit")
                                }
                                OutlinedButton(
                                    onClick = {
                                        onLoadRoomDetails(match.id)
                                        roomEditTournament = match
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Room")
                                }
                                Button(
                                    onClick = {
                                        onSelectMatchRegistrations(match.id)
                                        registrationsTournament = match
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoyalPurple,
                                        contentColor = PureWhite
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Players")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        StaffMatchEditorDialog(
            existing = null,
            existingRoom = null,
            onDismiss = { showCreateDialog = false },
            onSave = { title, subtitle, mode, status, bannerKey, bannerBase64, entry, prize, slots, sched, rules, rId, rPass ->
                showCreateDialog = false
                onSaveTournament(null, title, subtitle, mode, status, bannerKey, bannerBase64, entry, prize, slots, sched, rules, rId, rPass)
            }
        )
    }

    editingTournament?.let { match ->
        StaffMatchEditorDialog(
            existing = match,
            existingRoom = roomDetailsCache[match.id],
            onDismiss = { editingTournament = null },
            onSave = { title, subtitle, mode, status, bannerKey, bannerBase64, entry, prize, slots, sched, rules, rId, rPass ->
                editingTournament = null
                onSaveTournament(match, title, subtitle, mode, status, bannerKey, bannerBase64, entry, prize, slots, sched, rules, rId, rPass)
            }
        )
    }

    roomEditTournament?.let { match ->
        RoomCredsDialog(
            tournament = match,
            existing = roomDetailsCache[match.id],
            onDismiss = { roomEditTournament = null },
            onSave = { rId, rPass ->
                roomEditTournament = null
                onUpdateRoomCreds(match.id, rId, rPass)
            }
        )
    }

    registrationsTournament?.let { match ->
        RegistrationsDialog(
            tournamentTitle = match.title,
            registrations = registrationsForSelected,
            onDismiss = {
                registrationsTournament = null
                onSelectMatchRegistrations(null)
            }
        )
    }
}

@Composable
fun RoomCredsDialog(
    tournament: Tournament,
    existing: RoomDetails?,
    onDismiss: () -> Unit,
    onSave: (roomId: String, roomPassword: String) -> Unit
) {
    var roomId by remember(existing) { mutableStateOf(existing?.roomId ?: "") }
    var roomPass by remember(existing) { mutableStateOf(existing?.roomPassword ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Room ID & Password • ${tournament.title}", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomId,
                    onValueChange = { roomId = it },
                    label = { Text("Custom Room ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = roomPass,
                    onValueChange = { roomPass = it },
                    label = { Text("Room Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(roomId, roomPass) },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
            ) {
                Text("Upload Credentials")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RegistrationsDialog(
    tournamentTitle: String,
    registrations: List<Registration>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Registered Players (${registrations.size})", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tournamentTitle, style = MaterialTheme.typography.bodySmall, color = MutedPurple)
                if (registrations.isEmpty()) {
                    Text(
                        text = "No players have joined this match yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkInk
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(registrations, key = { it.id }) { reg ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftLavender
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = reg.playerName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = DarkInk
                                    )
                                    Text(
                                        text = "FF IGN: ${reg.freeFireName.ifBlank { "—" }} • UID: ${reg.freeFireUid.ifBlank { "—" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedPurple
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
