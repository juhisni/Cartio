package fi.cartio.domain.suggestion

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion

interface CategorySuggestionEngine {
    fun normalize(input: String): String
    suspend fun suggest(input: String): ProductCategory
    fun suggestions(query: String): List<ProductSuggestion> = emptyList()
}
