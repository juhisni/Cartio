package fi.cartio

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertTextContains
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuickAddFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun ensureActiveList(name: String) {
        if (runCatching { rule.onNodeWithTag("open_quick_add").fetchSemanticsNode() }.isSuccess) {
            rule.onNodeWithTag("active_list_card").performClick()
            rule.onNodeWithText("Create new list").performClick()
        } else {
            rule.onNodeWithTag("create_new_list").performClick()
        }
        rule.onNodeWithTag("new_list_name").performTextInput("$name ${System.nanoTime()}")
        rule.onNodeWithTag("confirm_create_list").performClick()
        rule.waitUntil(5_000) {
            runCatching { rule.onNodeWithTag("open_quick_add").fetchSemanticsNode() }.isSuccess
        }
    }

    @Test fun addMilkAndKeepSheetOpen() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()
        ensureActiveList("Milk test")
        rule.onNodeWithTag("open_quick_add").performClick()
        rule.onNodeWithTag("quick_add_input").performTextInput("milk")
        rule.onNodeWithTag("quick_add_input").performImeAction()
        rule.waitUntil(5_000) { runCatching { rule.onNodeWithTag("product_milk").fetchSemanticsNode() }.isSuccess }
        rule.onNodeWithTag("category_DAIRY").fetchSemanticsNode()
        rule.onNodeWithTag("product_milk").fetchSemanticsNode()
        rule.onNodeWithTag("quick_add_input").fetchSemanticsNode()
        rule.onNodeWithTag("quick_add_input").performTextInput("milk")
        rule.waitForIdle()
        assertFalse(runCatching { rule.onNodeWithTag("suggestion_milk").fetchSemanticsNode() }.isSuccess)
        assertFalse(runCatching { rule.onNodeWithTag("add_typed_product").fetchSemanticsNode() }.isSuccess)
    }

    @Test fun typedUnknownProductCanBeAddedWithExplicitButton() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()
        ensureActiveList("Custom product test")
        rule.onNodeWithTag("open_quick_add").performClick()
        rule.onNodeWithTag("quick_add_input").performTextInput("testituote")
        rule.onNodeWithTag("quick_add_input").performImeAction()
        rule.waitForIdle()
        assertFalse(runCatching { rule.onNodeWithTag("product_testituote").fetchSemanticsNode() }.isSuccess)
        rule.onNodeWithTag("quick_add_input").fetchSemanticsNode()
        rule.onNodeWithTag("add_typed_product").performClick()
        rule.waitUntil(5_000) {
            runCatching { rule.onNodeWithTag("product_testituote").fetchSemanticsNode() }.isSuccess
        }
        rule.onNodeWithTag("category_OTHER").fetchSemanticsNode()
        rule.onNodeWithTag("quick_add_input").fetchSemanticsNode()
    }

    @Test fun quickAddQueryAndSheetSurviveActivityRecreation() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()
        ensureActiveList("Recreation test")
        rule.onNodeWithTag("open_quick_add").performClick()
        rule.onNodeWithTag("quick_add_input").performTextInput("ban")

        rule.activityRule.scenario.recreate()
        rule.waitForIdle()

        rule.onNodeWithTag("quick_add_input").assertTextContains("ban")
    }
}
