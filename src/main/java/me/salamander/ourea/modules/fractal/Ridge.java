package me.salamander.ourea.modules.fractal;

import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.util.MathHelper;

//TODO: This ridge noise is really bad.
public class Ridge implements NoiseSampler{
    private NoiseSampler source;
    private int octaves;
    private float persistence = 0.5f;
    private float lacunarity = 2.0f;
    private float scale;

    public Ridge(){
        calculateScale();
    }

    public Ridge(NoiseSampler source, int octaves, float persistence, float lacunarity) {
        this.source = source;
        this.persistence = persistence;
        this.lacunarity = lacunarity;
        this.octaves = octaves;
        calculateScale();
    }

    private void calculateScale(){
        //Scale = 1 + persistance + persistance^2 + ... + persistance^(octaves-1)
        this.scale = (float) ((persistence - 1) / ((Math.pow(persistence, octaves) - 1) * 0.31f)); //Magic!
    }

    public void setSource(NoiseSampler source) {
        this.source = source;
    }

    public void setOctaves(int octaves) {
        this.octaves = octaves;
        calculateScale();
    }

    public void setPersistence(float persistence) {
        this.persistence = persistence;
        calculateScale();
    }

    public void setLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
        calculateScale();
    }

    @Override
    public float sample(float x, float y, int seed) {
        float value = 0;
        float frequency = 1;
        float amplitude = 1;

        for (int i = 0; i < octaves; i++) {
            float signal = Math.abs(source.sample(x * frequency, y * frequency, seed));
            value += (signal - 0.5f) * amplitude;
            amplitude *= (1 - signal) * 0.5f + 0.5f;

            frequency *= lacunarity;
            amplitude *= persistence;
            seed *= 74247473L;
        }

        return value * scale;
    }

    @Override
    public float sample(float x, float y, float z, int seed) {
        float value = 0;
        float frequency = 1;
        float amplitude = 1;

        for (int i = 0; i < octaves; i++) {
            float signal = Math.abs(source.sample(x * frequency, y * frequency, z * frequency, seed));
            value += (signal - 0.5f) * amplitude;
            amplitude *= (1 - signal) * 0.4f + 0.6f;

            frequency *= lacunarity;
            amplitude *= persistence;
            seed *= 74247473L;
        }

        return value * scale;
    }
}
