package me.salamander.ourea.modules.fractal;

import me.salamander.ourea.modules.NoiseSampler;

public class FBM implements NoiseSampler {
    private NoiseSampler source;
    private int octaves;
    private float persistence = 0.5f;
    private float lacunarity = 2.0f;
    private float scale;

    public FBM(){
        calculateScale();
    }

    public FBM(NoiseSampler source, int octaves, float persistence, float lacunarity) {
        this.source = source;
        this.persistence = persistence;
        this.lacunarity = lacunarity;
        this.octaves = octaves;
        calculateScale();
    }

    private void calculateScale(){
        //Scale = 1 + persistance + persistance^2 + ... + persistance^(octaves-1)
        this.scale = (float) ((persistence - 1) / (Math.pow(persistence, octaves) - 1)); //Magic!
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
    public void setSalt(long salt) {

    }

    @Override
    public void setFrequency(float frequency) {

    }

    @Override
    public float sample(float x, float y, long seed) {
        float value = 0;
        float frequency = 1;
        float amplitude = 1;
        for (int i = 0; i < octaves; i++) {
            value += source.sample(x * frequency, y * frequency, seed) * amplitude;
            frequency *= lacunarity;
            amplitude *= persistence;
            seed *= 74247473L;
        }
        return value * scale;
    }

    @Override
    public float sample(float x, float y, float z, long seed) {
        float value = 0;
        float frequency = 1;
        float amplitude = 1;
        for (int i = 0; i < octaves; i++) {
            value += source.sample(x * frequency, y * frequency, seed) * amplitude;
            frequency *= lacunarity;
            amplitude *= persistence;
            seed *= 74247473L;
        }
        return value * scale;
    }
}
