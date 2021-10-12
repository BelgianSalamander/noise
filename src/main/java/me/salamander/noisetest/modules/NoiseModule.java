package me.salamander.noisetest.modules;

public interface NoiseModule {
    double sample(double x, double y); //TODO: Make this take and return floats. Double is not needed
    default void setSeed(long s){

    }

    long getSeed();
}
