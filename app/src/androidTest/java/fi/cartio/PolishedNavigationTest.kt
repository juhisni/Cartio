package fi.cartio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class PolishedNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun savedEmptyStateAndLocalizedSettingsAreReachable() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()

        rule.onNodeWithText("Saved").performClick()
        rule.onNodeWithText("No saved lists").assertIsDisplayed()
        rule.onNodeWithTag("saved_create_new_list").assertIsDisplayed()

        rule.onNodeWithText("Settings").performClick()
        rule.onNodeWithText("Privacy & data").assertIsDisplayed()
    }

    @Test fun mainNavigationReturnsFromSavedListsOpenedFromStartScreen() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()

        rule.onNodeWithText("Open saved lists").performClick()
        rule.onNodeWithText("No saved lists").assertIsDisplayed()
        rule.onNodeWithText("Main").performClick()

        rule.onNodeWithText("What would you like to do?").assertIsDisplayed()
        rule.onNodeWithTag("create_new_list").assertIsDisplayed()
    }

    @Test fun aNamedListCanBeCreatedFromTheStartScreen() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()

        rule.onNodeWithText("What would you like to do?").assertIsDisplayed()
        rule.onNodeWithTag("create_new_list").performClick()
        rule.onNodeWithTag("new_list_name").performTextInput("Weekly groceries")
        rule.onNodeWithTag("list_icon_selector").performClick()
        rule.onNodeWithTag("list_icon_party").performClick()
        rule.onNodeWithTag("confirm_create_list").performClick()

        rule.onNodeWithTag("active_list_card").assertIsDisplayed()
        rule.onNodeWithText("Weekly groceries").assertIsDisplayed()
        rule.onNodeWithText("🎉").assertIsDisplayed()
    }
}
