package com.arroyo.probabilistic.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SizingCalculatorTest {

    @Test
    void estimatedFalsePositiveRateMatchesKnownValues() {
        double result = SizingCalculator.estimatedFalsePositiveRate(28756, 7, 3000);
        assertEquals(0.0096, result, 0.001);
    }

    @Test
    void nextPrimeReturnsTheSame() {
        assertEquals(7, SizingCalculator.nextPrimeNum(7));
    }

    @Test
    void nextPrimeSkipsTheEvenNumber() {
        assertEquals(11, SizingCalculator.nextPrimeNum(10));
    }

    @Test
    void nextPrimeEdgeCases() {
        assertEquals(2, SizingCalculator.nextPrimeNum(0));
        assertEquals(2, SizingCalculator.nextPrimeNum(1));
        assertEquals(2, SizingCalculator.nextPrimeNum(2));
        assertEquals(3, SizingCalculator.nextPrimeNum(3));
    }

    @Test
    void optimalMThrowsOnValidation() {
        assertThrows(IllegalArgumentException.class, () -> SizingCalculator.optimalM(0, 0.01));
        assertThrows(IllegalArgumentException.class, () -> SizingCalculator.optimalM(1000, 1.0));
        assertThrows(IllegalArgumentException.class, () -> SizingCalculator.optimalM(1000, 0.0));
    }

    @Test
    void optimalKThrowsOnValidation() {
        assertThrows(IllegalArgumentException.class, () -> SizingCalculator.optimalK(0, 1000));
        assertThrows(IllegalArgumentException.class, () -> SizingCalculator.optimalK(1000, 0));
    }

    @Test
    void estimatedFPRThrowsOnValidation() {
        assertThrows(IllegalArgumentException.class, () -> SizingCalculator.estimatedFalsePositiveRate(0, 7, 1000));
        assertThrows(IllegalArgumentException.class, () -> SizingCalculator.estimatedFalsePositiveRate(1000, 0, 1000));
        assertThrows(IllegalArgumentException.class, () -> SizingCalculator.estimatedFalsePositiveRate(1000, 7, 0));
    }

    @Test
    void optimalMReturnsPrime() {
        int m = SizingCalculator.optimalM(3000, 0.01);
        assertTrue(SizingCalculator.isPrimeNum(m));
    }
}
