package fi.cartio.core.designsystem

import fi.cartio.core.model.ProductCategory

fun productIcon(name: String, category: ProductCategory): String {
    val value = name.lowercase()
    return when {
        value.has("banaani", "banana") -> "🍌"
        value.has("omena", "apple") -> "🍎"
        value.has("appelsiini", "orange", "mandariini", "mandarin") -> "🍊"
        value.has("sitruuna", "lemon") -> "🍋"
        value.has("rypäle", "grape") -> "🍇"
        value.has("mansikka", "strawberry") -> "🍓"
        value.has("mustikka", "blueberry", "marja", "berry") -> "🫐"
        value.has("vesimeloni", "watermelon") -> "🍉"
        value.has("ananas", "pineapple") -> "🍍"
        value.has("avokado", "avocado") -> "🥑"
        value.has("tomaatti", "tomato") -> "🍅"
        value.has("kurkku", "cucumber") -> "🥒"
        value.has("porkkana", "carrot") -> "🥕"
        value.has("peruna", "potato") -> "🥔"
        value.has("valkosipuli", "garlic") -> "🧄"
        value.has("sipuli", "onion") -> "🧅"
        value.has("paprika", "pepper") -> "🫑"
        value.has("maissi", "corn") -> "🌽"
        value.has("sieni", "mushroom") -> "🍄"
        value.has("parsakaali", "broccoli") -> "🥦"
        value.has("salaatti", "lettuce", "kaali", "cabbage") -> "🥬"
        value.has("maito", "milk") -> "🥛"
        value.has("juusto", "cheese") -> "🧀"
        value.has("kananmuna", "egg") -> "🥚"
        value == "voi" || value.has("butter") -> "🧈"
        value.has("jogurtti", "yogurt", "rahka", "quark") -> "🥣"
        value.has("leipä", "bread", "sämpylä", "roll", "patonki", "baguette") -> "🍞"
        value.has("croissant", "voisarvi") -> "🥐"
        value.has("riisi", "rice") -> "🍚"
        value.has("pasta", "spaghetti", "makaroni", "macaroni", "nuudeli", "noodle") -> "🍝"
        value.has("kana", "chicken", "broileri") -> "🍗"
        value.has("naudan", "beef", "pihvi", "steak") -> "🥩"
        value.has("pekoni", "bacon") -> "🥓"
        value.has("makkara", "sausage") -> "🌭"
        value.has("kala", "fish", "lohi", "salmon", "tonnikala", "tuna") -> "🐟"
        value.has("katkarapu", "shrimp") -> "🍤"
        value.has("jäätelö", "ice cream") -> "🍨"
        value.has("pizza") -> "🍕"
        value.has("kahvi", "coffee") -> "☕"
        value.has("tee", "tea") -> "🍵"
        value.has("vesi", "water") -> "💧"
        value.has("mehu", "juice") -> "🧃"
        value.has("limonadi", "soda", "cola", "kolajuoma") -> "🥤"
        value.has("suklaa", "chocolate") -> "🍫"
        value.has("keksi", "cookie", "biscuit") -> "🍪"
        value.has("hunaja", "honey") -> "🍯"
        value.has("suola", "salt", "mauste", "spice") -> "🧂"
        value.has("öljy", "oil") -> "🫗"
        value.has("talouspaperi", "paper towel", "wc-paperi", "toilet paper") -> "🧻"
        value.has("saippua", "soap", "pesuaine", "detergent") -> "🧴"
        value.has("roskapussi", "trash bag") -> "🗑️"
        value.has("patteri", "battery") -> "🔋"
        else -> categoryIcon(category)
    }
}

fun categoryIcon(category: ProductCategory): String = when (category) {
    ProductCategory.FRUITS_VEGETABLES -> "🥬"
    ProductCategory.DAIRY -> "🥛"
    ProductCategory.BREAD_GRAINS -> "🌾"
    ProductCategory.MEAT_FISH -> "🐟"
    ProductCategory.FROZEN -> "❄️"
    ProductCategory.PANTRY -> "🥫"
    ProductCategory.DRINKS -> "🧃"
    ProductCategory.HOUSEHOLD -> "🧻"
    ProductCategory.OTHER -> "🛒"
}

private fun String.has(vararg terms: String) = terms.any { it in this }
