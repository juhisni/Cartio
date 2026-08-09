package fi.cartio.domain.suggestion

import fi.cartio.core.model.ProductCategory
import fi.cartio.data.local.CartioDao
import java.text.Normalizer
import javax.inject.Inject

class OfflineCategorySuggestionEngine @Inject constructor(private val dao: CartioDao) : CategorySuggestionEngine {
    private val dictionary = mapOf(
        ProductCategory.FRUITS_VEGETABLES to setOf("banaani", "banana", "bananas", "tomaatti", "tomato", "avokado", "avocado", "omena", "apple", "kurkku", "cucumber", "peruna", "potato"),
        ProductCategory.DAIRY to setOf("maito", "milk", "kevytmaito", "täysmaito", "juusto", "cheese", "jogurtti", "yogurt", "voi", "butter"),
        ProductCategory.BREAD_GRAINS to setOf("leipä", "bread", "riisi", "rice", "kaura", "oats", "murot", "cereal"),
        ProductCategory.MEAT_FISH to setOf("lohi", "salmon", "kana", "chicken", "jauheliha", "minced meat", "kala", "fish"),
        ProductCategory.FROZEN to setOf("pakaste", "frozen", "jäätelö", "ice cream"),
        ProductCategory.PANTRY to setOf("pasta", "spaghetti", "makaroni", "macaroni", "jauho", "flour", "sokeri", "sugar", "kahvi", "coffee"),
        ProductCategory.DRINKS to setOf("vesi", "water", "mehu", "juice", "limonadi", "soda"),
        ProductCategory.HOUSEHOLD to setOf("talouspaperi", "paper towel", "saippua", "soap", "pesuaine", "detergent"),
    )

    override fun normalize(input: String): String = normalizeProductInput(input)

    override suspend fun suggest(input: String): ProductCategory {
        val normalized = normalize(input)
        dao.learnedCategory(normalized)?.let { return it }
        dictionary.entries.firstOrNull { (_, aliases) -> normalized in aliases }?.let { return it.key }
        val words = normalized.split(' ').toSet()
        return dictionary.entries.firstOrNull { (_, aliases) -> aliases.any { it in words } }?.key ?: ProductCategory.OTHER
    }
}

fun normalizeProductInput(input: String): String = Normalizer.normalize(input.trim().lowercase(), Normalizer.Form.NFC)
    .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ").replace(Regex("\\s+"), " ").trim()
