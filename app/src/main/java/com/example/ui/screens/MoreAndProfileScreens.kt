package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CreditTransaction
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.CustomerSupportRow
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LoadingStateBox
import com.example.ui.components.MascotAvatar
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
import com.example.ui.theme.SoftYellowBg
import com.example.ui.theme.SplashDarkPurple
import com.example.ui.theme.StatusOnlineGreen
import com.example.ui.viewmodel.UiState
import com.example.util.ImageUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MoreScreen(
    profile: UserProfile,
    userRole: UserRole,
    isBusy: Boolean,
    onUpdateProfile: (name: String, ffName: String, ffUid: String, avatarBase64: String?) -> Unit,
    onOpenCreditsHistory: () -> Unit,
    onOpenStaffPortal: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var showSettingsInfo by remember { mutableStateOf(false) }
    var pendingAvatarBase64 by remember(profile.avatarBase64) { mutableStateOf(profile.avatarBase64) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val encoded = ImageUtils.uriToCompressedBase64(context, uri, maxDimension = 384, quality = 70)
                if (encoded != null) {
                    pendingAvatarBase64 = encoded
                    onUpdateProfile(
                        profile.name,
                        profile.freeFireName,
                        profile.freeFireUid,
                        encoded
                    )
                }
            }
        }
    }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Player Hub",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PureWhite
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RoyalPurple
                ) {
                    Text(
                        text = profile.iceId,
                        style = MaterialTheme.typography.labelMedium,
                        color = EsportsYellow,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Player Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CardLavenderBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                MascotAvatar(
                                    avatarBase64 = pendingAvatarBase64,
                                    size = 72.dp,
                                    borderColor = EsportsYellow
                                )
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(28.dp)
                                        .clickable {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .testTag("upload_avatar_button"),
                                    shape = CircleShape,
                                    color = RoyalPurple,
                                    border = BorderStroke(1.5.dp, PureWhite)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Upload profile picture",
                                            tint = PureWhite,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.name.ifBlank { "Player" },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = DarkInk
                                )
                                Text(
                                    text = profile.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedPurple
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SoftLavender
                                    ) {
                                        Text(
                                            text = "Wins: ${profile.wins}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RoyalPurple,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SoftYellowBg
                                    ) {
                                        Text(
                                            text = "${profile.creditsBalance} Credits",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DarkInk,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SoftLavender
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Free Fire Game Name: ${profile.freeFireName.ifBlank { "Not set" }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkInk
                                )
                                Text(
                                    text = "Free Fire UID: ${profile.freeFireUid.ifBlank { "Not set" }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkInk
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showEditDialog = true },
                            enabled = !isBusy,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalPurple,
                                contentColor = PureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Player & Free Fire Details", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // Wallet / Non-cash Tournament Credits Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenCreditsHistory)
                        .testTag("more_credits_history_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CardLavenderBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SoftYellowBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = RoyalPurple
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tournament Credits & History",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DarkInk
                                )
                                Text(
                                    text = "Non-cash credits • Real-money payments disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedPurple
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EsportsYellow
                        ) {
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkInk,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Customer Support Card (Circular WhatsApp & YouTube icons)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CardLavenderBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Customer Support & Official Channel",
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap below for WhatsApp helpdesk or watch matches on Official Gamer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedPurple
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CustomerSupportRow(darkText = true)
                    }
                }
            }

            // Settings & Account Security Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSettingsInfo = !showSettingsInfo },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CardLavenderBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = RoyalPurple
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Settings & Account Security",
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkInk
                            )
                        }
                        if (showSettingsInfo) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "• Signed in via Google Credential Manager\n• Cloud Firestore role-based access rules enforced\n• Real-money deposits & cash payouts disabled pending legal & age review",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedPurple
                            )
                        }
                    }
                }
            }

            // PRIVATE STAFF PORTAL: Only rendered when Firestore backend confirms ADMIN or HOST role!
            // Completely hidden from normal players!
            if (userRole == UserRole.ADMIN || userRole == UserRole.HOST) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenStaffPortal)
                            .testTag("private_staff_portal_entry"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepPurple),
                        border = BorderStroke(1.5.dp, EsportsYellow)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(SplashDarkPurple, RoyalPurple))
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (userRole == UserRole.ADMIN) {
                                        Icons.Default.AdminPanelSettings
                                    } else {
                                        Icons.Default.Shield
                                    },
                                    contentDescription = null,
                                    tint = EsportsYellow,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (userRole == UserRole.ADMIN) "Admin Console" else "Host Console",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "Verified role • Private management portal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EsportsYellow
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Verified",
                                tint = EsportsYellow
                            )
                        }
                    }
                }
            }

            // Sign Out
            item {
                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AlertBlockedRed.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertBlockedRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (showEditDialog) {
        ProfileEditDialog(
            profile = profile,
            onDismiss = { showEditDialog = false },
            onSave = { newName, ffName, ffUid ->
                showEditDialog = false
                onUpdateProfile(newName, ffName, ffUid, null)
            }
        )
    }
}

