package me.salamander.noisetest.modules.types;

public interface NoiseModule {
    double sample(double x, double y);
    default void setSeed(long s){

    }
}
