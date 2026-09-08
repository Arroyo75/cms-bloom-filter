package com.arroyo.probabilistic.cms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountMinSketchTest {

    private CountMinSketch<String> cms;

    @BeforeEach
    void setUp() {
        cms = CountMinSketch.create(1000, 5);
    }

    @Test
    void insertedElementIsFound() {
        cms.add("Palindrome");
        assertEquals(1, cms.minimalFrequency("Palindrome"));
    }


}
