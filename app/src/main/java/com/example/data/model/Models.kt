package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TournamentMode(
    val code: String,
    val shortTitle: String,
    val subtitle: String,
    val fullLabel: String,
    @DrawableRes val defaultBannerRes: Int
) {
    CS("CS", "CS", "5v5", "CS • 5v5 Clash Squad", R.drawable.img_banner_cs),
    BR("BR", "BR", "Squad", "BR • Battle Royale", R.drawable.img_banner_br),
    NIGHT_MODE("NIGHT_MODE", "Night", "11PM-5AM", "Night Mode • 11PM-5AM", R.drawable.img_banner_night),
    SURVIVAL("SURVIVAL", "Survival", "Solo", "Survival • Solo", R.drawable.img_banner_br);

    companion object {
        fun fromCode(code: String?): TournamentMode {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: CS
        }
    }
}

enum class MatchStatus(val code: String, val label: String) {
    UPCOMING("UPCOMING", "Upcoming"),
    ONGOING("ONGOING", "Ongoing"),
    COMPLETED("COMPLETED", "Completed");

    companion object {
        fun fromCode(code: String?): MatchStatus {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: UPCOMING
        }
    }
}

enum class UserRole {
    PLAYER,
    HOST,
    ADMIN
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val iceId: String = "",
    val name: String = "",
    val email: String = "",
    val freeFireName: String = "",
    val freeFireUid: String = "",
    val avatarBase64: String? = null,
    val creditsBalance: Long = 0L,
    val isBlocked: Boolean = false,
    val blockReason: String? = null,
    val matchesPlayed: Long = 0L,
    val wins: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HostRecord(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val assignedMode: String = "ALL",
    val isBlocked: Boolean = false,
    val blockReason: String? = null,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Tournament(
    @DocumentId val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val mode: String = TournamentMode.CS.code,
    val status: String = MatchStatus.UPCOMING.code,
    val bannerKey: String = "banner_cs",
    val bannerBase64: String? = null,
    val entryLabel: String = "Free Entry • Non-Cash",
    val prizeLabel: String = "500 Tournament Credits",
    val totalSlots: Long = 100L,
    val joinedSlots: Long = 0L,
    val scheduleText: String = "",
    val rules: String = "",
    val createdBy: String = "",
    val createdByRole: String = "ADMIN",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val availableSlots: Long
        get() = (totalSlots - joinedSlots).coerceAtLeast(0L)

    val modeEnum: TournamentMode
        get() = TournamentMode.fromCode(mode)

    val statusEnum: MatchStatus
        get() = MatchStatus.fromCode(status)
}

@IgnoreExtraProperties
data class RoomDetails(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val roomId: String = "",
    val roomPassword: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Registration(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val tournamentTitle: String = "",
    val mode: String = TournamentMode.CS.code,
    val userId: String = "",
    val hostId: String = "",
    val playerName: String = "",
    val freeFireName: String = "",
    val freeFireUid: String = "",
    val status: String = "CONFIRMED",
    @ServerTimestamp val joinedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MatchResult(
    @DocumentId val id: String = "",
    val userId: String = "",
    val playerName: String = "",
    val freeFireName: String = "",
    val tournamentId: String = "",
    val tournamentTitle: String = "",
    val mode: String = "ALL",
    val wins: Long = 0L,
    val kills: Long = 0L,
    val points: Long = 0L,
    val rank: Long = 1L,
    val rewardCredits: Long = 0L,
    val publishedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "SYSTEM",
    val targetUserId: String = "ALL",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CreditTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val type: String = "CREDIT_AWARD",
    val amountCredits: Long = 0L,
    val note: String = "",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val actorId: String = "",
    val actorEmail: String = "",
    val actorRole: String = "ADMIN",
    val action: String = "",
    val targetId: String = "",
    val details: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
