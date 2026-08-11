package fi.cartio

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.suggestedUnits
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitSuggestionsTest {
    @Test fun suggestsVolumeUnitsForDrinks() {
        assertEquals(listOf("l", "ml", "pcs", "pkg"), suggestedUnits(ProductCategory.DRINKS, finnish = false))
    }

    @Test fun localizesCountAndPackageUnits() {
        assertEquals(listOf("pcs", "pkg", "roll"), suggestedUnits(ProductCategory.HOUSEHOLD, finnish = false))
        assertEquals(listOf("kpl", "pkt", "rll"), suggestedUnits(ProductCategory.HOUSEHOLD, finnish = true))
    }
}
