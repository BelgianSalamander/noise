package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.noise.PerlinNoise2D;

import java.util.Random;

public class Perlin implements NoiseModule {
    private final long seed;
    private PerlinNoise2D[] perlinSamplers;
    private int octaves;
    private double frequency = 1.0;
    private double persistence = 0.5;
    private double lacunarity = 2.0;

    public Perlin(){
        this.seed = (new Random()).nextLong();
        this.octaves = 6;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers();
    }

    public Perlin(int octaves){
        this.seed = (new Random()).nextLong();
        this.octaves = octaves;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers();
    }

    public Perlin(int octaves, long seed){
        this.seed = seed;
        this.octaves = octaves;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers();
    }

    private void createSamplers(){
        Random random = new Random(seed);

        for(int i = 0; i < octaves; i++){
            perlinSamplers[i] = new PerlinNoise2D(random.nextLong());
        }
    }

    @Override
    public double sample(double x, double y) {
        double total = 0;
        for(int i = 0; i < octaves; i++)
            total += Math.pow(persistence, i) * perlinSamplers[i].sample(x * frequency * Math.pow(lacunarity, i), y * frequency * Math.pow(lacunarity, i));
        return total;
    }

    public void setPersistence(double persistence) {
        this.persistence = persistence;
    }

    public void setLacunarity(double lacunarity) {
        this.lacunarity = lacunarity;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public void setNumOctaves(int octaves){
        if(this.octaves == octaves) return;

        this.octaves = octaves;
        createSamplers();
    }
}
