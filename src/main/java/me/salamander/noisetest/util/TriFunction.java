package me.salamander.noisetest.util;

@FunctionalInterface
public interface TriFunction<T, U, V, B> {
    B apply(T first, U second, V third);
}
