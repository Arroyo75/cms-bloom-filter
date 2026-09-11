package com.arroyo.probabilistic.bloom.variants;

import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;
import com.arroyo.probabilistic.util.SizingCalculator;

import java.util.Objects;

public class CountingBloomFilter<T> {
    private final byte[] countArray;
    private final int numOfHash;
    private final int arraySize;
    private final ElementConverter<T> elementConverter;
    private final HashFunction h1;
    private final HashFunction h2;

    private CountingBloomFilter(int arraySize, int numOfHash) {
        this(arraySize, numOfHash, ElementConverters.standardConverter(), HashFunctions.primary(), HashFunctions.secondary());
    }

    private CountingBloomFilter(int arraySize, int numOfHash, ElementConverter<T> elementConverter, HashFunction h1, HashFunction h2) {
        if(arraySize <= 0) {
            throw new IllegalArgumentException("M (arraySize) must be at least 1");
        }
        if(numOfHash <= 0) {
            throw new IllegalArgumentException("K (numOfHash) must be at least 1");
        }
        this.countArray = new byte[arraySize];
        this.numOfHash = numOfHash;
        this.arraySize = arraySize;
        this.elementConverter = elementConverter;
        this.h1 = h1;
        this.h2 = h2;
    }

    public static <T> CountingBloomFilter<T> create(int m, int k) {
        return new CountingBloomFilter<>(m, k);
    }

    public static <T> CountingBloomFilter<T> create(int m, int k, ElementConverter<T> eC, HashFunction h1, HashFunction h2) {
        return new CountingBloomFilter<>(m, k, eC, h1, h2);
    }

    public static <T> CountingBloomFilter<T> create(int n, double p) {
        int m = SizingCalculator.optimalM(n, p);
        int k = SizingCalculator.optimalK(m, n);
        return new CountingBloomFilter<>(m, k);
    }

    public static <T> CountingBloomFilter<T> create(int n, double p, ElementConverter<T> eC, HashFunction h1, HashFunction h2) {
        int m = SizingCalculator.optimalM(n, p);
        int k = SizingCalculator.optimalK(m, n);
        return new CountingBloomFilter<>(m, k, eC, h1, h2);
    }

    public boolean add(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] h = bitsFor(x);
        for(int bit : h) {
            if(unsignedValue(bit) == 255) throw new IllegalStateException("Filter overflow");
        }
        for (int bit : h) {
            countArray[bit] += 1;
        }
        return true;
    }

    public boolean delete(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] h = bitsFor(x);
        for(int bit : h) {
            if(unsignedValue(bit) == 0) throw new IllegalStateException("Element was never inserted");
        }
        for (int bit : h) {
            countArray[bit] -= 1;
        }
        return true;
    }

    public boolean mightContain(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] h = bitsFor(x);
        for (int bit : h) {
            if(unsignedValue(bit) == 0) {
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

    public double estimatedFalsePositiveRate(int n) {
        return SizingCalculator.estimatedFalsePositiveRate(arraySize, numOfHash, n);
    }

    private int unsignedValue(int index) {
        return countArray[index] & 0xFF;
    }

}
