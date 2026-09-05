package com.arroyo.probabilistic.cms;

import java.util.Objects;

public class CountMinSketch {
    private final int[][] countArray;
    private final int columns; // w
    private final int rows; //number of independent hash functions (d)

    public CountMinSketch(int[][] countArray, int columns, int rows) {
        this.countArray = countArray;
        this.columns = columns;
        this.rows = rows;
    }

    public boolean add(String x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] r = new int[rows]; //run through hashes
        for (int i = 0; i < r.length; i++) {
            countArray[i][r[i]] += 1;
        }
        return true;
    }

    public int minimalFrequency(String x) {
        Objects.requireNonNull(x, "Element must not be null");
        int[] r = new int[rows]; //run through hashes
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < r.length; i++) {
            if(countArray[i][r[i]] < min) min = countArray[i][r[i]];
        }
        if(min == Integer.MAX_VALUE) min = 0;
        return min;
    }
}
