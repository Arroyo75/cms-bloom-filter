package com.arroyo.probabilistic.bloom.variants;

import com.arroyo.probabilistic.bloom.BloomFilter;
import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Scalable Bloom filter; a variant of the standard Bloom filter that
 * handles an unbounded stream of elements by chaining together multiple
 * BloomFilter instances, rather than being sized for a single
 * fixed capacity up front.
 * <p>
 * BloomFilter degrades once more elements are inserted
 * than it was sized for. This structure instead allocates a new, additional filter
 * once the current one reaches its designed capacity, so growth never
 * degrades accuracy the way a single fixed-size filter would.
 * </p>
 * Guarantees no false negatives, if {@link #mightContain} returns
 * {@code false}, the element was definitely never added to any filter
 * in the chain. It may however return false positives, same as a
 * standard Bloom filter.
 * <p>
 * Each successive filter added to the chain is given a progressively
 * tighter target false positive rate, shrinking geometrically by
 * {@link #fprStepRatio} per generation. This compensates for the
 * fact that checking {@code n} chained filters compounds their
 * individual false positive rates - without tightening, the effective
 * false positive rate of the whole chain would drift upward, unbounded,
 * as more filters are added.
 */
public class ScalableBloomFilter<T> {

    private final double fprStepRatio;
    private final List<FilterEntry<T>> bloomFilters = new ArrayList<>();
    private double lastFilterFPR;
    private final ElementConverter<T> elementConverter;
    private final HashFunction h1;
    private final HashFunction h2;

    private ScalableBloomFilter(int firstFilterCapacity, double firstFilterFPR) {
        this(firstFilterCapacity, firstFilterFPR, 0.9, ElementConverters.standardConverter(), HashFunctions.primary(), HashFunctions.secondary());
    }

    private ScalableBloomFilter(int firstFilterCapacity, double firstFilterFPR, double fprStepRatio, ElementConverter<T> elementConverter, HashFunction h1, HashFunction h2) {
        if(firstFilterCapacity <= 0) {
            throw new IllegalArgumentException("M (arraySize) must be at least 1");
        }
        if(firstFilterFPR <= 0 || firstFilterFPR >= 1) {
            throw new IllegalArgumentException("FPR must be (0, 1)");
        }
        if(fprStepRatio <= 0 || fprStepRatio >= 1) {
            throw new IllegalArgumentException("FPR must be (0, 1)");
        }
        bloomFilters.add(new FilterEntry<>(
                BloomFilter.create(firstFilterCapacity, firstFilterFPR, elementConverter, h1, h2),
                firstFilterCapacity
        ));
        this.fprStepRatio = fprStepRatio;
        this.lastFilterFPR = firstFilterFPR;
        this.elementConverter = elementConverter;
        this.h1 = h1;
        this.h2 = h2;
    }

    public static <T> ScalableBloomFilter<T> create(int n, double p) {
        return new ScalableBloomFilter<>(n, p);
    }

    /**
     * Overloaded factory from above, allows to pass own ElementConverter
     * and HashFunction implementations, which are reused by every
     * filter created in the chain as it grows.
     * @param n is an approximated number of the elements the first
     *          internal filter should hold.
     * @param p is the acceptable False Positive Rate for the first
     *          internal filter.
     * @param eC element converter that converts type T into String in order
     *           to make it hashable
     * @param h1 hash function used as a base
     * @param h2 hash function used as a step (should not be the same as above)
     * @return new instance of ScalableBloomFilter
     */
    public static <T> ScalableBloomFilter<T> create(int n, double p, double r, ElementConverter<T> eC, HashFunction h1, HashFunction h2) {
        return new ScalableBloomFilter<>(n, p, r, eC, h1, h2);
    }

    /**
     * Adds an element to the filter. If the most recently created
     * internal filter has reached its designed capacity, a new filter
     * is first allocated and appended to the chain, with a tighter
     * target false positive rate than the one before it.
     *
     * @param x the element to add; must not be null
     * @throws NullPointerException if x is null
     */
    public boolean add(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        FilterEntry<T> fE =  bloomFilters.getLast();
        if(fE.added == fE.capacity) {
            double newFilterFPR = lastFilterFPR * fprStepRatio;
            fE = new FilterEntry<>(
                    BloomFilter.create(fE.capacity, newFilterFPR, elementConverter, h1, h2),
                    fE.capacity
            );
            bloomFilters.addLast(fE);
            this.lastFilterFPR = newFilterFPR;
        }
        fE.filter.add(x);
        fE.added += 1;
        return true;
    }

    /**
     * Checks for an element across every filter in the chain, returning
     * {@code true} as soon as any one of them reports the element might
     * be present.
     *
     * @param x the element to check for; must not be null
     * @throws NullPointerException if x is null
     */
    public boolean mightContain(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        for (FilterEntry<T> fE : bloomFilters) {
            if(fE.filter.mightContain(x)) return true;
        }
        return false;
    }

    /**
     * Pairs one internal BloomFilter with its designed capacity
     * and a running count of how many elements have been added to it so
     * far, so {@link #add} can detect when it's time to allocate the
     * next filter in the chain.
     */
    private static class FilterEntry<T> {
        final BloomFilter<T> filter;
        final int capacity;
        int added;

        FilterEntry(BloomFilter<T> filter, int capacity) {
            this.filter = filter;
            this.capacity = capacity;
            this.added = 0;
        }

    }
}
