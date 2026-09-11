package com.arroyo.probabilistic.bloom.variants;

import com.arroyo.probabilistic.bloom.BloomFilter;
import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScalableBloomFilter<T> {
    private static final double FPR_TIGHTENING_RATIO = 0.9;

    private final List<FilterEntry<T>> bloomFilters = new ArrayList<>();
    private double lastFilterFPR;
    private final ElementConverter<T> elementConverter;
    private final HashFunction h1;
    private final HashFunction h2;

    private ScalableBloomFilter(int firstFilterCapacity, double firstFilterFPR) {
        this(firstFilterCapacity, firstFilterFPR, ElementConverters.standardConverter(), HashFunctions.primary(), HashFunctions.secondary());
    }

    private ScalableBloomFilter(int firstFilterCapacity, double firstFilterFPR, ElementConverter<T> elementConverter, HashFunction h1, HashFunction h2) {
        bloomFilters.add(new FilterEntry<>(
                BloomFilter.create(firstFilterCapacity, firstFilterFPR, elementConverter, h1, h2),
                firstFilterCapacity
        ));
        this.lastFilterFPR = firstFilterFPR;
        this.elementConverter = elementConverter;
        this.h1 = h1;
        this.h2 = h2;
    }

    public static <T> ScalableBloomFilter<T> create(int n, double p) {
        return new ScalableBloomFilter<>(n, p);
    }

    public static <T> ScalableBloomFilter<T> create(int n, double p, ElementConverter<T> eC, HashFunction h1, HashFunction h2) {
        return new ScalableBloomFilter<>(n, p, eC, h1, h2);
    }

    public boolean add(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        FilterEntry<T> fE =  bloomFilters.getLast();
        if(fE.added == fE.capacity) {
            double newFilterFPR = lastFilterFPR * FPR_TIGHTENING_RATIO;
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

    public boolean mightContain(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        for (FilterEntry<T> fE : bloomFilters) {
            if(fE.filter.mightContain(x)) return true;
        }
        return false;
    }

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
