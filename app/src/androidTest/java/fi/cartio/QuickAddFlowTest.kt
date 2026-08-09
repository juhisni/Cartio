package fi.cartio

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuickAddFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun addMilkAndKeepSheetOpen() {
        rule.mainClock.advanceTimeBy(2_500)
        rule.waitForIdle()
        rule.onNodeWithTag("open_quick_add").performClick()
        rule.onNodeWithTag("quick_add_input").performTextInput("maito")
        rule.onNodeWithTag("quick_add_input").performImeAction()
        rule.waitUntil(5_000) { runCatching { rule.onNodeWithTag("product_maito").fetchSemanticsNode() }.isSuccess }
        rule.onNodeWithTag("category_DAIRY").fetchSemanticsNode()
        rule.onNodeWithTag("product_maito").fetchSemanticsNode()
        rule.onNodeWithTag("quick_add_input").fetchSemanticsNode()
    }
}
