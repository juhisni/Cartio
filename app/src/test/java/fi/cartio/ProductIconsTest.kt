package fi.cartio

import fi.cartio.core.designsystem.productIcon
import fi.cartio.core.model.ProductCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductIconsTest {
    @Test fun ingredientDoesNotOverrideProductForm() {
        assertEquals("🍿", productIcon("Banaanilastu", ProductCategory.PANTRY))
        assertEquals("🍶", productIcon("Viinietikka", ProductCategory.PANTRY))
        assertEquals("🫙", productIcon("Kalaliemikuutio", ProductCategory.PANTRY))
        assertEquals("🫙", productIcon("Mansikkahillo", ProductCategory.PANTRY))
        assertEquals("🥛", productIcon("Mantelijuoma", ProductCategory.FRUITS_VEGETABLES))
    }

    @Test fun misleadingFineliCategoryDoesNotControlKnownProductIcon() {
        assertEquals("🥩", productIcon("Jauheliha", ProductCategory.PANTRY))
        assertEquals("🐖", productIcon("Sianliha", ProductCategory.PANTRY))
        assertEquals("🌭", productIcon("Makkara", ProductCategory.PANTRY))
        assertEquals("🌿", productIcon("Maitohorsma", ProductCategory.FRUITS_VEGETABLES))
        assertEquals("🌼", productIcon("Voikukka", ProductCategory.FRUITS_VEGETABLES))
    }

    @Test fun accentsAndEnglishNamesResolveConsistently() {
        assertEquals("🍞", productIcon("Leipä", ProductCategory.BREAD_GRAINS))
        assertEquals("🍞", productIcon("Bread", ProductCategory.BREAD_GRAINS))
        assertEquals("🍇", productIcon("Viinirypäle", ProductCategory.FRUITS_VEGETABLES))
        assertEquals("🍇", productIcon("Grape", ProductCategory.FRUITS_VEGETABLES))
    }
}
