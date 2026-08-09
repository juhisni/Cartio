package fi.cartio.domain.suggestion

import fi.cartio.core.model.ProductCategory

interface CategorySuggestionEngine {
    fun normalize(input: String): String
    suspend fun suggest(input: String): ProductCategory
}
