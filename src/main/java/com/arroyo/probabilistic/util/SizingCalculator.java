package com.arroyo.probabilistic.util;

public class SizingCalculator {

    private static final double LN2 = Math.log(2);

    /**
     * Formula to receive the optimal value of array size
     * derived from the general FPR formula.
     * m = -(n * ln(p)) / ln(2)^2
     * @param n is a number of elements added to array
     * @param p is an acceptable False Positive Rate
     * @return the optimal value of m (bit array size), rounded up to the
     * *         next prime to avoid degenerate Kirsch-Mitzenmacher index derivation
     * @throws IllegalArgumentException if n is not positive or p is not in (0, 1)
     */
    public static int optimalM(int n, double p) {

        if(n <= 0) {
            throw new IllegalArgumentException("N must be at least 1");
        }

        if(p <= 0 || p >= 1) {
            throw new IllegalArgumentException("FPR must be (0, 1)");
        }

        int m = (int) Math.ceil(-(n * Math.log(p)) / (LN2 * LN2));
        return nextPrimeNum(m);
    }

    /**
     * Formula to receive the optimal number of hash functions used on
     * the added element derived from the general FPR formula.
     * k = (m/n) * ln(2)
     * @param m is the bit array size
     * @param n is the number of elements added to array
     * @return the optimal value of k (number of hash functions used on the elements)
     * @throws IllegalArgumentException if n or m is not positive
     */
    public static int optimalK(int m, int n) {

        if(m <= 0) {
            throw new IllegalArgumentException("M (arraySize) must be at least 1");
        }

        if(n <= 0) {
            throw new IllegalArgumentException("N must be at least 1");
        }

        return (int) Math.round(((double) m / n) * LN2);
    }

    /**
     * Estimates the False Positive Rate of a Bloom filter given its size,
     * number of hash functions and number of added elements.
     * p ~= (1 - e^(-kn/m))^k
     * @param m is the bit arraySize
     * @param k is the number of hash functions
     * @param n is the number of added elements
     * @return the estimated False Positive Rate (0, 1)
     * @throws IllegalArgumentException if n or m or k is not positive
     */
    public static double estimatedFalsePositiveRate(int m, int k, int n) {

        if(m <= 0) {
            throw new IllegalArgumentException("M (arraySize) must be at least 1");
        }

        if(k <= 0) {
            throw new IllegalArgumentException("K (numOfHash) must be at least 1");
        }

        if(n <= 0) {
            throw new IllegalArgumentException("N must be at least 1");
        }

        return Math.pow(1 - (Math.exp(-(double)(k*n)/m)), k);
    }

    /**
     * Given the integer, returns the next prime number.
     * Needed for array size optimization.
     */
    public static int nextPrimeNum(int n) {
        if (n <= 2) return 2;
        int possible = (n % 2 == 0) ? n + 1 : n;
        while (!isPrimeNum(possible)) possible += 2;
        return possible;
    }

    /**
     * Checks if a number is prime number.
     */
    public static boolean isPrimeNum(int n) {
        if (n < 2) return false;
        for (int i = 2; (long) i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
