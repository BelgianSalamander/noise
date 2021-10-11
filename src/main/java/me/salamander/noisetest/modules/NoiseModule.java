package me.salamander.noisetest.modules;

public interface NoiseModule {
    double sample(double x, double y);
    default void setSeed(long s){

    }

    long getSeed();
}
