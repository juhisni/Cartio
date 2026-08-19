package fi.cartio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertTextContains
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

class PolishedNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun aboutDetailsAreReachableAndBackReturnsToSettings() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()

        rule.onNodeWithText("Settings").performClick()
        rule.onNodeWithText("About Cartio").performClick()
        rule.onNodeWithText("Privacy & data").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithText("Language").assertIsDisplayed()
    }

    @Test fun mainNavigationReturnsFromSavedLists() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()

        if (runCatching { rule.onNodeWithText("Open saved lists").fetchSemanticsNode() }.isSuccess) {
            rule.onNodeWithText("Open saved lists").performClick()
        } else {
            rule.onNodeWithText("Saved").performClick()
        }
        rule.onNodeWithText("Main").performClick()

        val hasStartState = runCatching { rule.onNodeWithTag("create_new_list").fetchSemanticsNode() }.isSuccess
        val hasActiveList = runCatching { rule.onNodeWithTag("active_list_card").fetchSemanticsNode() }.isSuccess
        assertTrue(hasStartState || hasActiveList)
    }

    @Test fun aNamedListCanBeCreatedFromTheStartScreen() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()

        if (runCatching { rule.onNodeWithTag("create_new_list").fetchSemanticsNode() }.isSuccess) {
            rule.onNodeWithTag("create_new_list").performClick()
        } else {
            rule.onNodeWithTag("active_list_card").performClick()
            rule.onNodeWithText("Create new list").performClick()
        }
        val listName = "UI test ${System.nanoTime()}"
        rule.onNodeWithTag("new_list_name").performTextInput(listName)
        rule.onNodeWithTag("list_icon_selector").performClick()
        rule.onNodeWithTag("list_icon_party").performClick()
        rule.onNodeWithTag("confirm_create_list").performClick()

        rule.waitUntil(timeoutMillis = 5_000) { rule.onAllNodesWithTag("active_list_card").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("active_list_card").assertIsDisplayed()
        rule.onNodeWithText(listName).assertIsDisplayed()
        rule.onNodeWithText("🎉").assertIsDisplayed()
    }

    @Test fun partiallyEnteredListNameSurvivesActivityRecreation() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()
        if (runCatching { rule.onNodeWithTag("create_new_list").fetchSemanticsNode() }.isSuccess) {
            rule.onNodeWithTag("create_new_list").performClick()
        } else {
            rule.onNodeWithTag("active_list_card").performClick()
            rule.onNodeWithText("Create new list").performClick()
        }
        rule.onNodeWithTag("new_list_name").performTextInput("Rotation draft")

        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        rule.onNodeWithTag("new_list_name").assertTextContains("Rotation draft")
    }
}
