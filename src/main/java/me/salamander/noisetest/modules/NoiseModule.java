package me.salamander.noisetest.modules;

public interface NoiseModule {
    float sample(float x, float y); //TODO: Make this take and return floats. Double is not needed
    default void setSeed(long s){

    }

    long getSeed();
}
