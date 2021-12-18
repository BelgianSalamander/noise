package me.salamander.ourea.modules.source.coord;

import me.salamander.ourea.glsl.transpile.annotation.Inline;
import me.salamander.ourea.modules.NoiseSampler;

public class Y implements NoiseSampler {
    @Override
    @Inline
    public float sample(float x, float y, int seed) {
        return y;
    }

    @Override
    @Inline
    public float sample(float x, float y, float z, int seed) {
        return y;
    }
}
