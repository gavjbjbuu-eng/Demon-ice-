package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.MatchStatus
import com.example.data.model.TournamentMode
import com.example.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class IceEsportsRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserProfile_authenticatedPlayer_createsAndObservesProfile() = runBlocking {
        val uid = signInTestUser("player_${UUID.randomUUID()}@test.com")
        val repository = IceEsportsRepository(firestore, auth)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserProfile(checkNotNull(auth.currentUser))
        }
        assertTrue("Profile creation should succeed: ${result.exceptionOrNull()}", result.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(uid).first { it != null }
        }
        assertNotNull(observed)
        assertEquals(uid, observed?.userId)
        assertEquals(0L, observed?.creditsBalance)
        assertEquals(false, observed?.isBlocked)
    }

    @Test
    fun observeTournaments_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = IceEsportsRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTournaments().first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated caller")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            if (firestoreException != null) {
                assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
            } else {
                assertTrue(e is IllegalStateException)
            }
        }
    }

    @Test
    fun crossUserProfileRead_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_${UUID.randomUUID()}@test.com")
        val aliceRepo = IceEsportsRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.ensureUserProfile(checkNotNull(auth.currentUser)).getOrThrow()
        }

        signInTestUser("bob_${UUID.randomUUID()}@test.com")
        val exception = try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).get().await()
            }
            null
        } catch (e: FirebaseFirestoreException) {
            e
        }
        assertNotNull("Cross-user read should throw FirebaseFirestoreException", exception)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun playerCannotCreateTournamentOrPublishResults() = runBlocking {
        signInTestUser("regular_${UUID.randomUUID()}@test.com")
        val repo = IceEsportsRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile(checkNotNull(auth.currentUser)).getOrThrow()
        }

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createOrUpdateTournament(
                existingTournament = null,
                title = "Unauthorized Match",
                subtitle = "5v5",
                mode = TournamentMode.CS,
                status = MatchStatus.UPCOMING,
                bannerKey = "banner_cs",
                bannerBase64 = null,
                entryLabel = "Free Entry",
                prizeLabel = "100 Credits",
                totalSlots = 50L,
                scheduleText = "Today 11 AM",
                rules = "Standard",
                roomId = "",
                roomPassword = "",
                actorRole = UserRole.PLAYER
            )
        }
        assertTrue(createResult.isFailure)
        val firestoreErr = createResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreErr)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreErr?.code)

        val publishResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.publishResult(
                userId = checkNotNull(auth.currentUser?.uid),
                playerName = "Hacker",
                freeFireName = "HackFF",
                tournamentId = "t_none",
                tournamentTitle = "Fake",
                mode = TournamentMode.CS,
                wins = 99,
                kills = 99,
                points = 9999,
                rank = 1,
                rewardCredits = 9999
            )
        }
        assertTrue(publishResult.isFailure)
        val publishErr = publishResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(publishErr)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, publishErr?.code)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
