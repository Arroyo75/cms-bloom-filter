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

public class ScalableBloomFilterTest {

    private ScalableBloomFilter<String> sbf;

    @BeforeEach
    void setUp() {
        sbf = ScalableBloomFilter.create(1000, 0.01);
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
    void createOverloadTest() {
        ElementConverter<String> eC = ElementConverters.stringConverter();
        HashFunction h1 = HashFunctions.seeded(23);
        HashFunction h2 = HashFunctions.seeded(31);

        ScalableBloomFilter<String> sbf2 = ScalableBloomFilter.create(1000, 0.01, 0.9, eC, h1, h2);
        sbf2.add("Odyssey");
        assertTrue(sbf2.mightContain("Odyssey"));
    }

    @Test
    void growingPastFirstFilterCapacityStillFindsEarlyElements() {
        // Small initial capacity so we can force multiple generations
        // in the chain without inserting huge numbers of elements.
        ScalableBloomFilter<String> smallSbf = ScalableBloomFilter.create(100, 0.01);

        List<String> elements = new ArrayList<>();
        // Insert well past 3 generations worth of capacity.
        for (int i = 0; i < 350; i++) {
            String x = UUID.randomUUID().toString();
            smallSbf.add(x);
            elements.add(x);
        }

        // Elements from every generation - including the very first,
        // long-since-full filter - must still be found.
        for (String x : elements) {
            assertTrue(smallSbf.mightContain(x), "False negative for: " + x);
        }
    }

    @Test
    void falsePositiveRateStaysReasonableAcrossGrowth() {
        int initialCapacity = 500;
        double targetRate = 0.01;
        double tighteningRatio = 0.9;
        ScalableBloomFilter<String> growingSbf = ScalableBloomFilter.create(initialCapacity, targetRate);

        int totalInserted = initialCapacity * 5;
        for (int i = 0; i < totalInserted; i++) {
            growingSbf.add(UUID.randomUUID().toString());
        }

        int fp = 0;
        int testSize = 7000;
        for (int i = 0; i < testSize; i++) {
            if (growingSbf.mightContain(UUID.randomUUID().toString())) {
                fp++;
            }
        }

        double testRate = (double) fp / testSize;

        int expectedGenerations = 5;
        double expectedBound = 0;
        double currentFPR = targetRate;
        for (int i = 0; i < expectedGenerations; i++) {
            expectedBound += currentFPR;
            currentFPR *= tighteningRatio;
        }

        assertTrue(testRate < expectedBound * 1.5,
                "False Positive rate " + testRate + " exceeds tolerance of " + (expectedBound * 1.5));
    }
}
