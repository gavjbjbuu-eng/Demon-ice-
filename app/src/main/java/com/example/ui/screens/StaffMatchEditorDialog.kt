package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.MatchStatus
import com.example.data.model.RoomDetails
import com.example.data.model.Tournament
import com.example.data.model.TournamentMode
import com.example.ui.theme.CardLavenderBorder
import com.example.ui.theme.DarkInk
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalPurple
import com.example.util.ImageUtils
import kotlinx.coroutines.launch

@Composable
fun StaffMatchEditorDialog(
    existing: Tournament?,
    existingRoom: RoomDetails?,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var subtitle by remember { mutableStateOf(existing?.subtitle ?: "") }
    var mode by remember { mutableStateOf(existing?.modeEnum ?: TournamentMode.CS) }
    var status by remember { mutableStateOf(existing?.statusEnum ?: MatchStatus.UPCOMING) }
    var entryLabel by remember { mutableStateOf(existing?.entryLabel ?: "Free Entry • Non-Cash") }
    var prizeLabel by remember { mutableStateOf(existing?.prizeLabel ?: "500 Tournament Credits") }
    var totalSlotsText by remember { mutableStateOf((existing?.totalSlots ?: 48L).toString()) }
    var scheduleText by remember { mutableStateOf(existing?.scheduleText ?: "Today • 8:00 PM") }
    var rules by remember {
        mutableStateOf(
            existing?.rules ?: "1. Register with valid Free Fire UID.\n2. Join custom room 10 mins early.\n3. Fair play only."
        )
    }
    var roomId by remember { mutableStateOf(existingRoom?.roomId ?: "") }
    var roomPassword by remember { mutableStateOf(existingRoom?.roomPassword ?: "") }
    var bannerBase64 by remember { mutableStateOf(existing?.bannerBase64) }

    val bannerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val encoded = ImageUtils.uriToCompressedBase64(context, uri, maxDimension = 720, quality = 65)
                if (encoded != null) {
                    bannerBase64 = encoded
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) "Create Tournament Match" else "Edit Tournament Match",
                style = MaterialTheme.typography.titleLarge,
                color = DarkInk
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Match Title *") },
                    placeholder = { Text("e.g. CS Clash Cup #1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle") },
                    placeholder = { Text("e.g. 5v5 - Clash Squad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Game Mode", style = MaterialTheme.typography.labelMedium, color = DarkInk)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TournamentMode.entries.forEach { m ->
                        val active = mode == m
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) RoyalPurple else PureWhite,
                            border = BorderStroke(1.dp, if (active) RoyalPurple else CardLavenderBorder),
                            modifier = Modifier.clickable { mode = m }
                        ) {
                            Text(
                                text = m.shortTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) PureWhite else DarkInk,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text("Match Status", style = MaterialTheme.typography.labelMedium, color = DarkInk)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MatchStatus.entries.forEach { st ->
                        val active = status == st
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) RoyalPurple else PureWhite,
                            border = BorderStroke(1.dp, if (active) RoyalPurple else CardLavenderBorder),
                            modifier = Modifier.clickable { status = st }
                        ) {
                            Text(
                                text = st.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) PureWhite else DarkInk,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalSlotsText,
                        onValueChange = { totalSlotsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Total Slots") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = scheduleText,
                        onValueChange = { scheduleText = it },
                        label = { Text("Date & Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                }

                OutlinedTextField(
                    value = entryLabel,
                    onValueChange = { entryLabel = it },
                    label = { Text("Entry Info (Non-Cash)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prizeLabel,
                    onValueChange = { prizeLabel = it },
                    label = { Text("Credit Prize Info") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roomId,
                        onValueChange = { roomId = it },
                        label = { Text("Room ID (Optional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = roomPassword,
                        onValueChange = { roomPassword = it },
                        label = { Text("Room Password") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = rules,
                    onValueChange = { rules = it },
                    label = { Text("Match Rules") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (bannerBase64 != null) "Custom Banner Selected (Tap to Replace)" else "Upload Custom Banner (Optional)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val slots = totalSlotsText.toLongOrNull()?.coerceIn(2L, 200L) ?: 48L
                    onSave(
                        title,
                        subtitle,
                        mode,
                        status,
                        "banner_${mode.code.lowercase()}",
                        bannerBase64,
                        entryLabel,
                        prizeLabel,
                        slots,
                        scheduleText,
                        rules,
                        roomId,
                        roomPassword
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
            ) {
                Text(if (existing == null) "Publish Match" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
