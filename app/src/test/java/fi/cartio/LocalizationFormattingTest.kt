package fi.cartio

import fi.cartio.core.localization.strings
import fi.cartio.core.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizationFormattingTest {
    @Test fun englishCountsUseCorrectSingularAndPluralForms() {
        val text = strings(AppLanguage.ENGLISH)
        assertEquals("1 item", text.itemCountText(1))
        assertEquals("2 items · 1 completed", text.listProgressText(2, 1))
        assertEquals("1 item remaining out of 2", text.categoryProgressDescription(1, 2))
    }

    @Test fun finnishCountsUseCorrectSingularAndPluralForms() {
        val text = strings(AppLanguage.FINNISH)
        assertEquals("1 tuote", text.itemCountText(1))
        assertEquals("2 tuotetta · 1 valmiina", text.listProgressText(2, 1))
        assertEquals("1 tuote jäljellä 2 tuotteesta", text.categoryProgressDescription(1, 2))
    }
}
