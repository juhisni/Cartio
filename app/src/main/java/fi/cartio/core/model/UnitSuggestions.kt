package fi.cartio.core.model

fun suggestedUnits(category: ProductCategory, finnish: Boolean): List<String> = when (category) {
    ProductCategory.DRINKS, ProductCategory.DAIRY -> if (finnish) listOf("l", "ml", "kpl", "pkt") else listOf("l", "ml", "pcs", "pkg")
    ProductCategory.MEAT_FISH, ProductCategory.FRUITS_VEGETABLES, ProductCategory.FROZEN -> if (finnish) listOf("kg", "g", "kpl", "pkt") else listOf("kg", "g", "pcs", "pkg")
    ProductCategory.BREAD_GRAINS, ProductCategory.PANTRY -> if (finnish) listOf("g", "kg", "pkt", "kpl") else listOf("g", "kg", "pkg", "pcs")
    ProductCategory.HOUSEHOLD -> if (finnish) listOf("kpl", "pkt", "rll") else listOf("pcs", "pkg", "roll")
    ProductCategory.OTHER -> if (finnish) listOf("kpl", "pkt", "kg", "l") else listOf("pcs", "pkg", "kg", "l")
}
