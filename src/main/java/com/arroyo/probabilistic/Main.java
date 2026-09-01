package com.arroyo.probabilistic;

import com.arroyo.probabilistic.bloom.BloomFilter;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;

public class Main {
    public static void main(String[] args) {

        HashFunction seed1 = HashFunctions.seeded(23);
        HashFunction seed2 = HashFunctions.seeded(31);

        BloomFilter bf = BloomFilter.create(5000, 10, seed1, seed2);
        System.out.println(bf.add("Bakersfield"));
        System.out.println(bf.mightContain("Walter"));
        System.out.println(bf.mightContain("Baker"));
        System.out.println(bf.mightContain("Bakersfieldday"));
        System.out.println(bf.mightContain("Bakersfield"));

    }
}
