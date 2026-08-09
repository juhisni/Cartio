package fi.cartio.domain.suggestion

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.AppLanguage
import fi.cartio.data.local.CartioDao
import java.text.Normalizer
import javax.inject.Inject

class OfflineCategorySuggestionEngine @Inject constructor(
    private val dao: CartioDao,
    private val catalog: BundledProductCatalog,
) : CategorySuggestionEngine {
    override fun normalize(input: String): String = normalizeProductInput(input)

    override suspend fun suggest(input: String): ProductCategory {
        val normalized = normalize(input)
        dao.learnedCategory(normalized)?.let { return it }
        return catalog.exactCategory(normalized)
            ?: catalog.keywordCategory(normalized)
            ?: ProductCategory.OTHER
    }

    override fun suggestions(query: String, language: AppLanguage): List<ProductSuggestion> =
        catalog.suggestions(normalize(query), language)
}

fun normalizeProductInput(input: String): String = Normalizer.normalize(input.trim().lowercase(), Normalizer.Form.NFC)
    .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ").replace(Regex("\\s+"), " ").trim()
