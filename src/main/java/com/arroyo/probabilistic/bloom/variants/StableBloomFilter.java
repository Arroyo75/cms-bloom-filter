package com.arroyo.probabilistic.bloom.variants;

import com.arroyo.probabilistic.bloom.BloomFilter;
import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;

import java.util.BitSet;
import java.util.Objects;
import java.util.Random;

/**
 * Stable Bloom filter; a variant of the standard Bloom filter designed
 * for genuinely unbounded, continuous streams, using fixed memory
 * forever rather than growing (like {@link ScalableBloomFilter}) or
 * degrading in accuracy as more elements are inserted.
 * <p>
 * <b>Unlike every other filter in this library, this structure does
 * NOT guarantee an absence of false negatives.</b> An element that was
 * genuinely added earlier may later report {@code false} from
 * {@link #mightContain}, if enough subsequent insertions have randomly
 * decayed the counters it depends on. This is a deliberate
 * trade-off: rather than growing memory indefinitely to remember every
 * element ever seen, this filter lets old information decay away
 * automatically, keeping memory usage fixed regardless of how long the
 * stream runs.
 * </p>
 * On every {@link #add}, {@code P} ({@link #numOfDecrements}) randomly
 * chosen cells, unrelated to the element being added, are each
 * decremented by 1 (till 0), making room for new data. The
 * element's own k hash-derived positions are then set to {@code Max}
 * ({@link #maxValue}), the highest possible counter value, giving
 * freshly-inserted (or recently re-inserted) elements the most
 * resistance to being evicted by future decay. Membership is checked
 * the same way as {@link CountingBloomFilter}, all k positions must be
 * nonzero.
 * <p>
 * Recommended values for {@code Max} are small (1, 3, 7) per the
 * original Stable Bloom Filter paper (Deng &amp; Rafiei); larger values
 * reduce false negatives at the cost of needing proportionally more
 * cells decremented per insertion.
 */
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

    /**
     * Overloaded factory from above, allows to pass own ElementConverter
     * and HashFunction implementations.
     * @param m is a size of the counter array; must be positive
     * @param k is a number of hash functions used; must be positive
     * @param p is the number of randomly chosen cells decremented on
     *          every add; must be positive
     * @param max is the counter ceiling freshly-inserted positions are
     *            set to; must be between 1 and 127. Recommended values
     *            are 1, 3, or 7.
     * @param eC element converter that converts type T into String in order
     *           to make it hashable
     * @param h1 hash function used as a base
     * @param h2 hash function used as a step (should not be the same as above)
     * @return new instance of StableBloomFilter
     */
    public static <T> StableBloomFilter<T> create(int m, int k, int p, byte max, ElementConverter<T> eC, HashFunction h1, HashFunction h2) {
        return new StableBloomFilter<>(m, k, p, max, eC, h1, h2);
    }

    /**
     * Adds an element to the filter. First decrements P randomly chosen
     * cells by 1 to make room, then sets the element's k
     * hash-derived positions to Max.
     *
     * @param x the element to add; must not be null
     * @throws NullPointerException if x is null
     */
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

    /**
     * Checks for an element in the filter. May return a false negative
     * for an added element whose positions have since decayed
     * to 0 through unrelated calls to {@link #add}.
     *
     * @param x the element to check for; must not be null
     * @throws NullPointerException if x is null
     */
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

}
