package com.arroyo.probabilistic.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ElementConverterTest {

    @Test
    void standardConverterIntegerTest() {
        ElementConverter<Integer> eC = ElementConverters.standardConverter();
        assertEquals("42", eC.toHashable(42));
    }

    @Test
    void standardConverterCustomTest() {
        ElementConverter<StringBuilder> eC = ElementConverters.standardConverter();
        StringBuilder sb = new StringBuilder("test");
        assertEquals("test", eC.toHashable(sb));
    }
}
