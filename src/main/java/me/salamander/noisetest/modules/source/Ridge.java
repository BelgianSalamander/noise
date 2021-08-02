package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.noise.PerlinNoise2D;

import java.util.Random;

public class Ridge implements NoiseModule {
    private final long seed;
    private final PerlinNoise2D[] perlinSamplers;
    private final int octaves;
    private double frequency = 1.0;
    private double persistence = 0.5;
    private double lacunarity = 2.0;

    public Ridge(){
        this.seed = (new Random()).nextLong();
        this.octaves = 6;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers();
    }

    public Ridge(int octaves){
        this.seed = (new Random()).nextLong();
        this.octaves = octaves;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers();
    }

    public Ridge(int octaves, long seed){
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

        final double offset = 1.0;
        final double gain = 2.0;

        double weight = 1.0;

        for(int i = 0; i < octaves; i++) {
            double signal =  perlinSamplers[i].sample(x * frequency * Math.pow(lacunarity, i), y * frequency * Math.pow(lacunarity, i));

            signal = offset - Math.abs(signal);
            signal *= signal * weight;

            weight = signal * gain;

            total += signal * Math.pow(persistence, i);
        }

        return Math.min(1, total / (octaves));
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
}
