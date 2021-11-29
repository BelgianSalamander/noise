package me.salamander.ourea.modules;

public interface NoiseSampler {
    void setSalt(int salt);
    void setFrequency(float frequency);

    float sample(float x, float y, int seed);
    float sample(float x, float y, float z, int seed);

    static float safeSample(NoiseSampler sampler, float x, float y, int seed) {
        if(sampler == null) {
            return 0;
        }
        return sampler.sample(x, y, seed);
    }
}
