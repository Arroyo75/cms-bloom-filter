package com.arroyo.probabilistic.hash;

@FunctionalInterface
public interface ElementConverter<T> {
        String toHashable(T t);
}
