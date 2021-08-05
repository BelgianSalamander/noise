package me.salamander.noisetest.util;

public class Pair<T, Z> {
    private final T first;
    private final Z second;

    public Pair(T first, Z second){
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public Z getSecond() {
        return second;
    }
}
