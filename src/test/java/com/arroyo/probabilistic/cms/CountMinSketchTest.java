package com.arroyo.probabilistic.cms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CountMinSketchTest {

    private CountMinSketch<String> cms;

    @BeforeEach
    void setUp() {
        cms = CountMinSketch.create(1000, 5);
    }

    @Test
    void insertedElementCountIsOne() {
        cms.add("Palindrome");
        assertEquals(1, cms.minimalFrequency("Palindrome"));
    }

    @Test
    void multipleInsertedElementsAreFound() {
        cms.add("Pneumatic");
        cms.add("Hippopotamus");
        cms.add("Flowery");

        assertEquals(1, cms.minimalFrequency("Pneumatic"));
        assertEquals(1, cms.minimalFrequency("Hippopotamus"));
        assertEquals(1, cms.minimalFrequency("Flowery"));
    }

    @Test
    void checkForNonexistentRecord() {
        assertEquals(0, cms.minimalFrequency("Flowery"));
    }

    @Test
    void insertedSingleLetterIsFound() {
        cms.add("X");
        assertEquals(1, cms.minimalFrequency("X"));
    }

    @Test
    void insertedEmptyString() {
        cms.add("");
        assertEquals(1, cms.minimalFrequency(""));
    }

    @Test
    void repeatedInsertionFrequencyCheck() {
        cms.add("Stooges");
        cms.add("Stooges");
        cms.add("Stooges");
        assertEquals(3, cms.minimalFrequency("Stooges"));
    }

    @Test
    void neverUnderestimatesTrueCount() {
        CountMinSketch<String> cms2 = CountMinSketch.create(3, 2); // tiny, forces collisions
        for (int i = 0; i < 100; i++) {
            cms2.add("target");
            cms2.add("collider" + i);
        }
        assertTrue(cms2.minimalFrequency("target") >= 100);
    }

    @Test
    void nonPositiveColumnsThrows() {
        assertThrows(IllegalArgumentException.class, () -> CountMinSketch.create(0, 5));
    }

    @Test
    void nonPositiveRowsThrows() {
        assertThrows(IllegalArgumentException.class, () -> CountMinSketch.create(1000, 0));
    }

    @Test
    void insertedNullHandled() {
        assertThrows(NullPointerException.class, () -> cms.add(null));
    }

    @Test
    void checkForNull() {
        assertThrows(NullPointerException.class, () -> cms.minimalFrequency(null));
    }

    @Test
    void createOverload() {
        CountMinSketch<String> cms3 = CountMinSketch.create(0.01, 0.01);
        cms3.add("Odyssey");
        assertEquals(1, cms3.minimalFrequency("Odyssey"));
    }

}
