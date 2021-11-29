package me.salamander.ourea.modules.source.coord;

import me.salamander.ourea.modules.NoiseSampler;

public class X implements NoiseSampler {
    @Override
    public void setSalt(int salt) {

    }

    @Override
    public void setFrequency(float frequency) {

    }

    @Override
    public float sample(float x, float y, int seed) {
        return x;
    }

    @Override
    public float sample(float x, float y, float z, int seed) {
        return x;
    }
}
