package fi.cartio

import fi.cartio.core.model.formatQuantity
import org.junit.Assert.assertEquals
import org.junit.Test

class QuantityFormatterTest {
    @Test fun wholeNumbersDoNotShowDecimalZero() {
        assertEquals("2 kpl", formatQuantity(2.0, "kpl"))
    }

    @Test fun meaningfulDecimalsAndMissingUnitsArePreserved() {
        assertEquals("2.5 kg", formatQuantity(2.5, "kg"))
        assertEquals("3", formatQuantity(3.0, null))
    }
}
