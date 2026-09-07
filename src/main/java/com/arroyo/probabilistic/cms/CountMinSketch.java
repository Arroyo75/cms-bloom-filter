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

    private CountMinSketch(int columns, int rows) {
        this.countArray = new int[rows][columns];
        this.columns = columns;
        this.rows = rows;
        this.hashFunctions = new HashFunction[rows];
        this.elementConverter = ElementConverters.standardConverter();
        for (int i = 0; i < rows; i++) {
            this.hashFunctions[i] = HashFunctions.seeded(i);
        }
    }

    public static <T> CountMinSketch<T> create(int columns, int rows) {
        return new CountMinSketch<>(columns, rows);
    }

    public static <T> CountMinSketch<T> create(double epsilon, double delta) {
        return new CountMinSketch<>(SizingCalculator.optimalColumns(epsilon), SizingCalculator.optimalRows(delta));
    }

    public boolean add(T x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] r = calculateHashes(x); //run through hashes
        for (int i = 0; i < r.length; i++) {
            countArray[i][r[i]] += 1;
        }
        return true;
    }

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

    private int[] calculateHashes(T x) {
        String s = elementConverter.toHashable(x);
        int[] res = new int[rows];
        for (int i = 0; i < rows; i++) {
            int y = hashFunctions[i].hash(s);
            res[i] = (int) Math.floorMod(y, (long) columns);
        }
        return res;
    }
}
