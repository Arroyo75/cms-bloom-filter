package com.arroyo.probabilistic;

import com.arroyo.probabilistic.bloom.BloomFilter;
import com.arroyo.probabilistic.bloom.variants.CountingBloomFilter;
import com.arroyo.probabilistic.bloom.variants.InverseBloomFilter;
import com.arroyo.probabilistic.bloom.variants.ScalableBloomFilter;
import com.arroyo.probabilistic.bloom.variants.StableBloomFilter;
import com.arroyo.probabilistic.cms.CountMinSketch;

public class Main {
    static void main(String[] args) {
        BloomFilter<Integer> bf = BloomFilter.create(1000, 5);
        CountMinSketch<Integer> cms = CountMinSketch.create(1000, 5);
        CountingBloomFilter<Integer> cbf = CountingBloomFilter.create(1000, 5);
        ScalableBloomFilter<Integer> sbf = ScalableBloomFilter.create(1000, 0.1);
        StableBloomFilter<Integer> stbf = StableBloomFilter.create(1000, 5, 5, (byte)7);
        InverseBloomFilter<Integer> ibf = InverseBloomFilter.create(1000);
    }
}
