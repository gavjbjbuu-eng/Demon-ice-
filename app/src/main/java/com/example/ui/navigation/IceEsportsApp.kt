package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.auth.AuthHelper
import com.example.data.model.Tournament
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.IceEsportsRepository
import com.example.ui.components.CustomerSupportRow
import com.example.ui.components.LoadingStateBox
import com.example.ui.components.MascotAvatar
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BlockedScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HostPanelScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MatchDetailScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.MyMatchesScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WalletCreditsScreen
import com.example.ui.theme.DarkInk
import com.example.ui.theme.EsportsYellow
import com.example.ui.theme.MutedPurple
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SoftLavender
import com.example.ui.viewmodel.IceEsportsViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private enum class MainTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    MY_MATCHES("My Matches", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    LEADERBOARD("Leaderboard", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard),
    NOTIFICATIONS("Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    MORE("More", Icons.Filled.Settings, Icons.Outlined.Settings)
}

private sealed interface OverlayRoute {
    data object None : OverlayRoute
    data class MatchDetail(val tournamentId: String) : OverlayRoute
    data object CreditsHistory : OverlayRoute
    data object StaffPortal : OverlayRoute
}

@Composable
fun IceEsportsApp() {
    var showSplash by remember { mutableStateOf(true) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        // Key by uid so signing out or switching accounts resets ViewModels & Compose state
        key(user.uid) {
            AuthenticatedIceEsportsShell(
                firebaseUser = user,
                onSignedOut = { currentUser = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedIceEsportsShell(
    firebaseUser: FirebaseUser,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: IceEsportsViewModel = viewModel(
        key = firebaseUser.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                IceEsportsViewModel(
                    repository = IceEsportsRepository(db, Firebase.auth),
                    currentUserId = firebaseUser.uid,
                    firebaseUser = firebaseUser
                )
            }
        }
    )

    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val tournamentsState by viewModel.tournaments.collectAsStateWithLifecycle()
    val registrations by viewModel.myRegistrations.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboard.collectAsStateWithLifecycle()
    val notificationsState by viewModel.notifications.collectAsStateWithLifecycle()
    val transactionsState by viewModel.transactions.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val hostBlocked by viewModel.hostBlockStatus.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedModeFilter.collectAsStateWithLifecycle()
    val leaderboardFilter by viewModel.leaderboardModeFilter.collectAsStateWithLifecycle()
    val roomCache by viewModel.roomDetailsCache.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allHosts by viewModel.allHosts.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val matchRegistrations by viewModel.selectedMatchRegistrations.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    val performSignOut: () -> Unit = {
        AuthHelper.signOut(
            credentialManager = credentialManager,
            scope = scope,
            onSignOutComplete = onSignedOut
        )
    }

    val profile = when (val s = profileState) {
        is UiState.Success -> s.data
        else -> null
    }

    // Immediate real-time account block enforcement
    if (profile != null && profile.isBlocked) {
        BlockedScreen(
            reason = profile.blockReason ?: "Suspicious Activity",
            onSignOut = performSignOut
        )
        return
    }

    if (profileState is UiState.Loading || profile == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = OffWhite) {
            LoadingStateBox("Connecting to Ice Esports Cloud...")
        }
        return
    }

    var activeTab by remember { mutableStateOf(MainTab.HOME) }
    var overlay by remember { mutableStateOf<OverlayRoute>(OverlayRoute.None) }
    var showQuickDrawer by remember { mutableStateOf(false) }
    var showAllMatchesToggle by remember { mutableStateOf(false) }

    val joinedIds = remember(registrations) { registrations.map { it.tournamentId }.toSet() }
    val tournamentsList = remember(tournamentsState) {
        (tournamentsState as? UiState.Success)?.data ?: emptyList()
    }
    val unreadNotifCount = remember(notificationsState) {
        (notificationsState as? UiState.Success)?.data?.size ?: 0
    }

    if (overlay != OverlayRoute.None) {
        BackHandler {
            overlay = OverlayRoute.None
        }
    }

    when (val route = overlay) {
        is OverlayRoute.MatchDetail -> {
            val match = tournamentsList.firstOrNull { it.id == route.tournamentId }
            if (match == null) {
                overlay = OverlayRoute.None
            } else {
                MatchDetailScreen(
                    tournament = match,
                    profile = profile,
                    userRole = userRole,
                    isJoined = match.id in joinedIds,
                    roomDetails = roomCache[match.id],
                    isBusy = isBusy,
                    onLoadRoomDetails = { viewModel.loadRoomDetails(match.id) },
                    onJoinNow = { viewModel.joinTournament(match, profile) },
                    onBack = { overlay = OverlayRoute.None }
                )
            }
            return
        }
        OverlayRoute.CreditsHistory -> {
            WalletCreditsScreen(
                profile = profile,
                transactionsState = transactionsState,
                onBack = { overlay = OverlayRoute.None }
            )
            return
        }
        OverlayRoute.StaffPortal -> {
            when (userRole) {
                UserRole.ADMIN -> {
                    AdminPanelScreen(
                        profile = profile,
                        users = allUsers,
                        hosts = allHosts,
                        tournaments = tournamentsList,
                        auditLogs = auditLogs,
                        registrationsForSelected = matchRegistrations,
                        roomDetailsCache = roomCache,
                        onSelectMatchRegistrations = viewModel::selectMatchForRegistrations,
                        onLoadRoomDetails = viewModel::loadRoomDetails,
                        onSaveTournament = viewModel::saveTournament,
                        onDeleteTournament = viewModel::deleteTournament,
                        onUpdateRoomCreds = viewModel::updateRoomCredentials,
                        onToggleUserBlock = viewModel::toggleUserBlock,
                        onAwardCredits = viewModel::awardCredits,
                        onCreateHost = viewModel::createHost,
                        onToggleHostBlock = viewModel::toggleHostBlock,
                        onPublishResult = viewModel::publishMatchResult,
                        onSendNotification = viewModel::sendNotification,
                        onBack = { overlay = OverlayRoute.None }
                    )
                    return
                }
                UserRole.HOST -> {
                    HostPanelScreen(
                        profile = profile,
                        hostBlocked = hostBlocked,
                        allTournaments = tournamentsList,
                        registrationsForSelected = matchRegistrations,
                        roomDetailsCache = roomCache,
                        onSelectMatchRegistrations = viewModel::selectMatchForRegistrations,
                        onLoadRoomDetails = viewModel::loadRoomDetails,
                        onSaveTournament = viewModel::saveTournament,
                        onUpdateRoomCreds = viewModel::updateRoomCredentials,
                        onBack = { overlay = OverlayRoute.None }
                    )
                    return
                }
                UserRole.PLAYER -> {
                    overlay = OverlayRoute.None
                }
            }
        }
        OverlayRoute.None -> Unit
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = OffWhite,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = PureWhite,
                        windowInsets = WindowInsets.navigationBars,
                        tonalElevation = 8.dp
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (tab != MainTab.MY_MATCHES) showAllMatchesToggle = false
                                    activeTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalPurple,
                                    selectedTextColor = RoyalPurple,
                                    indicatorColor = SoftLavender,
                                    unselectedIconColor = MutedPurple,
                                    unselectedTextColor = MutedPurple
                                ),
                                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = PureWhite
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        MainTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    if (tab != MainTab.MY_MATCHES) showAllMatchesToggle = false
                                    activeTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = RoyalPurple,
                                    selectedTextColor = RoyalPurple,
                                    indicatorColor = SoftLavender,
                                    unselectedIconColor = MutedPurple,
                                    unselectedTextColor = MutedPurple
                                )
                            )
                        }
                    }
                }

                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (activeTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                profile = profile,
                                tournamentsState = tournamentsState,
                                joinedTournamentIds = joinedIds,
                                selectedMode = selectedMode,
                                unreadNotificationCount = unreadNotifCount,
                                onSelectMode = viewModel::selectModeFilter,
                                onOpenMenu = { showQuickDrawer = true },
                                onOpenNotifications = { activeTab = MainTab.NOTIFICATIONS },
                                onOpenCredits = { overlay = OverlayRoute.CreditsHistory },
                                onOpenMatch = { match -> overlay = OverlayRoute.MatchDetail(match.id) },
                                onSeeAllMatches = {
                                    showAllMatchesToggle = true
                                    activeTab = MainTab.MY_MATCHES
                                }
                            )
                        }
                        MainTab.MY_MATCHES -> {
                            MyMatchesScreen(
                                tournamentsState = tournamentsState,
                                registrations = registrations,
                                showAllPublicFallback = showAllMatchesToggle,
                                onOpenMatch = { match -> overlay = OverlayRoute.MatchDetail(match.id) }
                            )
                        }
                        MainTab.LEADERBOARD -> {
                            LeaderboardScreen(
                                leaderboardState = leaderboardState,
                                selectedFilter = leaderboardFilter,
                                onSelectFilter = viewModel::setLeaderboardModeFilter
                            )
                        }
                        MainTab.NOTIFICATIONS -> {
                            NotificationsScreen(
                                notificationsState = notificationsState
                            )
                        }
                        MainTab.MORE -> {
                            MoreScreen(
                                profile = profile,
                                userRole = userRole,
                                isBusy = isBusy,
                                onUpdateProfile = viewModel::updateProfile,
                                onOpenCreditsHistory = { overlay = OverlayRoute.CreditsHistory },
                                onOpenStaffPortal = {
                                    viewModel.refreshRole()
                                    overlay = OverlayRoute.StaffPortal
                                },
                                onSignOut = performSignOut
                            )
                        }
                    }
                }
            }
        }
    }

    if (showQuickDrawer) {
        ModalBottomSheet(
            onDismissRequest = { showQuickDrawer = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            QuickMenuSheetContent(
                profile = profile,
                onNavigateTab = { tab ->
                    showQuickDrawer = false
                    activeTab = tab
                },
                onOpenCredits = {
                    showQuickDrawer = false
                    overlay = OverlayRoute.CreditsHistory
                },
                onSignOut = {
                    showQuickDrawer = false
                    performSignOut()
                }
            )
        }
    }
}

