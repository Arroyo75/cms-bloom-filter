package com.arroyo.probabilistic;

import com.arroyo.probabilistic.bloom.BloomFilter;
import com.arroyo.probabilistic.hash.ElementConverter;
import com.arroyo.probabilistic.hash.ElementConverters;
import com.arroyo.probabilistic.hash.HashFunction;
import com.arroyo.probabilistic.hash.HashFunctions;

public class Main {
    static void main(String[] args) {

        ElementConverter<Integer> eC = ElementConverters.standardConverter();
        HashFunction seed1 = HashFunctions.seeded(23);
        HashFunction seed2 = HashFunctions.seeded(31);

        BloomFilter<Integer> bf = BloomFilter.create(5000, 10, eC, seed1, seed2);
        System.out.println(bf.add(55));
        System.out.println(bf.mightContain(56));
        System.out.println(bf.mightContain(5));
        System.out.println(bf.mightContain(555));
        System.out.println(bf.mightContain(55));
    }
}
