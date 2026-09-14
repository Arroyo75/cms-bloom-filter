package com.arroyo.probabilistic.bloom.variants;

import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StableBloomFilterTest {

    private StableBloomFilter<String> sbf;

    @BeforeEach
    void setUp() {
        sbf = StableBloomFilter.create(1000, 5, 10, (byte) 3);
    }

    @Test
    void insertedElementIsFound() {
        sbf.add("Palindrome");
        assertTrue(sbf.mightContain("Palindrome"));
    }

    @Test
    void multipleInsertedElementsAreFound() {
        sbf.add("Pneumatic");
        sbf.add("Hippopotamus");
        sbf.add("Flowery");

        assertTrue(sbf.mightContain("Pneumatic"));
        assertTrue(sbf.mightContain("Hippopotamus"));
        assertTrue(sbf.mightContain("Flowery"));
    }

    @Test
    void checkForNonexistentRecord() {
        assertFalse(sbf.mightContain("okay"));
    }

    @Test
    void insertedSingleLetterIsFound() {
        sbf.add("X");
        assertTrue(sbf.mightContain("X"));
    }

    @Test
    void insertedEmptyString() {
        sbf.add("");
        assertTrue(sbf.mightContain(""));
    }

    @Test
    void insertedVeryLongStringIsFound() {
        String x = "asdasfasdasdasfasdasfasdddddfdsgfdsgadfdsgnsifansignadifSNIGBNASIDASFaafasfasnigiSNFIASNGIASNFIASGNaodasfhefiaN";
        sbf.add(x);
        assertTrue(sbf.mightContain(x));
    }

    @Test
    void insertedNullHandled() {
        assertThrows(NullPointerException.class, () -> sbf.add(null));
    }

    @Test
    void checkForNull() {
        assertThrows(NullPointerException.class, () -> sbf.mightContain(null));
    }

    @Test
    void nonPositiveArraySizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> StableBloomFilter.create(0, 5, 10, (byte) 3));
    }

    @Test
    void nonPositiveHashCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> StableBloomFilter.create(1000, 0, 10, (byte) 3));
    }

    @Test
    void nonPositiveDecrementsThrows() {
        assertThrows(IllegalArgumentException.class, () -> StableBloomFilter.create(1000, 5, 0, (byte) 3));
    }

    @Test
    void nonPositiveMaxThrows() {
        assertThrows(IllegalArgumentException.class, () -> StableBloomFilter.create(1000, 5, 10, (byte) 0));
    }

    @Test
    void createOverloadTest() {
        ElementConverter<String> eC = ElementConverters.stringConverter();
        HashFunction h1 = HashFunctions.seeded(23);
        HashFunction h2 = HashFunctions.seeded(31);

        StableBloomFilter<String> sbf2 = StableBloomFilter.create(1000, 5, 10, (byte) 3, eC, h1, h2);
        sbf2.add("Odyssey");
        assertTrue(sbf2.mightContain("Odyssey"));
    }

    @Test
    void decayRefreshmentTest() {
        //small array, aggressive decrements, to force fast decay.
        StableBloomFilter<String> smallSbf = StableBloomFilter.create(50, 3, 20, (byte) 3);

        smallSbf.add("persistent");

        for (int i = 0; i < 500; i++) {
            if (i % 10 == 0) {
                smallSbf.add("persistent");
            } else {
                smallSbf.add(UUID.randomUUID().toString());
            }
        }

        assertTrue(smallSbf.mightContain("persistent"));
    }

    @Test
    void eventualFalseNegative() {
        StableBloomFilter<String> tinySbf = StableBloomFilter.create(20, 2, 15, (byte) 1);

        tinySbf.add("old");
        assertTrue(tinySbf.mightContain("old"));

        boolean becameFalseNegative = false;
        for (int i = 0; i < 200; i++) {
            tinySbf.add(UUID.randomUUID().toString());
            if (!tinySbf.mightContain("old")) {
                becameFalseNegative = true;
                break;
            }
        }

        assertTrue(becameFalseNegative,
                "Expected decay to eventually cause a false negative for 'old' given aggressive eviction settings");
    }
}
