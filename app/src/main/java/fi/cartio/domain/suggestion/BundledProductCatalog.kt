package fi.cartio.domain.suggestion

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fi.cartio.R
import fi.cartio.core.model.AppLanguage
import fi.cartio.core.model.ProductCategory
import fi.cartio.core.model.ProductSuggestion
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

internal data class CatalogProduct(
    val finnishName: String,
    val englishName: String,
    val category: ProductCategory,
) {
    val normalizedFinnish = normalizeProductInput(finnishName)
    val normalizedEnglish = normalizeProductInput(englishName)
}

private data class EverydayProduct(
    val finnishName: String,
    val englishName: String,
    val category: ProductCategory,
    val finnishAliases: Set<String> = setOf(finnishName),
    val englishAliases: Set<String> = setOf(englishName),
)

@Singleton
class BundledProductCatalog @Inject constructor(@ApplicationContext context: Context) {
internal val products: List<CatalogProduct> = context.resources.openRawResource(R.raw.fineli_catalog).bufferedReader(Charsets.UTF_8).useLines { lines ->
        lines.filter { it.isNotBlank() && !it.startsWith('#') }.mapNotNull(::parseProduct).distinctBy {
            "${it.normalizedFinnish}|${it.normalizedEnglish}"
        }.toList()
    }

    private val everydayProducts = listOf(
        EverydayProduct("Banaani", "Banana", ProductCategory.FRUITS_VEGETABLES),
        EverydayProduct("Maito", "Milk", ProductCategory.DAIRY, setOf("Maito", "Kevytmaito", "Täysmaito"), setOf("Milk")),
        EverydayProduct("Leipä", "Bread", ProductCategory.BREAD_GRAINS),
        EverydayProduct("Kananmuna", "Egg", ProductCategory.DAIRY, setOf("Kananmuna", "Kananmunat"), setOf("Egg", "Eggs")),
        EverydayProduct("Juusto", "Cheese", ProductCategory.DAIRY),
        EverydayProduct("Kurkku", "Cucumber", ProductCategory.FRUITS_VEGETABLES),
        EverydayProduct("Pasta", "Pasta", ProductCategory.PANTRY, setOf("Pasta", "Spagetti", "Makaroni"), setOf("Pasta", "Spaghetti", "Macaroni")),
        EverydayProduct("Lohi", "Salmon", ProductCategory.MEAT_FISH),
        EverydayProduct("Appelsiini", "Orange", ProductCategory.FRUITS_VEGETABLES),
        EverydayProduct("Omena", "Apple", ProductCategory.FRUITS_VEGETABLES),
        EverydayProduct("Peruna", "Potato", ProductCategory.FRUITS_VEGETABLES),
        EverydayProduct("Kana", "Chicken", ProductCategory.MEAT_FISH),
        EverydayProduct("Riisi", "Rice", ProductCategory.BREAD_GRAINS),
        EverydayProduct("Kahvi", "Coffee", ProductCategory.PANTRY),
        EverydayProduct("Vesi", "Water", ProductCategory.DRINKS),
    )

    private val exactCategories = buildMap {
        products.forEach { product ->
            put(product.normalizedFinnish, product.category)
            put(product.normalizedEnglish, product.category)
        }
        everydayProducts.forEach { product ->
            (product.finnishAliases + product.englishAliases).forEach { alias -> put(normalizeProductInput(alias), product.category) }
        }
    }
    private val searchableNames = products.flatMap { product ->
        listOf(product.normalizedFinnish to product.category, product.normalizedEnglish to product.category)
    }.plus(everydayProducts.flatMap { product ->
        (product.finnishAliases + product.englishAliases).map { normalizeProductInput(it) to product.category }
    }).distinctBy { it.first }.sortedByDescending { it.first.length }

    private val finnishNames = (products.map { it.normalizedFinnish } + everydayProducts.flatMap { product ->
        product.finnishAliases.map(::normalizeProductInput)
    }).toSet()
    private val englishNames = (products.map { it.normalizedEnglish } + everydayProducts.flatMap { product ->
        product.englishAliases.map(::normalizeProductInput)
    }).toSet()

    fun exactCategory(normalized: String): ProductCategory? = exactCategories[normalized]

    fun keywordCategory(normalized: String): ProductCategory? = searchableNames.firstOrNull { (name, _) ->
        Regex("(^|\\s)${Regex.escape(name)}($|\\s)").containsMatchIn(normalized)
    }?.second

    fun suggestions(query: String, language: AppLanguage): List<ProductSuggestion> {
        if (query.isBlank()) return everydayProducts.take(8).map {
            ProductSuggestion(if (language == AppLanguage.FINNISH) it.finnishName else it.englishName, it.category)
        }

        val everydayMatches = everydayProducts.asSequence().mapNotNull { product ->
            val aliases = if (language == AppLanguage.FINNISH) product.finnishAliases else product.englishAliases
            val score = aliases.minOf { matchScore(normalizeProductInput(it), query) }
            if (score == Int.MAX_VALUE) null else RankedSuggestion(
                ProductSuggestion(if (language == AppLanguage.FINNISH) product.finnishName else product.englishName, product.category),
                score,
            )
        }
        val fineliMatches = products.asSequence().mapNotNull { product ->
            val name = if (language == AppLanguage.FINNISH) product.finnishName else product.englishName
            val normalizedName = if (language == AppLanguage.FINNISH) product.normalizedFinnish else product.normalizedEnglish
            if (isRedundantPlural(normalizedName, language)) return@mapNotNull null
            val score = matchScore(normalizedName, query)
            if (score == Int.MAX_VALUE) null else RankedSuggestion(
                suggestion = ProductSuggestion(
                    name = name.displayCase(),
                    category = product.category,
                ),
                score = score,
            )
        }
        return (everydayMatches + fineliMatches).sortedWith(compareBy<RankedSuggestion> { it.score }.thenBy { it.suggestion.name.length })
            .distinctBy { it.suggestion.name.lowercase(Locale.ROOT) }
            .take(8)
            .map { it.suggestion }
            .toList()
    }

    private fun parseProduct(line: String): CatalogProduct? {
        val columns = line.split('\t')
        if (columns.size != 3) return null
        val category = runCatching { ProductCategory.valueOf(columns[0]) }.getOrNull() ?: return null
        return CatalogProduct(columns[1].trim(), columns[2].trim(), category)
    }

    private fun matchScore(value: String, query: String): Int = when {
        value == query -> 0
        value.startsWith(query) -> 1
        value.split(' ').any { it.startsWith(query) } -> 2
        query in value -> 3
        else -> Int.MAX_VALUE
    }

    private fun isRedundantPlural(name: String, language: AppLanguage): Boolean {
        val words = name.split(' ')
        val last = words.lastOrNull() ?: return false
        val prefix = words.dropLast(1).joinToString(" ").let { if (it.isBlank()) "" else "$it " }
        val singularCandidates = if (language == AppLanguage.FINNISH) {
            if (last.length > 2 && last.endsWith('t')) listOf(last.dropLast(1)) else emptyList()
        } else buildList {
            if (last.length > 3 && last.endsWith("ies")) add(last.dropLast(3) + "y")
            if (last.length > 3 && last.endsWith("es")) add(last.dropLast(2))
            if (last.length > 2 && last.endsWith('s') && !last.endsWith("ss")) add(last.dropLast(1))
        }
        val available = if (language == AppLanguage.FINNISH) finnishNames else englishNames
        return singularCandidates.any { "$prefix$it" in available }
    }

    private fun String.displayCase() = lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.getDefault()) }
    private data class RankedSuggestion(val suggestion: ProductSuggestion, val score: Int)
}
