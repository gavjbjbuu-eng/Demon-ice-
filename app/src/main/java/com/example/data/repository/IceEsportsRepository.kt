package com.example.data.repository

import android.content.Context
import com.example.R
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class IceEsportsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private fun requireUser(): FirebaseUser {
        return auth.currentUser
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun requireUserId(): String = requireUser().uid

    private fun generateIceId(uid: String): String {
        val clean = uid.filter { it.isLetterOrDigit() }.uppercase()
        val suffix = if (clean.length >= 5) clean.take(5) else clean.padEnd(5, '0')
        return "ICE$suffix"
    }

    suspend fun ensureUserProfile(user: FirebaseUser): Result<UserProfile> {
        val docRef = db.collection("users").document(user.uid)
        return try {
            val snap = docRef.get().await()
            val profile = if (snap.exists()) {
                snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: UserProfile(userId = user.uid)
            } else {
                val iceId = generateIceId(user.uid)
                val displayName = user.displayName?.take(60)?.ifBlank { "Ice Player" } ?: "Ice Player"
                val userEmail = user.email ?: "${user.uid}@iceesports.app"
                val payload = mapOf(
                    "userId" to user.uid,
                    "iceId" to iceId,
                    "name" to displayName,
                    "email" to userEmail,
                    "freeFireName" to "",
                    "freeFireUid" to "",
                    "creditsBalance" to 0L,
                    "isBlocked" to false,
                    "matchesPlayed" to 0L,
                    "wins" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                UserProfile(
                    userId = user.uid,
                    iceId = iceId,
                    name = displayName,
                    email = userEmail
                )
            }

            // Claim pending host invite in parallel if Admin pre-authorized this verified email
            val email = user.email?.lowercase()
            if (!email.isNullOrBlank()) {
                runCatching {
                    coroutineScope {
                        val myHostDeferred = async { db.collection("hosts").document(user.uid).get().await() }
                        val inviteDeferred = async { db.collection("hosts").document("invite_$email").get().await() }
                        val myHostDoc = myHostDeferred.await()
                        val inviteDoc = inviteDeferred.await()
                        if (!myHostDoc.exists() && inviteDoc.exists() && inviteDoc.getBoolean("isBlocked") != true) {
                            val claimPayload = mapOf(
                                "hostId" to user.uid,
                                "userId" to user.uid,
                                "name" to (inviteDoc.getString("name") ?: profile.name),
                                "email" to email,
                                "assignedMode" to (inviteDoc.getString("assignedMode") ?: "ALL"),
                                "isBlocked" to false,
                                "createdBy" to (inviteDoc.getString("createdBy") ?: user.uid),
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                            db.collection("hosts").document(user.uid).set(claimPayload).await()
                        }
                    }
                }
            }

            Result.success(profile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun resolveStaffAccess(uid: String, email: String?): Pair<UserRole, Pair<Boolean, String?>> {
        if (email != null && email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true) && auth.currentUser?.isEmailVerified == true) {
            return UserRole.ADMIN to (false to null)
        }
        return try {
            coroutineScope {
                val adminDeferred = async { db.collection("admins").document(uid).get().await() }
                val hostDeferred = async { db.collection("hosts").document(uid).get().await() }
                val adminDoc = adminDeferred.await()
                val hostDoc = hostDeferred.await()

                val isHostBlocked = hostDoc.exists() && hostDoc.getBoolean("isBlocked") == true
                val blockPair = if (isHostBlocked) {
                    true to (hostDoc.getString("blockReason") ?: "Host access suspended by Admin")
                } else {
                    false to null
                }

                val role = when {
                    adminDoc.exists() -> UserRole.ADMIN
                    hostDoc.exists() && !isHostBlocked -> UserRole.HOST
                    else -> UserRole.PLAYER
                }
                role to blockPair
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UserRole.PLAYER to (false to null)
        }
    }

    suspend fun resolveUserRole(uid: String, email: String?): UserRole = resolveStaffAccess(uid, email).first

    suspend fun isHostBlocked(uid: String): Pair<Boolean, String?> {
        return try {
            val hostDoc = db.collection("hosts").document(uid).get().await()
            if (hostDoc.exists() && hostDoc.getBoolean("isBlocked") == true) {
                true to (hostDoc.getString("blockReason") ?: "Host access suspended by Admin")
            } else {
                false to null
            }
        } catch (e: Exception) {
            false to null
        }
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
        val docRef = db.collection("users").document(uid)
        emitAll(
            docRef.snapshots()
                .map { it.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    unwrapAndLog(error, OperationType.GET, docRef.path, uid)
                    throw error
                }
        )
    }

    suspend fun updatePlayerProfile(
        uid: String,
        name: String,
        freeFireName: String,
        freeFireUid: String,
        avatarBase64: String?
    ): Result<Unit> {
        val docRef = db.collection("users").document(uid)
        return try {
            val payload = mutableMapOf<String, Any>(
                "name" to name.trim().take(60),
                "freeFireName" to freeFireName.trim().take(60),
                "freeFireUid" to freeFireUid.trim().take(32),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (avatarBase64 != null) {
                payload["avatarBase64"] = avatarBase64
            }
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    fun observeTournaments(): Flow<List<Tournament>> = flow {
        val uid = requireUserId()
        val path = "tournaments"
        emitAll(
            db.collection(path)
                .whereIn("status", listOf("UPCOMING", "ONGOING", "COMPLETED"))
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Tournament::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeMyRegistrations(uid: String): Flow<List<Registration>> = flow {
        val path = "registrations"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Registration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.joinedAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun getRoomDetails(tournamentId: String): Result<RoomDetails?> {
        val docRef = db.collection("tournaments").document(tournamentId)
            .collection("room").document("details")
        return try {
            val snap = docRef.get().await()
            val details = if (snap.exists()) {
                snap.toObject(RoomDetails::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
            Result.success(details)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun joinTournament(tournament: Tournament, profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        require(tournament.id.isNotBlank()) { "Tournament ID is required." }
        if (profile.isBlocked) {
            return Result.failure(IllegalStateException("Your account is blocked."))
        }
        if (tournament.status != MatchStatus.UPCOMING.code) {
            return Result.failure(IllegalStateException("Registration is closed for this match."))
        }
        if (tournament.joinedSlots >= tournament.totalSlots) {
            return Result.failure(IllegalStateException("All tournament slots are full."))
        }

        val regId = "${tournament.id}_$uid"
        val regRef = db.collection("registrations").document(regId)
        val matchRef = db.collection("tournaments").document(tournament.id)

        return try {
            val (existingReg, latestMatch) = coroutineScope {
                val regDeferred = async { regRef.get().await() }
                val matchDeferred = async { matchRef.get().await() }
                regDeferred.await() to matchDeferred.await()
            }
            if (existingReg.exists()) {
                return Result.failure(IllegalStateException("You have already joined this match."))
            }
            val currentJoined = latestMatch.getLong("joinedSlots") ?: tournament.joinedSlots
            val totalSlots = latestMatch.getLong("totalSlots") ?: tournament.totalSlots
            if (currentJoined >= totalSlots) {
                return Result.failure(IllegalStateException("All slots are full."))
            }

            val regPayload = mapOf(
                "tournamentId" to tournament.id,
                "tournamentTitle" to tournament.title,
                "mode" to tournament.mode,
                "userId" to uid,
                "hostId" to tournament.createdBy.ifBlank { uid },
                "playerName" to profile.name.ifBlank { "Player" },
                "freeFireName" to profile.freeFireName,
                "freeFireUid" to profile.freeFireUid,
                "status" to "CONFIRMED",
                "joinedAt" to FieldValue.serverTimestamp()
            )

            val batch = db.batch()
            batch.set(regRef, regPayload)
            batch.update(
                matchRef,
                mapOf(
                    "joinedSlots" to (currentJoined + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, regRef.path, auth)
            Result.failure(e)
        }
    }

    fun observeLeaderboard(modeFilter: String? = null): Flow<List<MatchResult>> = flow {
        val uid = requireUserId()
        val path = "results"
        val query = if (modeFilter.isNullOrBlank() || modeFilter == "ALL") {
            db.collection(path).whereIn("mode", listOf("ALL", "CS", "BR", "NIGHT_MODE", "SURVIVAL"))
        } else {
            db.collection(path).whereEqualTo("mode", modeFilter)
        }
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MatchResult::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(compareByDescending<MatchResult> { it.wins }.thenByDescending { it.points })
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeNotifications(uid: String): Flow<List<AppNotification>> = flow {
        val path = "notifications"
        emitAll(
            db.collection(path)
                .whereIn("targetUserId", listOf("ALL", uid))
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeTransactions(uid: String): Flow<List<CreditTransaction>> = flow {
        val path = "transactions"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CreditTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    // --- Admin & Host Operations ---

    fun observeAllUsers(): Flow<List<UserProfile>> = flow {
        val uid = requireUserId()
        val path = "users"
        emitAll(
            db.collection(path).snapshots()
                .map { snapshot ->
                    snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeAllHosts(): Flow<List<HostRecord>> = flow {
        val uid = requireUserId()
        val path = "hosts"
        emitAll(
            db.collection(path).snapshots()
                .map { snapshot ->
                    snapshot.toObjects(HostRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeAuditLogs(): Flow<List<AuditLog>> = flow {
        val uid = requireUserId()
        val path = "auditLogs"
        emitAll(
            db.collection(path).snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AuditLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeRegistrationsForTournament(tournamentId: String, hostIdOnly: String?): Flow<List<Registration>> = flow {
        val uid = requireUserId()
        val path = "registrations"
        val query = if (hostIdOnly != null) {
            db.collection(path)
                .whereEqualTo("tournamentId", tournamentId)
                .whereEqualTo("hostId", hostIdOnly)
        } else {
            db.collection(path).whereEqualTo("tournamentId", tournamentId)
        }
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Registration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.joinedAt?.seconds ?: 0L }
                }
                .catch { error ->
                    unwrapAndLog(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun createOrUpdateTournament(
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
        roomPassword: String,
        actorRole: UserRole
    ): Result<String> {
        val user = requireUser()
        val uid = user.uid
        val docRef = if (existingTournament != null && existingTournament.id.isNotBlank()) {
            db.collection("tournaments").document(existingTournament.id)
        } else {
            db.collection("tournaments").document()
        }

        return try {
            if (existingTournament == null) {
                val payload = mapOf(
                    "title" to title.trim().take(80),
                    "subtitle" to subtitle.trim().take(80),
                    "mode" to mode.code,
                    "status" to status.code,
                    "bannerKey" to bannerKey,
                    "bannerBase64" to bannerBase64,
                    "entryLabel" to entryLabel.trim().take(60),
                    "prizeLabel" to prizeLabel.trim().take(60),
                    "totalSlots" to totalSlots.coerceIn(2L, 200L),
                    "joinedSlots" to 0L,
                    "scheduleText" to scheduleText.trim().take(80),
                    "rules" to rules.trim().take(1500),
                    "createdBy" to uid,
                    "createdByRole" to if (actorRole == UserRole.ADMIN) "ADMIN" else "HOST",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            } else {
                val payload = mutableMapOf<String, Any>(
                    "title" to title.trim().take(80),
                    "subtitle" to subtitle.trim().take(80),
                    "mode" to mode.code,
                    "status" to status.code,
                    "bannerKey" to bannerKey,
                    "entryLabel" to entryLabel.trim().take(60),
                    "prizeLabel" to prizeLabel.trim().take(60),
                    "totalSlots" to totalSlots.coerceIn(existingTournament.joinedSlots.coerceAtLeast(2L), 200L),
                    "scheduleText" to scheduleText.trim().take(80),
                    "rules" to rules.trim().take(1500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (bannerBase64 != null) {
                    payload["bannerBase64"] = bannerBase64
                }
                docRef.update(payload).await()
            }

            if (roomId.isNotBlank() || roomPassword.isNotBlank()) {
                updateRoomCredentials(docRef.id, roomId, roomPassword, actorRole)
            }

            writeAuditLog(
                actorRole = actorRole,
                action = if (existingTournament == null) "CREATE_MATCH" else "UPDATE_MATCH",
                targetId = docRef.id,
                details = "${title.trim()} (${mode.code}) [${status.code}]"
            )
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateRoomCredentials(
        tournamentId: String,
        roomId: String,
        roomPassword: String,
        actorRole: UserRole
    ): Result<Unit> {
        val uid = requireUserId()
        val roomRef = db.collection("tournaments").document(tournamentId)
            .collection("room").document("details")
        return try {
            val payload = mapOf(
                "tournamentId" to tournamentId,
                "roomId" to roomId.trim().take(40),
                "roomPassword" to roomPassword.trim().take(40),
                "updatedBy" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            roomRef.set(payload, SetOptions.merge()).await()
            writeAuditLog(
                actorRole = actorRole,
                action = "UPDATE_ROOM_CREDS",
                targetId = tournamentId,
                details = "Updated Room ID: ${roomId.trim()}"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, roomRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteTournament(tournamentId: String, actorRole: UserRole): Result<Unit> {
        val docRef = db.collection("tournaments").document(tournamentId)
        return try {
            docRef.delete().await()
            writeAuditLog(
                actorRole = actorRole,
                action = "DELETE_MATCH",
                targetId = tournamentId,
                details = "Deleted match $tournamentId"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun setUserBlocked(targetUserId: String, blocked: Boolean, reason: String): Result<Unit> {
        val docRef = db.collection("users").document(targetUserId)
        return try {
            val cleanReason = if (blocked) reason.trim().ifBlank { "Suspicious Activity" }.take(200) else ""
            docRef.update(
                mapOf(
                    "isBlocked" to blocked,
                    "blockReason" to cleanReason,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(
                actorRole = UserRole.ADMIN,
                action = if (blocked) "BLOCK_USER" else "UNBLOCK_USER",
                targetId = targetUserId,
                details = if (blocked) "Reason: $cleanReason" else "Account unblocked"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun awardCreditsToPlayer(
        targetUserId: String,
        amountCredits: Long,
        title: String,
        note: String
    ): Result<Unit> {
        val adminUid = requireUserId()
        val userRef = db.collection("users").document(targetUserId)
        val txRef = db.collection("transactions").document()
        return try {
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "creditsBalance" to FieldValue.increment(amountCredits),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                txRef,
                mapOf(
                    "userId" to targetUserId,
                    "title" to title.trim().take(80),
                    "type" to "CREDIT_AWARD",
                    "amountCredits" to amountCredits,
                    "note" to note.trim().take(200),
                    "createdBy" to adminUid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            writeAuditLog(
                actorRole = UserRole.ADMIN,
                action = "AWARD_CREDITS",
                targetId = targetUserId,
                details = "+$amountCredits credits: $title"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun createHostAccount(
        name: String,
        email: String,
        existingUserId: String?,
        assignedMode: String
    ): Result<Unit> {
        val adminUid = requireUserId()
        val cleanEmail = email.trim().lowercase()
        val docId = existingUserId?.takeIf { it.isNotBlank() } ?: "invite_$cleanEmail"
        val docRef = db.collection("hosts").document(docId)
        return try {
            val payload = mapOf(
                "hostId" to docId,
                "userId" to (existingUserId ?: ""),
                "name" to name.trim().take(60),
                "email" to cleanEmail.take(120),
                "assignedMode" to assignedMode,
                "isBlocked" to false,
                "createdBy" to adminUid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            writeAuditLog(
                actorRole = UserRole.ADMIN,
                action = "CREATE_HOST",
                targetId = docId,
                details = "Created host $cleanEmail ($assignedMode)"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun setHostBlocked(hostId: String, blocked: Boolean, reason: String): Result<Unit> {
        val docRef = db.collection("hosts").document(hostId)
        return try {
            val cleanReason = if (blocked) reason.trim().ifBlank { "Policy violation" }.take(200) else ""
            docRef.update(
                mapOf(
                    "isBlocked" to blocked,
                    "blockReason" to cleanReason,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(
                actorRole = UserRole.ADMIN,
                action = if (blocked) "BLOCK_HOST" else "UNBLOCK_HOST",
                targetId = hostId,
                details = if (blocked) "Reason: $cleanReason" else "Host unblocked"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun publishResult(
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
    ): Result<Unit> {
        val adminUid = requireUserId()
        val resultRef = db.collection("results").document()
        return try {
            val batch = db.batch()
            batch.set(
                resultRef,
                mapOf(
                    "userId" to userId,
                    "playerName" to playerName.trim().take(60),
                    "freeFireName" to freeFireName.trim().take(60),
                    "tournamentId" to tournamentId,
                    "tournamentTitle" to tournamentTitle.trim().take(80),
                    "mode" to mode.code,
                    "wins" to wins.coerceAtLeast(0L),
                    "kills" to kills.coerceAtLeast(0L),
                    "points" to points.coerceAtLeast(0L),
                    "rank" to rank.coerceAtLeast(1L),
                    "rewardCredits" to rewardCredits.coerceAtLeast(0L),
                    "publishedBy" to adminUid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            // Update user's win tally & non-cash reward credits if user doc exists
            val userRef = db.collection("users").document(userId)
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                batch.update(
                    userRef,
                    mapOf(
                        "wins" to FieldValue.increment(wins.coerceAtLeast(0L)),
                        "creditsBalance" to FieldValue.increment(rewardCredits.coerceAtLeast(0L)),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (rewardCredits > 0L) {
                    val txRef = db.collection("transactions").document()
                    batch.set(
                        txRef,
                        mapOf(
                            "userId" to userId,
                            "title" to "Match Reward • $tournamentTitle",
                            "type" to "CREDIT_AWARD",
                            "amountCredits" to rewardCredits,
                            "note" to "Rank #$rank • $kills kills",
                            "createdBy" to adminUid,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            batch.commit().await()
            writeAuditLog(
                actorRole = UserRole.ADMIN,
                action = "PUBLISH_RESULT",
                targetId = resultRef.id,
                details = "$playerName Rank #$rank ($tournamentTitle) +$rewardCredits credits"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, resultRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun sendNotification(
        title: String,
        message: String,
        type: String,
        targetUserId: String
    ): Result<Unit> {
        val adminUid = requireUserId()
        val notifRef = db.collection("notifications").document()
        return try {
            notifRef.set(
                mapOf(
                    "title" to title.trim().take(80),
                    "message" to message.trim().take(500),
                    "type" to type,
                    "targetUserId" to targetUserId.ifBlank { "ALL" },
                    "createdBy" to adminUid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(
                actorRole = UserRole.ADMIN,
                action = "SEND_NOTIFICATION",
                targetId = notifRef.id,
                details = "${title.trim()} -> $targetUserId"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, notifRef.path, auth)
            Result.failure(e)
        }
    }

    private suspend fun writeAuditLog(
        actorRole: UserRole,
        action: String,
        targetId: String,
        details: String
    ) {
        val user = auth.currentUser ?: return
        val logId = UUID.randomUUID().toString()
        runCatching {
            db.collection("auditLogs").document(logId).set(
                mapOf(
                    "actorId" to user.uid,
                    "actorEmail" to (user.email ?: "staff@iceesports.app"),
                    "actorRole" to if (actorRole == UserRole.ADMIN) "ADMIN" else "HOST",
                    "action" to action.take(60),
                    "targetId" to targetId.take(128),
                    "details" to details.take(300),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    private fun unwrapAndLog(error: Throwable, op: OperationType, path: String, uid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path, auth)
        }
    }

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "gavjbjbuu@gmail.com"
    }
}
