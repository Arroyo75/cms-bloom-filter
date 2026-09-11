package com.arroyo.probabilistic.cms;

import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;
import com.arroyo.probabilistic.util.SizingCalculator;

import java.util.Objects;

public class CountMinSketch<T> {
    private final int[][] countArray;
    private final int columns; // mirrors arraySize of Bloom Filter (w)
    private final int rows; //number of independent hash functions (d)
    private final ElementConverter<T> elementConverter;
    private final HashFunction[] hashFunctions;
    private int totalCount;

    private CountMinSketch(int columns, int rows) {

        HashFunction[] hf = new HashFunction[rows];
        ElementConverter<T> ec = ElementConverters.standardConverter();
        for (int i = 0; i < rows; i++) {
            hf[i] = HashFunctions.seeded(i);
        }
        this(columns, rows, ec, hf);
    }

    private CountMinSketch(int columns, int rows, ElementConverter<T> elementConverter, HashFunction[] hashFunctions) {
        if(columns <= 0) {
            throw new IllegalArgumentException("Columns must be at least 1");
        }
        if(rows <= 0) {
            throw new IllegalArgumentException("Rows must be at least 1");
        }
        this.countArray = new int[rows][columns];
        this.columns = columns;
        this.rows = rows;
        this.hashFunctions = hashFunctions;
        this.elementConverter = elementConverter;
        this.totalCount = 0;
    }

    public static <T> CountMinSketch<T> create(int columns, int rows) {
        return new CountMinSketch<>(columns, rows);
    }

    public static <T> CountMinSketch<T> create(int columns, int rows, ElementConverter<T> eC, HashFunction[] hf) {
        return new CountMinSketch<>(columns, rows, eC, hf);
    }

    public static <T> CountMinSketch<T> create(double epsilon, double delta) {
        return new CountMinSketch<>(SizingCalculator.optimalColumns(epsilon), SizingCalculator.optimalRows(delta));
    }

    public static <T> CountMinSketch<T> create(double epsilon, double delta, ElementConverter<T> eC, HashFunction[] hf) {
        return new CountMinSketch<>(SizingCalculator.optimalColumns(epsilon), SizingCalculator.optimalRows(delta), eC, hf);
    }

    /**
     * Adds an element to the array.
     *
     * @param x the element to add; must not be null
     * @throws NullPointerException if x is null
     */
    public boolean add(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] r = calculateHashes(x); //run through hashes
        for (int i = 0; i < r.length; i++) {
            countArray[i][r[i]] += 1;
        }
        totalCount++;
        return true;
    }

    /**
     * Checks for an element in the filter.
     *
     * @param x the element to check for; must not be null
     * @throws NullPointerException if x is null
     */
    public int minimalFrequency(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] r = calculateHashes(x); //run through hashes
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < r.length; i++) {
            int y = countArray[i][r[i]];
            if(y < min) min = y;
        }
        if(min == Integer.MAX_VALUE) min = 0;
        return min;
    }

    /**
     * For each row in the 2d array, method applies different
     * hash function to the x element receiving an index to be
     * checked/incremeneted. Those indexes for the element are returned
     * in a form of an array.
     * @param x the element to calculate hashes for
     * @return int array holding computed hash values for x.
     */
    private int[] calculateHashes(T x) {
        String s = elementConverter.toHashable(x);
        int[] res = new int[rows];
        for (int i = 0; i < rows; i++) {
            int y = hashFunctions[i].hash(s);
            res[i] = (int) Math.floorMod(y, (long) columns);
        }
        return res;
    }

    /**
     * Returns totalCount field value.
     * @return int representing a number of inserted elements.
     */
    public int totalInserted() {
        return totalCount;
    }
}
