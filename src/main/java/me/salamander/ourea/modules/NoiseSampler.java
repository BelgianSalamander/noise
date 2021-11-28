package me.salamander.ourea.modules;

public interface NoiseSampler {
    void setSalt(long salt);
    void setFrequency(float frequency);

    float sample(float x, float y, long seed);
    float sample(float x, float y, float z, long seed);
}
