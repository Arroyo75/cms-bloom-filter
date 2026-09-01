package com.arroyo.probabilistic.bloom;

import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;
import com.arroyo.probabilistic.util.SizingCalculator;

import java.util.BitSet;
import java.util.Objects;

/**
 * Bloom filter; a space-efficient probabilistic data structure for
 * approximate membership testing.
 * <p>
 * Guarantees no false negatives - if {@link #mightContain} returns
 * {@code false}, the element was definitely never added. It may however
 * return false positives - {@code true} does not guarantee the element
 * was actually added, only that it probably was.
 * </p>
 */
public class BloomFilter {
    private final BitSet bitArray;
    private final int numOfHash;
    private final int arraySize;
    public final HashFunction h1;
    public final HashFunction h2;

    private BloomFilter(int arraySize, int numOfHash) {
        this(arraySize, numOfHash, HashFunctions.primary(), HashFunctions.secondary());
    }

    private BloomFilter(int arraySize, int numOfHash, HashFunction h1, HashFunction h2) {
        if(arraySize <= 0) {
            throw new IllegalArgumentException("M (arraySize) must be at least 1");
        }
        if(numOfHash <= 0) {
            throw new IllegalArgumentException("K (numOfHash) must be at least 1");
        }
        this.bitArray = new BitSet(arraySize);
        this.numOfHash = numOfHash;
        this.arraySize = arraySize;
        this.h1 = h1;
        this.h2 = h2;
    }

    /**
     * Static factory method creating instance of BloomFilter
     * @param m is a size of bit array; must be positive
     * @param k is a number of hash functions used; must be positive
     * @return new instance of BloomFilter
     */
    public static BloomFilter create(int m, int k) {
        return new BloomFilter(m, k);
    }

    /**
     * Overloaded factory from above, allows to pass own HashFunction
     * functional interfaces.
     * @param m is a size of bit array; must be positive
     * @param k is a number of hash functions used; must be positive
     * @param h1 hash function used as a base
     * @param h2 hash function used as a step (should not be the same as above)
     * @return new instance of BloomFilter
     */
    public static BloomFilter create(int m, int k, HashFunction h1, HashFunction h2) {
        return new BloomFilter(m, k, h1, h2);
    }

    /**
     * Creates BloomFilter based on how many elements should it be able to store
     * and the acceptable False Positive Rate.
     * @param n is an approximated number of the elements that the filter should hold.
     * @param p is an acceptable False Positive Rate, so how much % of elements
     *          can the filter determine to be present, but are actually not there.
     * @return
     */
    public static BloomFilter create(int n, double p) {
        int m = SizingCalculator.optimalM(n, p);
        int k = SizingCalculator.optimalK(m, n);
        return new BloomFilter(m, k);
    }

    /**
     * Same as above but with custom HashFunction's.
     * Check two methods above.
     */
    public static BloomFilter create(int n, double p, HashFunction h1, HashFunction h2) {
        int m = SizingCalculator.optimalM(n, p);
        int k = SizingCalculator.optimalK(m, n);
        return new BloomFilter(m, k, h1, h2);
    }

    /**
     * Adds an element to the filter.
     *
     * @param x the element to add; must not be null
     * @throws NullPointerException if x is null
     */
    public boolean add(String x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] h = bitsFor(x);
        for (int bit : h) {
            bitArray.set(bit, true);
        }
        return true;
    }

    /**
     * Checks for an element in the filter
     *
     * @param x the element to check for; must not be null
     * @throws NullPointerException if x is null
     */
    public boolean mightContain(String x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] h = bitsFor(x);
        for (int bit : h) {
            if(!bitArray.get(bit)) {
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
    private int[] bitsFor(String x) {
        long base = h1.hash(x);
        long step = h2.hash(x);
        if(step == 0) step = 1;
        int[] bits = new int[numOfHash];
        for (int i = 0; i < numOfHash; i++) {
            long h = base + (long) i * step;
            bits[i] = (int) Math.floorMod(h, (long)arraySize);
        }
        return bits;
    }
}
