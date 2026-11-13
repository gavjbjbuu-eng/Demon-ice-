package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.CreditTransaction
import com.example.data.model.HostRecord
import com.example.data.model.MatchResult
import com.example.data.model.MatchStatus
import com.example.data.model.Registration
import com.example.data.model.RoomDetails
import com.example.data.model.Tournament
import com.example.data.model.TournamentMode
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.IceEsportsRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class IceEsportsViewModel(
    private val repository: IceEsportsRepository,
    private val currentUserId: String,
    private val firebaseUser: FirebaseUser
) : ViewModel() {

    private val _userRole = MutableStateFlow(UserRole.PLAYER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _hostBlockStatus = MutableStateFlow<Pair<Boolean, String?>>(false to null)
    val hostBlockStatus: StateFlow<Pair<Boolean, String?>> = _hostBlockStatus.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _selectedModeFilter = MutableStateFlow<TournamentMode?>(null)
    val selectedModeFilter: StateFlow<TournamentMode?> = _selectedModeFilter.asStateFlow()

    private val _leaderboardModeFilter = MutableStateFlow<String>("ALL")
    val leaderboardModeFilter: StateFlow<String> = _leaderboardModeFilter.asStateFlow()

    private val _roomDetailsCache = MutableStateFlow<Map<String, RoomDetails?>>(emptyMap())
    val roomDetailsCache: StateFlow<Map<String, RoomDetails?>> = _roomDetailsCache.asStateFlow()

    private val _selectedMatchForRegistrations = MutableStateFlow<String?>(null)

    val userProfile: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreError(error) ?: error
            Log.w(TAG, "Error observing profile", root)
            emit(UiState.Error(root.message ?: "Failed to load player profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val tournaments: StateFlow<UiState<List<Tournament>>> = repository.observeTournaments()
        .map<List<Tournament>, UiState<List<Tournament>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreError(error) ?: error
            Log.w(TAG, "Error observing tournaments", root)
            emit(UiState.Error(root.message ?: "Failed to load tournaments"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val myRegistrations: StateFlow<List<Registration>> = repository.observeMyRegistrations(currentUserId)
        .catch { error ->
            Log.w(TAG, "Error observing registrations", unwrapFirestoreError(error) ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val leaderboard: StateFlow<UiState<List<MatchResult>>> = _leaderboardModeFilter
        .flatMapLatest { filter ->
            repository.observeLeaderboard(filter)
                .map<List<MatchResult>, UiState<List<MatchResult>>> { UiState.Success(it) }
                .catch { error ->
                    val root = unwrapFirestoreError(error) ?: error
                    Log.w(TAG, "Error observing leaderboard", root)
                    emit(UiState.Error(root.message ?: "Failed to load leaderboard"))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val notifications: StateFlow<UiState<List<AppNotification>>> = repository.observeNotifications(currentUserId)
        .map<List<AppNotification>, UiState<List<AppNotification>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreError(error) ?: error
            Log.w(TAG, "Error observing notifications", root)
            emit(UiState.Error(root.message ?: "Failed to load notifications"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val transactions: StateFlow<UiState<List<CreditTransaction>>> = repository.observeTransactions(currentUserId)
        .map<List<CreditTransaction>, UiState<List<CreditTransaction>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreError(error) ?: error
            Log.w(TAG, "Error observing transactions", root)
            emit(UiState.Error(root.message ?: "Failed to load credit history"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    // Staff-only reactive streams (only active when backend confirms ADMIN or HOST role)
    @OptIn(ExperimentalCoroutinesApi::class)
    val allUsers: StateFlow<List<UserProfile>> = _userRole
        .flatMapLatest { role ->
            if (role == UserRole.ADMIN) {
                repository.observeAllUsers().catch { emit(emptyList()) }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allHosts: StateFlow<List<HostRecord>> = _userRole
        .flatMapLatest { role ->
            if (role == UserRole.ADMIN) {
                repository.observeAllHosts().catch { emit(emptyList()) }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val auditLogs: StateFlow<List<AuditLog>> = _userRole
        .flatMapLatest { role ->
            if (role == UserRole.ADMIN) {
                repository.observeAuditLogs().catch { emit(emptyList()) }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMatchRegistrations: StateFlow<List<Registration>> = _selectedMatchForRegistrations
        .flatMapLatest { tournamentId ->
            val role = _userRole.value
            if (tournamentId.isNullOrBlank() || role == UserRole.PLAYER) {
                flowOf(emptyList())
            } else {
                val hostFilter = if (role == UserRole.HOST) currentUserId else null
                repository.observeRegistrationsForTournament(tournamentId, hostFilter)
                    .catch { emit(emptyList()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    init {
        initializeSession()
    }

    private fun initializeSession() {
        viewModelScope.launch {
            launch { repository.ensureUserProfile(firebaseUser) }
            val (role, blockPair) = repository.resolveStaffAccess(currentUserId, firebaseUser.email)
            _userRole.value = role
            _hostBlockStatus.value = blockPair
        }
    }

    fun refreshRole() {
        viewModelScope.launch {
            val (role, blockPair) = repository.resolveStaffAccess(currentUserId, firebaseUser.email)
            _userRole.value = role
            _hostBlockStatus.value = blockPair
        }
    }

    fun selectModeFilter(mode: TournamentMode?) {
        _selectedModeFilter.value = if (_selectedModeFilter.value == mode) null else mode
    }

    fun setLeaderboardModeFilter(mode: String) {
        _leaderboardModeFilter.value = mode
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun showMessage(message: String) {
        _actionMessage.value = message
    }

    fun updateProfile(
        name: String,
        freeFireName: String,
        freeFireUid: String,
        avatarBase64: String?
    ) {
        if (name.isBlank()) {
            _actionMessage.value = "Player name cannot be empty."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.updatePlayerProfile(currentUserId, name, freeFireName, freeFireUid, avatarBase64)
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) {
                "Profile updated successfully!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to update profile."
            }
        }
    }

    fun loadRoomDetails(tournamentId: String) {
        viewModelScope.launch {
            val res = repository.getRoomDetails(tournamentId)
            if (res.isSuccess) {
                _roomDetailsCache.value = _roomDetailsCache.value + (tournamentId to res.getOrNull())
            }
        }
    }

    fun joinTournament(tournament: Tournament, profile: UserProfile) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.joinTournament(tournament, profile)
            _isBusy.value = false
            if (res.isSuccess) {
                _actionMessage.value = "Slot confirmed! Room ID & Password unlocked."
                loadRoomDetails(tournament.id)
            } else {
                _actionMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Could not join match."
            }
        }
    }

    fun selectMatchForRegistrations(tournamentId: String?) {
        _selectedMatchForRegistrations.value = tournamentId
    }

    fun saveTournament(
        existingTournament: Tournament?,
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
    ) {
        if (title.trim().length < 3) {
            _actionMessage.value = "Match title must be at least 3 characters."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.createOrUpdateTournament(
                existingTournament = existingTournament,
                title = title,
                subtitle = subtitle.ifBlank { mode.subtitle },
                mode = mode,
                status = status,
                bannerKey = bannerKey,
                bannerBase64 = bannerBase64,
                entryLabel = entryLabel.ifBlank { "Free Entry • Non-Cash" },
                prizeLabel = prizeLabel.ifBlank { "500 Tournament Credits" },
                totalSlots = totalSlots,
                scheduleText = scheduleText.ifBlank { "Today • 8:00 PM" },
                rules = rules.ifBlank { "Fair play only. No teaming or third-party tools." },
                roomId = roomId,
                roomPassword = roomPassword,
                actorRole = _userRole.value
            )
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) {
                if (existingTournament == null) "Tournament created!" else "Tournament updated!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to save tournament."
            }
        }
    }

    fun updateRoomCredentials(tournamentId: String, roomId: String, roomPassword: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.updateRoomCredentials(tournamentId, roomId, roomPassword, _userRole.value)
            _isBusy.value = false
            if (res.isSuccess) {
                loadRoomDetails(tournamentId)
                _actionMessage.value = "Room ID & Password updated!"
            } else {
                _actionMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to update room details."
            }
        }
    }

    fun deleteTournament(tournamentId: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.deleteTournament(tournamentId, _userRole.value)
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) {
                "Tournament deleted."
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to delete tournament."
            }
        }
    }

    fun toggleUserBlock(targetUserId: String, blocked: Boolean, reason: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.setUserBlocked(targetUserId, blocked, reason)
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) {
                if (blocked) "Player account blocked." else "Player account unblocked."
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to update block status."
            }
        }
    }

    fun awardCredits(targetUserId: String, amountCredits: Long, title: String, note: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.awardCreditsToPlayer(targetUserId, amountCredits, title, note)
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) {
                "Tournament credits updated!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to award credits."
            }
        }
    }

    fun createHost(name: String, email: String, existingUserId: String?, assignedMode: String) {
        if (name.isBlank() || email.isBlank()) {
            _actionMessage.value = "Host name and Gmail address are required."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.createHostAccount(name, email, existingUserId, assignedMode)
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) {
                "Host account authorized!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to create host."
            }
        }
    }

    fun toggleHostBlock(hostId: String, blocked: Boolean, reason: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.setHostBlocked(hostId, blocked, reason)
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) {
                if (blocked) "Host blocked." else "Host unblocked."
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to update host status."
            }
        }
    }

    fun publishMatchResult(
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
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.publishResult(
                userId = userId,
                playerName = playerName,
                freeFireName = freeFireName,
                tournamentId = tournamentId,
                tournamentTitle = tournamentTitle,
                mode = mode,
                wins = wins,
                kills = kills,
                points = points,
                rank = rank,
                rewardCredits = rewardCredits
            )
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) {
                "Winning record published to leaderboard!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to publish result."
            }
        }
    }

    fun sendNotification(title: String, message: String, type: String, targetUserId: String) {
        if (title.isBlank() || message.isBlank()) {
            _actionMessage.value = "Title and message are required."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.sendNotification(title, message, type, targetUserId)
            _isBusy.value = false
            _actionMessage.value = if (res.isSuccess) {
                "Notification sent!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to send notification."
            }
        }
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? {
        return generateSequence<Throwable>(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }

    private companion object {
        const val TAG = "IceEsportsVM"
    }
}
