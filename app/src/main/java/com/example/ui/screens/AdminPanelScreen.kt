package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.HostRecord
import com.example.data.model.MatchStatus
import com.example.data.model.Registration
import com.example.data.model.RoomDetails
import com.example.data.model.Tournament
import com.example.data.model.TournamentMode
import com.example.data.model.UserProfile
import com.example.ui.theme.AlertBlockedRed
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

private enum class AdminDialog {
    NONE,
    USERS,
    HOSTS,
    CREATE_MATCH,
    MATCHES_MANAGER,
    REGISTRATIONS,
    PUBLISH_RESULT,
    NOTIFICATIONS,
    AUDIT_LOGS,
    CREDITS_POLICY
}

@Composable
fun AdminPanelScreen(
    profile: UserProfile,
    users: List<UserProfile>,
    hosts: List<HostRecord>,
    tournaments: List<Tournament>,
    auditLogs: List<AuditLog>,
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
    onDeleteTournament: (String) -> Unit,
    onUpdateRoomCreds: (tournamentId: String, roomId: String, roomPassword: String) -> Unit,
    onToggleUserBlock: (uid: String, blocked: Boolean, reason: String) -> Unit,
    onAwardCredits: (uid: String, amount: Long, title: String, note: String) -> Unit,
    onCreateHost: (name: String, email: String, existingUid: String?, assignedMode: String) -> Unit,
    onToggleHostBlock: (hostId: String, blocked: Boolean, reason: String) -> Unit,
    onPublishResult: (
        userId: String,
        playerName: String,
        freeFireName: String,
        tournamentId: String,
        tournamentTitle: String,
        mode: TournamentMode,
        wins: Long,
        kills: Long,
        points: Long,
        rank: Long,
        rewardCredits: Long
    ) -> Unit,
    onSendNotification: (title: String, message: String, type: String, targetUserId: String) -> Unit,
    onBack: () -> Unit
) {
    var activeDialog by remember { mutableStateOf(AdminDialog.NONE) }
    var editingTournament by remember { mutableStateOf<Tournament?>(null) }
    var roomEditTournament by remember { mutableStateOf<Tournament?>(null) }
    var registrationsMatch by remember { mutableStateOf<Tournament?>(null) }

    val gridItems = remember {
        listOf(
            Triple("Users\nManage", Icons.Default.Group, AdminDialog.USERS),
            Triple("Hosts\nManage", Icons.Default.Shield, AdminDialog.HOSTS),
            Triple("Create\nMatch", Icons.Default.AddCircle, AdminDialog.CREATE_MATCH),
            Triple("Matches\nControl", Icons.Default.SportsEsports, AdminDialog.MATCHES_MANAGER),
            Triple("Match\nSlots", Icons.Default.HowToReg, AdminDialog.REGISTRATIONS),
            Triple("Block\nUser", Icons.Default.PersonOff, AdminDialog.USERS),
            Triple("Block\nHost", Icons.Default.Block, AdminDialog.HOSTS),
            Triple("Publish\nResults", Icons.Default.EmojiEvents, AdminDialog.PUBLISH_RESULT),
            Triple("Credits\nPolicy", Icons.Default.AccountBalanceWallet, AdminDialog.CREDITS_POLICY),
            Triple("Send\nAlerts", Icons.Default.NotificationsActive, AdminDialog.NOTIFICATIONS),
            Triple("Audit\nLogs", Icons.Default.History, AdminDialog.AUDIT_LOGS),
            Triple("Room ID\n& Pass", Icons.Default.VpnKey, AdminDialog.MATCHES_MANAGER)
        )
    }

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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = EsportsYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Admin Panel",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = DeepPurple,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        text = "Admin : ${profile.name.ifBlank { "Pushkar" }} • Online",
                        style = MaterialTheme.typography.titleSmall,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Dashboard summary counters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard("Players", users.size.toString(), Modifier.weight(1f))
                    AdminMetricCard("Hosts", hosts.size.toString(), Modifier.weight(1f))
                    AdminMetricCard("Matches", tournaments.size.toString(), Modifier.weight(1f))
                    AdminMetricCard("Logs", auditLogs.size.toString(), Modifier.weight(1f))
                }
            }

            // 3-column Admin action grid matching Screenshot 8
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    gridItems.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { (label, icon, dialogType) ->
                                AdminGridCard(
                                    label = label,
                                    icon = icon,
                                    onClick = { activeDialog = dialogType },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    when (activeDialog) {
        AdminDialog.NONE -> Unit
        AdminDialog.USERS -> {
            AdminUsersDialog(
                users = users,
                onToggleBlock = onToggleUserBlock,
                onAwardCredits = onAwardCredits,
                onDismiss = { activeDialog = AdminDialog.NONE }
            )
        }
        AdminDialog.HOSTS -> {
            AdminHostsDialog(
                hosts = hosts,
                users = users,
                onCreateHost = onCreateHost,
                onToggleHostBlock = onToggleHostBlock,
                onDismiss = { activeDialog = AdminDialog.NONE }
            )
        }
        AdminDialog.CREATE_MATCH -> {
            StaffMatchEditorDialog(
                existing = null,
                existingRoom = null,
                onDismiss = { activeDialog = AdminDialog.NONE },
                onSave = { title, subtitle, mode, status, bannerKey, bannerBase64, entry, prize, slots, sched, rules, rId, rPass ->
                    activeDialog = AdminDialog.NONE
                    onSaveTournament(null, title, subtitle, mode, status, bannerKey, bannerBase64, entry, prize, slots, sched, rules, rId, rPass)
                }
            )
        }
        AdminDialog.MATCHES_MANAGER -> {
            AdminMatchesManagerDialog(
                tournaments = tournaments,
                onEdit = { match ->
                    onLoadRoomDetails(match.id)
                    editingTournament = match
                },
                onEditRoom = { match ->
                    onLoadRoomDetails(match.id)
                    roomEditTournament = match
                },
                onDelete = onDeleteTournament,
                onDismiss = { activeDialog = AdminDialog.NONE }
            )
        }
        AdminDialog.REGISTRATIONS -> {
            AdminRegistrationsPickerDialog(
                tournaments = tournaments,
                onSelectMatch = { match ->
                    onSelectMatchRegistrations(match.id)
                    registrationsMatch = match
                },
                onDismiss = { activeDialog = AdminDialog.NONE }
            )
        }
        AdminDialog.PUBLISH_RESULT -> {
            AdminPublishResultDialog(
                users = users,
                tournaments = tournaments,
                onPublish = onPublishResult,
                onDismiss = { activeDialog = AdminDialog.NONE }
            )
        }
        AdminDialog.NOTIFICATIONS -> {
            AdminNotificationDialog(
                onSend = onSendNotification,
                onDismiss = { activeDialog = AdminDialog.NONE }
            )
        }
        AdminDialog.AUDIT_LOGS -> {
            AdminAuditLogsDialog(
                logs = auditLogs,
                onDismiss = { activeDialog = AdminDialog.NONE }
            )
        }
        AdminDialog.CREDITS_POLICY -> {
            AdminCreditsDisabledDialog(
                onDismiss = { activeDialog = AdminDialog.NONE }
            )
        }
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

    registrationsMatch?.let { match ->
        RegistrationsDialog(
            tournamentTitle = match.title,
            registrations = registrationsForSelected,
            onDismiss = {
                registrationsMatch = null
                onSelectMatchRegistrations(null)
            }
        )
    }
}

@Composable
private fun AdminMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, CardLavenderBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = RoyalPurple,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MutedPurple
            )
        }
    }
}

