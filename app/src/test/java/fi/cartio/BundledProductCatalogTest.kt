package fi.cartio

import fi.cartio.core.model.ProductCategory
import fi.cartio.domain.suggestion.BundledProductCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledProductCatalogTest {
    @Test fun catalogHasRecommendedConceptAndSearchTermScale() {
        assertTrue(BundledProductCatalog.products.size in 800..1_100)
        assertTrue(BundledProductCatalog.searchableTermCount in 3_000..4_000)
    }

    @Test fun bilingualSearchReturnsLocalizedProductNameAndCategory() {
        val english = BundledProductCatalog.suggestions("lactose-free milk").first()
        assertEquals("Lactose-free milk", english.name)
        assertEquals(ProductCategory.DAIRY, english.category)

        val finnish = BundledProductCatalog.suggestions("luomu banaani").first()
        assertEquals("Luomu banaani", finnish.name)
        assertEquals(ProductCategory.FRUITS_VEGETABLES, finnish.category)
    }
}
