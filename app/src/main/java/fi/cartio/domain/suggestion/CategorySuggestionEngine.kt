package fi.cartio.domain.suggestion

import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import fi.cartio.core.model.AppLanguage

interface CategorySuggestionEngine {
    fun normalize(input: String): String
    suspend fun suggest(input: String): ProductCategory
    fun suggestions(query: String, language: AppLanguage): List<ProductSuggestion> = emptyList()
}
