package me.salamander.ourea.modules.source.coord;

import me.salamander.ourea.glsl.transpile.annotation.Inline;
import me.salamander.ourea.modules.NoiseSampler;

public class Z implements NoiseSampler {
    @Override
    public void setSalt(int salt) {

    }

    @Override
    public void setFrequency(float frequency) {

    }

    @Override
    public float sample(float x, float y, int seed) {
        throw new UnsupportedOperationException("Cannot get Z of 2D noise");
    }

    @Override
    @Inline
    public float sample(float x, float y, float z, int seed) {
        return z;
    }
}
