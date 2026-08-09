package fi.cartio.core.model

import java.math.BigDecimal

fun formatQuantity(quantity: Double, unit: String?): String {
    val number = BigDecimal.valueOf(quantity).stripTrailingZeros().toPlainString()
    return listOfNotNull(number, unit?.trim()?.takeIf { it.isNotEmpty() }).joinToString(" ")
}