@Composable
private fun ProfileEditDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, ffName: String, ffUid: String) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var ffName by remember { mutableStateOf(profile.freeFireName) }
    var ffUid by remember { mutableStateOf(profile.freeFireUid) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Player Profile", style = MaterialTheme.typography.titleLarge, color = DarkInk)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ffName,
                    onValueChange = { ffName = it },
                    label = { Text("Free Fire Game Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ffUid,
                    onValueChange = { ffUid = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Free Fire UID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, ffName, ffUid) },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WalletCreditsScreen(
    profile: UserProfile,
    transactionsState: UiState<List<CreditTransaction>>,
    onBack: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
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
                    modifier = Modifier.testTag("credits_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite
                    )
                }
                Text(
                    text = "Tournament Credits",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepPurple)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(DeepPurple, RoyalPurple, ElectricPurple)))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Available Non-Cash Credits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PureWhite.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${profile.creditsBalance} Credits",
                            style = MaterialTheme.typography.displayLarge,
                            color = EsportsYellow
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Earned through verified tournament placements & Admin awards.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PureWhite
                        )
                    }
                }
            }

            // Compliance Notice per Requirement 9
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftYellowBg),
                    border = BorderStroke(1.dp, EsportsYellow)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = DarkInk
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Payments & Cash Payouts Disabled",
                                style = MaterialTheme.typography.titleSmall,
                                color = DarkInk
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Real-money deposits, cash entry-fee collection, and cash withdrawals are currently disabled pending legal and age eligibility review. All tournaments run on non-cash tournament credits.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkInk.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Credit Transaction History",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkInk
                )
            }

            when (transactionsState) {
                is UiState.Loading -> {
                    item { LoadingStateBox("Loading credit history...") }
                }
                is UiState.Error -> {
                    item {
                        EmptyStateCard(
                            title = "Could not load transactions",
                            subtitle = transactionsState.message
                        )
                    }
                }
                is UiState.Success -> {
                    val txList = transactionsState.data
                    if (txList.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No credit transactions yet",
                                subtitle = "Your match reward credits and Admin awards will appear here."
                            )
                        }
                    } else {
                        items(txList, key = { it.id }) { tx ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, CardLavenderBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tx.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = DarkInk
                                        )
                                        if (tx.note.isNotBlank()) {
                                            Text(
                                                text = tx.note,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedPurple
                                            )
                                        }
                                        tx.createdAt?.toDate()?.let { date ->
                                            Text(
                                                text = formatter.format(date),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MutedPurple.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (tx.amountCredits >= 0) "+${tx.amountCredits}" else "${tx.amountCredits}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (tx.amountCredits >= 0) StatusOnlineGreen else AlertBlockedRed,
                                        fontWeight = FontWeight.ExtraBold
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
