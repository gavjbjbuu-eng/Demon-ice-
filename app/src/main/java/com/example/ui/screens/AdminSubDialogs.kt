package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.HostRecord
import com.example.data.model.Tournament
import com.example.data.model.TournamentMode
import com.example.data.model.UserProfile
import com.example.ui.theme.AlertBlockedRed
import com.example.ui.theme.CardLavenderBorder
import com.example.ui.theme.DarkInk
import com.example.ui.theme.MutedPurple
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftYellowBg
import com.example.ui.theme.StatusOnlineGreen
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminUsersDialog(
    users: List<UserProfile>,
    onToggleBlock: (uid: String, blocked: Boolean, reason: String) -> Unit,
    onAwardCredits: (uid: String, amount: Long, title: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var blockTarget by remember { mutableStateOf<UserProfile?>(null) }
    var creditTarget by remember { mutableStateOf<UserProfile?>(null) }
    var blockReason by remember { mutableStateOf("Suspicious Activity") }
    var creditAmount by remember { mutableStateOf("100") }
    var creditNote by remember { mutableStateOf("Tournament credit award") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Players (${users.size})", style = MaterialTheme.typography.titleLarge) },
        text = {
            if (users.isEmpty()) {
                Text("No registered players yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users, key = { it.userId }) { user ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftLavender
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${user.name} (${user.iceId})",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = DarkInk
                                        )
                                        Text(
                                            text = "${user.email}\nFF: ${user.freeFireName.ifBlank { "—" }} • Credits: ${user.creditsBalance}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedPurple
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (user.isBlocked) AlertBlockedRed.copy(alpha = 0.14f) else StatusOnlineGreen.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = if (user.isBlocked) "BLOCKED" else "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (user.isBlocked) AlertBlockedRed else StatusOnlineGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { creditTarget = user },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("+ Credits")
                                    }
                                    Button(
                                        onClick = {
                                            if (user.isBlocked) {
                                                onToggleBlock(user.userId, false, "")
                                            } else {
                                                blockReason = "Suspicious Activity"
                                                blockTarget = user
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (user.isBlocked) StatusOnlineGreen else AlertBlockedRed
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (user.isBlocked) "Unblock" else "Block")
                                    }
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

    blockTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { blockTarget = null },
            title = { Text("Block ${user.name}?") },
            text = {
                OutlinedTextField(
                    value = blockReason,
                    onValueChange = { blockReason = it },
                    label = { Text("Block Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleBlock(user.userId, true, blockReason)
                        blockTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertBlockedRed)
                ) {
                    Text("Confirm Block")
                }
            },
            dismissButton = { TextButton(onClick = { blockTarget = null }) { Text("Cancel") } }
        )
    }

    creditTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { creditTarget = null },
            title = { Text("Award Credits • ${user.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = creditAmount,
                        onValueChange = { creditAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Credits (Non-Cash)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = creditNote,
                        onValueChange = { creditNote = it },
                        label = { Text("Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = creditAmount.toLongOrNull() ?: 0L
                        if (amt > 0) {
                            onAwardCredits(user.userId, amt, "Admin Credit Award", creditNote)
                        }
                        creditTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
                ) {
                    Text("Award")
                }
            },
            dismissButton = { TextButton(onClick = { creditTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AdminHostsDialog(
    hosts: List<HostRecord>,
    users: List<UserProfile>,
    onCreateHost: (name: String, email: String, existingUid: String?, assignedMode: String) -> Unit,
    onToggleHostBlock: (hostId: String, blocked: Boolean, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedUid by remember { mutableStateOf<String?>(null) }
    var assignedMode by remember { mutableStateOf("ALL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Hosts (${hosts.size})", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Authorize New Host (Admin-Only)", style = MaterialTheme.typography.titleSmall, color = RoyalPurple)
                if (users.isNotEmpty()) {
                    Text("Quick-fill from registered player:", style = MaterialTheme.typography.labelSmall, color = MutedPurple)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        users.take(8).forEach { u ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedUid == u.userId) RoyalPurple else SoftLavender,
                                modifier = Modifier.clickable {
                                    selectedUid = u.userId
                                    name = u.name
                                    email = u.email
                                }
                            ) {
                                Text(
                                    text = u.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedUid == u.userId) PureWhite else DarkInk,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Host Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        selectedUid = users.firstOrNull { u -> u.email.equals(it.trim(), ignoreCase = true) }?.userId
                    },
                    label = { Text("Host Gmail Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "CS", "BR", "NIGHT_MODE", "SURVIVAL").forEach { mode ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (assignedMode == mode) RoyalPurple else PureWhite,
                            border = BorderStroke(1.dp, CardLavenderBorder),
                            modifier = Modifier.clickable { assignedMode = mode }
                        ) {
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (assignedMode == mode) PureWhite else DarkInk,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        onCreateHost(name, email, selectedUid, assignedMode)
                        name = ""
                        email = ""
                        selectedUid = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Host Account")
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Existing Hosts", style = MaterialTheme.typography.titleSmall, color = DarkInk)
                hosts.forEach { host ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftLavender
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(host.name, style = MaterialTheme.typography.titleSmall, color = DarkInk)
                                Text("${host.email} • ${host.assignedMode}", style = MaterialTheme.typography.bodySmall, color = MutedPurple)
                            }
                            Button(
                                onClick = {
                                    onToggleHostBlock(host.hostId, !host.isBlocked, if (!host.isBlocked) "Suspended by Admin" else "")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (host.isBlocked) StatusOnlineGreen else AlertBlockedRed
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (host.isBlocked) "Unblock" else "Block")
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

@Composable
fun AdminPublishResultDialog(
    users: List<UserProfile>,
    tournaments: List<Tournament>,
    onPublish: (
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
    onDismiss: () -> Unit
) {
    var selectedUser by remember { mutableStateOf(users.firstOrNull()) }
    var selectedMatch by remember { mutableStateOf(tournaments.firstOrNull()) }
    var wins by remember { mutableStateOf("1") }
    var kills by remember { mutableStateOf("8") }
    var points by remember { mutableStateOf("100") }
    var rank by remember { mutableStateOf("1") }
    var rewardCredits by remember { mutableStateOf("250") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Match Result (Admin-Only)", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Select Player", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    users.forEach { u ->
                        val active = selectedUser?.userId == u.userId
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) RoyalPurple else SoftLavender,
                            modifier = Modifier.clickable { selectedUser = u }
                        ) {
                            Text(
                                text = u.freeFireName.ifBlank { u.name },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) PureWhite else DarkInk,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text("Select Match", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tournaments.forEach { m ->
                        val active = selectedMatch?.id == m.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) RoyalPurple else SoftLavender,
                            modifier = Modifier.clickable { selectedMatch = m }
                        ) {
                            Text(
                                text = m.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) PureWhite else DarkInk,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rank,
                        onValueChange = { rank = it.filter { c -> c.isDigit() } },
                        label = { Text("Rank") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wins,
                        onValueChange = { wins = it.filter { c -> c.isDigit() } },
                        label = { Text("Wins") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kills,
                        onValueChange = { kills = it.filter { c -> c.isDigit() } },
                        label = { Text("Kills") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = points,
                        onValueChange = { points = it.filter { c -> c.isDigit() } },
                        label = { Text("Points") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = rewardCredits,
                    onValueChange = { rewardCredits = it.filter { c -> c.isDigit() } },
                    label = { Text("Reward Credits (Non-Cash)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = selectedUser != null && selectedMatch != null,
                onClick = {
                    val u = selectedUser ?: return@Button
                    val m = selectedMatch ?: return@Button
                    onPublish(
                        u.userId,
                        u.name,
                        u.freeFireName.ifBlank { u.name },
                        m.id,
                        m.title,
                        m.modeEnum,
                        wins.toLongOrNull() ?: 1L,
                        kills.toLongOrNull() ?: 0L,
                        points.toLongOrNull() ?: 0L,
                        rank.toLongOrNull() ?: 1L,
                        rewardCredits.toLongOrNull() ?: 0L
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
            ) {
                Text("Publish Winner")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AdminNotificationDialog(
    onSend: (title: String, message: String, type: String, targetUserId: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("MATCH") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Broadcast Notification", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("MATCH", "RESULT", "SYSTEM", "SECURITY").forEach { t ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (type == t) RoyalPurple else SoftLavender,
                            modifier = Modifier.clickable { type = t }
                        ) {
                            Text(
                                text = t,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (type == t) PureWhite else DarkInk,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSend(title, message, type, "ALL")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
            ) {
                Text("Send to All Players")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AdminAuditLogsDialog(
    logs: List<AuditLog>,
    onDismiss: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Activity & Audit Logs (${logs.size})", style = MaterialTheme.typography.titleMedium) },
        text = {
            if (logs.isEmpty()) {
                Text("No audit entries recorded yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    modifier = Modifier.height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftLavender
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${log.actorRole}: ${log.action}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = RoyalPurple
                                    )
                                    log.createdAt?.toDate()?.let { d ->
                                        Text(
                                            text = formatter.format(d),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedPurple
                                        )
                                    }
                                }
                                Text(
                                    text = log.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkInk
                                )
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

@Composable
fun AdminCreditsDisabledDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment Review Status") },
        text = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftYellowBg,
                border = BorderStroke(1.dp, CardLavenderBorder)
            ) {
                Text(
                    text = "Real-money deposits, UTR payment approvals, paid entry-fee collection, and cash withdrawals are disabled until legal & age eligibility review is completed. Use 'Users Manage' to award non-cash tournament credits.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkInk,
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Understood") }
        }
    )
}
