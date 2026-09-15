package com.arroyo.probabilistic.bloom.variants;

import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CountingBloomFilterTest {

    private CountingBloomFilter<String> cbf;

    @BeforeEach
    void setUp() {
        cbf = CountingBloomFilter.create(1000, 5);
    }

    @Test
    void insertedElementIsFound() {
        cbf.add("Palindrome");
        assertTrue(cbf.mightContain("Palindrome"));
    }

    @Test
    void multipleInsertedElementsAreFound() {
        cbf.add("Pneumatic");
        cbf.add("Hippopotamus");
        cbf.add("Flowery");

        assertTrue(cbf.mightContain("Pneumatic"));
        assertTrue(cbf.mightContain("Hippopotamus"));
        assertTrue(cbf.mightContain("Flowery"));
    }

    @Test
    void checkForNonexistentRecord() {
        assertFalse(cbf.mightContain("okay"));
    }

    @Test
    void insertedSingleLetterIsFound() {
        cbf.add("X");
        assertTrue(cbf.mightContain("X"));
    }

    @Test
    void insertedEmptyString() {
        cbf.add("");
        assertTrue(cbf.mightContain(""));
    }

    @Test
    void insertedVeryLongStringIsFound() {
        String x = "asdasfasdasdasfasdasfasdddddfdsgfdsgadfdsgnsifansignadifSNIGBNASIDASFaafasfasnigiSNFIASNGIASNFIASGNaodasfhefiaN";
        cbf.add(x);
        assertTrue(cbf.mightContain(x));
    }

    @Test
    void insertedNullHandled() {
        assertThrows(NullPointerException.class, () -> cbf.add(null));
    }

    @Test
    void checkForNull() {
        assertThrows(NullPointerException.class, () -> cbf.mightContain(null));
    }

    @Test
    void deleteNullThrows() {
        assertThrows(NullPointerException.class, () -> cbf.delete(null));
    }

    @Test
    void nonPositiveArraySizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> CountingBloomFilter.create(0, 5));
    }

    @Test
    void nonPositiveHashCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> CountingBloomFilter.create(1000, 0));
    }

    @Test
    void createOverloadTest() {
        ElementConverter<String> eC = ElementConverters.stringConverter();
        HashFunction h1 = HashFunctions.seeded(23);
        HashFunction h2 = HashFunctions.seeded(31);

        CountingBloomFilter<String> cbf2 = CountingBloomFilter.create(1000, 5, eC, h1, h2);
        cbf2.add("Odyssey");
        assertTrue(cbf2.mightContain("Odyssey"));
    }

    @Test
    void deletedElementIsNoLongerFound() {
        cbf.add("Palindrome");
        cbf.delete("Palindrome");
        assertFalse(cbf.mightContain("Palindrome"));
    }

    @Test
    void deletingOneElementDoesNotBreakAnotherStillPresent() {
        CountingBloomFilter<String> smallCbf = CountingBloomFilter.create(20, 3);

        List<String> elements = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            String x = UUID.randomUUID().toString();
            smallCbf.add(x);
            elements.add(x);
        }

        for (int i = 0; i < elements.size() - 1; i++) {
            smallCbf.delete(elements.get(i));
        }

    }
}