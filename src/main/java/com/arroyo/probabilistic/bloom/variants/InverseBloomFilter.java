package com.arroyo.probabilistic.bloom.variants;

import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;

import java.util.BitSet;
import java.util.Objects;


/**
 * Inverse Bloom filter; a probabilistic data structure for duplicate
 * detection in a stream, with the opposite guarantee of a standard
 * Bloom filter.
 * <p>
 * Guarantees no false positives - if {@link #addCheckElement} returns
 * {@code true}, the exact same element was definitely present in this
 * filter's array at the time of the check. It may however return false
 * negatives - {@code false} does not guarantee the element was never
 * seen before, only that it was not found at its current slot, which
 * can happen if another element has since overwritten it.
 * </p>
 * Unlike BloomFilter, this structure stores the actual elements
 * (not just hash-derived bits or counters) and uses a single hash
 * function mapped to one slot per element - there is no k-hash
 * derivation, since each element occupies exactly one array position.
 */
public class InverseBloomFilter<T> {

    @SuppressWarnings("unchecked")
    private final T[] elementArray;
    private final int arraySize;
    private final HashFunction hashFunction;
    private final ElementConverter<T> elementConverter;

    private InverseBloomFilter(int arraySize) {
        this(arraySize, HashFunctions.primary(), ElementConverters.standardConverter());
    }

    private InverseBloomFilter(int arraySize, HashFunction hashFunction, ElementConverter<T> elementConverter) {
        if(arraySize <= 0) {
            throw new IllegalArgumentException("M (arraySize) must be at least 1");
        }
        this.elementArray = (T[]) new Object[arraySize];
        this.arraySize = arraySize;
        this.hashFunction = hashFunction;
        this.elementConverter = elementConverter;
    }

    /**
     * Static factory method creating an instance of InverseBloomFilter
     * with a default hash function and element converter.
     * @param m is a size of the element array; must be positive
     * @return new instance of InverseBloomFilter
     */
    public static <T> InverseBloomFilter<T> create(int m) {
        return new InverseBloomFilter<>(m);
    }

    /**
     * Overloaded factory from above, allows to pass own HashFunction
     * and ElementConverter implementations.
     * @param m is a size of the element array; must be positive
     * @param h hash function used to map an element to its slot
     * @param eC element converter that converts type T into String in order
     *           to make it hashable
     * @return new instance of InverseBloomFilter
     */
    public static <T> InverseBloomFilter<T> create(int m, HashFunction h, ElementConverter<T> eC) {
        return new InverseBloomFilter<>(m, h, eC);
    }

    /**
     * Checks whether the given element was already present at its slot,
     * then unconditionally stores it there, overwriting whatever was
     * previously in that slot.
     *
     * @param x the element to check and store; must not be null
     * @return {@code true} if the exact same element already occupied
     *         this slot (a confirmed duplicate); {@code false} otherwise,
     *         including if a different element occupied the slot
     * @throws NullPointerException if x is null
     */
    public boolean addCheckElement(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        int y = Math.floorMod(hashFunction.hash(elementConverter.toHashable(x)), arraySize);
        boolean isSame = x.equals(elementArray[y]);
        elementArray[y] = x;
        return isSame;
    }


}
