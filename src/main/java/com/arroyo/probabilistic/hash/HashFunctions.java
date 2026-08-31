package com.arroyo.probabilistic.hash;

public class HashFunctions {

    public static HashFunction primary() {
        return String::hashCode;
    }

    public static HashFunction secondary() {
        return x -> new StringBuilder(x).reverse().toString().hashCode();
    }

    public static HashFunction seeded(int seed) {
        return x -> (x.hashCode() ^ seed) * 0x9e3779b9;
    }
}
