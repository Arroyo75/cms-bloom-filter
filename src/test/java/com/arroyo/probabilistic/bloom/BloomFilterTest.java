package com.arroyo.probabilistic.bloom;

import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BloomFilterTest {

    private BloomFilter bf;

    @BeforeEach
    void setUp() {
        bf = BloomFilter.create(1000, 5);
    }

    @Test
    void insertedElementIsFound() {
        bf.add("Palindrome");
        assertTrue(bf.mightContain("Palindrome"));
    }

    @Test
    void multipleInsertedElementsAreFound() {
        bf.add("Pneumatic");
        bf.add("Hippopotamus");
        bf.add("Flowery");

        assertTrue(bf.mightContain("Pneumatic"));
        assertTrue(bf.mightContain("Hippopotamus"));
        assertTrue(bf.mightContain("Flowery"));
    }

    @Test
    void checkForNonexistentRecord() {
        assertFalse(bf.mightContain("okay"));
    }

    @Test
    void insertedSingleLetterIsFound() {
        bf.add("X");
        assertTrue(bf.mightContain("X"));
    }

    @Test
    void insertedEmptyString() {
        bf.add("");
        assertTrue(bf.mightContain(""));
    }

    @Test
    void insertedVeryLongStringIsFound() {
        bf.add("asdasfasdasdasfasdasfasdddddfdsgfdsgadfdsgnsifansignadifSNIGBNASIDASFaafasfasnigiSNFIASNGIASNFIASGNaodasfhefiaN");
        assertTrue(bf.mightContain("asdasfasdasdasfasdasfasdddddfdsgfdsgadfdsgnsifansignadifSNIGBNASIDASFaafasfasnigiSNFIASNGIASNFIASGNaodasfhefiaN"));
    }

    @Test
    void insertedNullHandled() {
        assertThrows(NullPointerException.class, () -> bf.add(null));
    }

    @Test
    void checkForNull() {
        assertThrows(NullPointerException.class, () -> bf.mightContain(null));
    }

    @Test
    void nonPositiveArraySizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> BloomFilter.create(0, 5));
    }

    @Test
    void nonPositiveHashCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> BloomFilter.create(1000, 0));
    }

    @Test
    void createOverloadTest() {
        HashFunction h1 = HashFunctions.seeded(23);
        HashFunction h2 = HashFunctions.seeded(31);

        BloomFilter bf2 = BloomFilter.create(1000, 5, h1, h2);
        bf2.add("Odyssey");
        assertTrue(bf2.mightContain("Odyssey"));
    }

    @Test
    void falsePositiveRateIsAcceptable() {
        int n = 5000;
        double targetRate = 0.01;
        double ln2 = Math.log(2);
        int m = (int) Math.ceil(-(n * Math.log(targetRate)) / (ln2 * ln2));
        m = BloomFilter.nextPrimeNum(m);
        int k = (int) Math.round(((double) m / n) * ln2);

        BloomFilter bf3 = BloomFilter.create(m, k);
        for (int i = 0; i < n; i++) {
            bf3.add(UUID.randomUUID().toString());
        }

        int fp = 0;
        int testSize = 7000;

        for(int i = 0; i < testSize; i++) {
            if(bf3.mightContain(UUID.randomUUID().toString())) {
                fp++;
            }
        }

        double testRate = (double) fp / testSize;

        assertTrue(testRate < targetRate * 2,
                "False Positive rate " + testRate + " acquired during tests exceeds tolerance fo taget " + targetRate);

    }


}
