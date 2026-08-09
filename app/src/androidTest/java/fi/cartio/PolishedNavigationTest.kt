package fi.cartio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class PolishedNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun savedEmptyStateAndLocalizedSettingsAreReachable() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()

        rule.onNodeWithText("Saved").performClick()
        rule.onNodeWithText("No saved lists").assertIsDisplayed()

        rule.onNodeWithText("Settings").performClick()
        rule.onNodeWithText("Fast, calm, and completely offline.").assertIsDisplayed()
    }
}