@Composable
private fun AdminGridCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(106.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, CardLavenderBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftLavender),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label.replace("\n", " "),
                    tint = RoyalPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DarkInk,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun AdminMatchesManagerDialog(
    tournaments: List<Tournament>,
    onEdit: (Tournament) -> Unit,
    onEditRoom: (Tournament) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Matches (${tournaments.size})", style = MaterialTheme.typography.titleMedium) },
        text = {
            if (tournaments.isEmpty()) {
                Text("No matches created yet.")
            } else {
                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tournaments, key = { it.id }) { match ->
                        Surface(shape = RoundedCornerShape(12.dp), color = SoftLavender) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(match.title, style = MaterialTheme.typography.titleSmall, color = DarkInk)
                                Text(
                                    text = "${match.modeEnum.shortTitle} • ${match.statusEnum.label} • ${match.joinedSlots}/${match.totalSlots}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedPurple
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(onClick = { onEdit(match) }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    }
                                    Button(
                                        onClick = { onEditRoom(match) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.VpnKey, contentDescription = "Room", modifier = Modifier.size(16.dp))
                                    }
                                    OutlinedButton(
                                        onClick = { onDelete(match.id) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertBlockedRed),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun AdminRegistrationsPickerDialog(
    tournaments: List<Tournament>,
    onSelectMatch: (Tournament) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("View Match Registrations", style = MaterialTheme.typography.titleMedium) },
        text = {
            if (tournaments.isEmpty()) {
                Text("Create a tournament first to inspect registrations.")
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tournaments, key = { it.id }) { match ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftLavender,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectMatch(match) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(match.title, style = MaterialTheme.typography.titleSmall, color = DarkInk)
                                    Text("${match.modeEnum.shortTitle} • ${match.scheduleText}", style = MaterialTheme.typography.bodySmall, color = MutedPurple)
                                }
                                Text("${match.joinedSlots}/${match.totalSlots}", style = MaterialTheme.typography.titleSmall, color = RoyalPurple)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
