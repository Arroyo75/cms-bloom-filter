package com.arroyo.probabilistic.hash;

@FunctionalInterface
public interface HashFunction {
    int hash(String x);
}
