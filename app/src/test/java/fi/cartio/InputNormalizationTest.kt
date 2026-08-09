package fi.cartio

import fi.cartio.domain.suggestion.normalizeProductInput
import org.junit.Assert.assertEquals
import org.junit.Test

class InputNormalizationTest {
    @Test fun trimsLowercasesAndRemovesPunctuation() { assertEquals("kevytmaito 1l", normalizeProductInput("  Kevytmaito, 1L! ")) }
    @Test fun preservesFinnishCharacters() { assertEquals("täysmaito", normalizeProductInput("TÄYSMAITO")) }
}
