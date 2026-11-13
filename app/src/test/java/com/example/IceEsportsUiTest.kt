package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.example.ui.screens.BlockedScreen
import com.example.ui.theme.IceEsportsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class IceEsportsUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun blockedScreen_displaysReasonAndSupportContact() {
        composeTestRule.setContent {
            IceEsportsTheme {
                BlockedScreen(
                    reason = "Suspicious Activity",
                    onSignOut = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("You are Blocked!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Suspicious Activity").assertIsDisplayed()
        composeTestRule.onNodeWithText("9525052870").assertIsDisplayed()
        composeTestRule.onNodeWithTag("blocked_sign_out_button")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
