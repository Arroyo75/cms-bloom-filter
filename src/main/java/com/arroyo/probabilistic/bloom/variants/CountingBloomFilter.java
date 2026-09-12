package com.arroyo.probabilistic.bloom.variants;

import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;
import com.arroyo.probabilistic.util.SizingCalculator;

import java.util.Objects;

/**
 * Counting Bloom filter; a variant of the standard Bloom filter that
 * replaces each bit with a small counter, allowing element deletion.
 * <p>
 * Guarantees no false negatives for elements that have not been deleted.
 * If {@link #mightContain} returns {@code false}, the element was
 * definitely never added (or has since been fully deleted). It may
 * however return false positives, {@code true} does not guarantee the
 * element was actually added, only that it probably was.
 * </p>
 * Each of the k positions for an element is stored as an 8-bit unsigned
 * counter (0-255, backed by a signed {@code byte} with unsigned
 * interpretation via {@link #unsignedValue}), incremented on
 * {@link #add} and decremented on {@link #delete}.
 */
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

    /**
     * Adds an element to the filter, incrementing the counter at each of
     * its k positions.
     *
     * @param x the element to add; must not be null
     * @throws NullPointerException if x is null
     * @throws IllegalStateException if any of the element's positions
     *         is already at the maximum counter value (255)
     */
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

    /**
     * Deletes an element from the filter, decrementing the counter at
     * each of its k positions.
     * <p>
     * Only safe to call for elements that are actually known to have
     * been added, the filter cannot verify this on its own, since it
     * never stores elements themselves, only hash-derived counters.
     *
     * @param x the element to delete; must not be null
     * @throws NullPointerException if x is null
     * @throws IllegalStateException if any of the element's positions
     *         is already at 0, indicating it was never inserted or has
     *         already been fully deleted
     */
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

    /**
     * Checks for an element in the filter.
     *
     * @param x the element to check for; must not be null
     * @throws NullPointerException if x is null
     */
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

    /**
     * Derives k index positions from two independent hash values using the
     * Kirsch-Mitzenmacher optimization: h_i(x) = h1(x) + i * h2(x).
     * Avoids needing k truly independent hash functions.
     */
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

    /**
     * Calculates FPR for this filter's size, number of hashes,
     * and how many elements it will hold.
     * @param n is an approximated number of the elements that the filter should hold.
     * @return the estimated FPR
     */
    public double estimatedFalsePositiveRate(int n) {
        return SizingCalculator.estimatedFalsePositiveRate(arraySize, numOfHash, n);
    }

    /**
     * Interprets the stored signed byte at the given index as an
     * unsigned value in the range 0-255, since Java's byte type is
     * signed and cannot natively represent values above 127.
     */
    private int unsignedValue(int index) {
        return countArray[index] & 0xFF;
    }

}
