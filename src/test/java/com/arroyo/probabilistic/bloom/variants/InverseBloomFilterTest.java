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

public class InverseBloomFilterTest {

    private InverseBloomFilter<String> ibf;

    @BeforeEach
    void setUp() {
        ibf = InverseBloomFilter.create(1000);
    }

    @Test
    void insertTest() {
        assertFalse(ibf.addCheckElement("Palindrome"));
    }

    @Test
    void duplicateIsDetected() {
        ibf.addCheckElement("Palindrome");
        assertTrue(ibf.addCheckElement("Palindrome"));
    }

    @Test
    void singleLetterDuplicate() {
        ibf.addCheckElement("X");
        assertTrue(ibf.addCheckElement("X"));
    }

    @Test
    void emptyStringDuplicate() {
        ibf.addCheckElement("");
        assertTrue(ibf.addCheckElement(""));
    }

    @Test
    void longStringDuplicate() {
        String x = "asdasfasdasdasfasdasfasdddddfdsgfdsgadfdsgnsifansignadifSNIGBNASIDASFaafasfasnigiSNFIASNGIASNFIASGNaodasfhefiaN";
        ibf.addCheckElement(x);
        assertTrue(ibf.addCheckElement(x));
    }

    @Test
    void nullElementThrows() {
        assertThrows(NullPointerException.class, () -> ibf.addCheckElement(null));
    }

    @Test
    void nonPositiveArraySizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> InverseBloomFilter.create(0));
    }

    @Test
    void createOverloadTest() {
        ElementConverter<String> eC = ElementConverters.stringConverter();
        HashFunction h = HashFunctions.seeded(23);

        InverseBloomFilter<String> ibf2 = InverseBloomFilter.create(1000, h, eC);
        assertFalse(ibf2.addCheckElement("Odyssey"));
        assertTrue(ibf2.addCheckElement("Odyssey"));
    }

    @Test
    void noFPDetected() {
        InverseBloomFilter<String> tinyIbf = InverseBloomFilter.create(3);

        List<String> distinctElements = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            distinctElements.add(UUID.randomUUID().toString());
        }

        for (String x : distinctElements) {
            assertFalse(tinyIbf.addCheckElement(x),
                    "False positive for genuinely unique element: " + x);
        }
    }

    @Test
    void collisionFalseNegative() {
        InverseBloomFilter<String> tinyIbf = InverseBloomFilter.create(1);

        tinyIbf.addCheckElement("first");
        tinyIbf.addCheckElement("second");

        assertFalse(tinyIbf.addCheckElement("first"));
    }
}
