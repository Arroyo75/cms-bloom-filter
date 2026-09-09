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
    void insertedNullHandled() {
        assertThrows(NullPointerException.class, () -> cms.add(null));
    }

    @Test
    void checkForNull() {
        assertThrows(NullPointerException.class, () -> cms.minimalFrequency(null));
    }



}
