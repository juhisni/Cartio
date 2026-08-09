package fi.cartio.domain.suggestion

import fi.cartio.core.model.ProductCategory
import fi.cartio.data.local.CartioDao
import java.text.Normalizer
import javax.inject.Inject

class OfflineCategorySuggestionEngine @Inject constructor(private val dao: CartioDao) : CategorySuggestionEngine {
    private val dictionary = mapOf(
        ProductCategory.FRUITS_VEGETABLES to setOf("banaani", "banana", "bananas", "tomaatti", "tomato", "tomatoes", "avokado", "avocado", "omena", "apple", "apples", "kurkku", "cucumber", "peruna", "potato", "potatoes", "appelsiini", "orange", "sipuli", "onion", "porkkana", "carrot", "salaatti", "lettuce", "paprika", "pepper"),
        ProductCategory.DAIRY to setOf("maito", "milk", "kevytmaito", "täysmaito", "juusto", "cheese", "jogurtti", "yogurt", "voi", "butter", "kananmuna", "kananmunat", "egg", "eggs", "kerma", "cream", "rahka", "quark"),
        ProductCategory.BREAD_GRAINS to setOf("leipä", "bread", "riisi", "rice", "kaura", "oats", "murot", "cereal", "sämpylä", "roll", "tortilla", "näkkileipä", "crispbread"),
        ProductCategory.MEAT_FISH to setOf("lohi", "salmon", "kana", "chicken", "jauheliha", "minced meat", "kala", "fish", "naudanliha", "beef", "sianliha", "pork", "makkara", "sausage", "kinkku", "ham"),
        ProductCategory.FROZEN to setOf("pakaste", "frozen", "jäätelö", "ice cream", "pakastevihannekset", "frozen vegetables", "pakastepizza", "frozen pizza"),
        ProductCategory.PANTRY to setOf("pasta", "spaghetti", "makaroni", "macaroni", "jauho", "flour", "sokeri", "sugar", "kahvi", "coffee", "tee", "tea", "suola", "salt", "öljy", "oil", "säilyke", "canned food", "mauste", "spice"),
        ProductCategory.DRINKS to setOf("vesi", "water", "kivennäisvesi", "sparkling water", "mehu", "juice", "limonadi", "soda", "virvoitusjuoma", "soft drink"),
        ProductCategory.HOUSEHOLD to setOf("talouspaperi", "paper towel", "wc paperi", "toilet paper", "saippua", "soap", "pesuaine", "detergent", "astianpesuaine", "dish soap", "roskapussi", "trash bag", "siivoussieni", "sponge"),
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
