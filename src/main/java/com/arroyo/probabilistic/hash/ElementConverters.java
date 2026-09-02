package com.arroyo.probabilistic.hash;

import java.nio.charset.StandardCharsets;

public class ElementConverters {
    public static ElementConverter<String> stringConverter() {
        return x -> x;
    }

    public static ElementConverter<byte[]> byteArrayConverter() {
        return x -> new String(x, StandardCharsets.ISO_8859_1);
    }

    public static <T> ElementConverter<T> standardConverter() {
        return Object::toString;
    }
}
