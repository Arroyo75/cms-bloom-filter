package com.arroyo.probabilistic.bloom.variants;

import com.arroyo.probabilistic.bloom.BloomFilter;
import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;

import java.util.BitSet;
import java.util.Objects;
import java.util.Random;

public class StableBloomFilter<T> {
    private final byte[] countArray;
    private final int numOfHash;
    private final int arraySize;
    private final byte maxValue;
    private static final int VALUE_OF_DECREMENT = 1;
    private final int numOfDecrements;
    private final ElementConverter<T> elementConverter;
    private final HashFunction h1;
    private final HashFunction h2;
    private final Random random = new Random();

    private StableBloomFilter(int arraySize, int numOfHash, int numOfDecrements, byte maxValue) {
        this(arraySize, numOfHash, numOfDecrements, maxValue, ElementConverters.standardConverter(), HashFunctions.primary(), HashFunctions.secondary());
    }

    private StableBloomFilter(int arraySize, int numOfHash, int numOfDecrements, byte maxValue, ElementConverter<T> elementConverter, HashFunction h1, HashFunction h2) {
        if(arraySize <= 0) {
            throw new IllegalArgumentException("M (arraySize) must be at least 1");
        }
        if(numOfHash <= 0) {
            throw new IllegalArgumentException("K (numOfHash) must be at least 1");
        }
        if(numOfDecrements <= 0) {
            throw new IllegalArgumentException("P (numOfDecrements) must be at least 1");
        }
        if(maxValue <= 0) {
            throw new IllegalArgumentException("Max (maxValue) must be (0, 128)");
        }
        this.countArray = new byte[arraySize];
        this.numOfHash = numOfHash;
        this.arraySize = arraySize;
        this.numOfDecrements = numOfDecrements;
        this.maxValue = maxValue;
        this.elementConverter = elementConverter;
        this.h1 = h1;
        this.h2 = h2;
    }

    public static <T> StableBloomFilter<T> create(int m, int k, int p, byte max) {
        return new StableBloomFilter<>(m, k, p, max);
    }

    public static <T> StableBloomFilter<T> create(int m, int k, int p, byte max, ElementConverter<T> eC, HashFunction h1, HashFunction h2) {
        return new StableBloomFilter<>(m, k, p, max, eC, h1, h2);
    }

    public boolean add(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        for(int i = 0; i < numOfDecrements; i++) {
            int rn = random.nextInt(arraySize);
            if (countArray[rn] != 0) {
                countArray[rn] -= VALUE_OF_DECREMENT;
            }
        }
        int[] h = bitsFor(x);
        for (int bit : h) {
            countArray[bit] = maxValue;
        }
        return true;
    }

    public boolean mightContain(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] h = bitsFor(x);
        for (int bit : h) {
            if(countArray[bit] == 0) {
                return false;
            }
        }
        return true;
    }

    private int[] bitsFor(T x) {
        String s = elementConverter.toHashable(x);
        long base = h1.hash(s);
        long step = h2.hash(s);
        if(step == 0) step = 1;
        int[] bits = new int[numOfHash];
        for (int i = 0; i < numOfHash; i++) {
            long h = base + (long) i * step;
            bits[i] = (int) Math.floorMod(h, (long)arraySize);
        }
        return bits;
    }

}