@Composable
private fun QuickMenuSheetContent(
    profile: UserProfile,
    onNavigateTab: (MainTab) -> Unit,
    onOpenCredits: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MascotAvatar(avatarBase64 = profile.avatarBase64, size = 48.dp, borderColor = EsportsYellow)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(profile.name, style = MaterialTheme.typography.titleMedium, color = DarkInk)
                Text("${profile.iceId} • ${profile.creditsBalance} Credits", style = MaterialTheme.typography.bodySmall, color = MutedPurple)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        TextButton(onClick = { onNavigateTab(MainTab.MY_MATCHES) }, modifier = Modifier.fillMaxWidth()) {
            Text("My Matches & Room IDs", color = RoyalPurple)
        }
        TextButton(onClick = { onNavigateTab(MainTab.LEADERBOARD) }, modifier = Modifier.fillMaxWidth()) {
            Text("Leaderboard Standings", color = RoyalPurple)
        }
        TextButton(onClick = onOpenCredits, modifier = Modifier.fillMaxWidth()) {
            Text("Non-Cash Tournament Credits", color = RoyalPurple)
        }
        TextButton(onClick = { onNavigateTab(MainTab.MORE) }, modifier = Modifier.fillMaxWidth()) {
            Text("Profile, Settings & Support", color = RoyalPurple)
        }
        Spacer(modifier = Modifier.height(8.dp))
        CustomerSupportRow(darkText = true)
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Sign Out", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
