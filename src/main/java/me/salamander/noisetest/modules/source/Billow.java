package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.noise.PerlinNoise2D;

import java.util.Random;

public class Billow implements GUIModule {
    private long seed;
    private PerlinNoise2D[] perlinSamplers;
    private int octaves;
    private double frequency = 1.0;
    private double persistence = 0.5;
    private double lacunarity = 2.0;

    public Billow(){
        this.seed = (new Random()).nextLong();
        this.octaves = 6;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers();
    }

    public Billow(int octaves){
        this.seed = (new Random()).nextLong();
        this.octaves = octaves;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers();
    }

    public Billow(int octaves, long seed){
        this.seed = seed;
        this.octaves = octaves;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers();
    }

    public void setNumOctaves(int octaves){
        if(this.octaves == octaves) return;

        this.octaves = octaves;

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
            total += Math.abs(Math.pow(persistence, i) * perlinSamplers[i].sample(x * frequency * Math.pow(lacunarity, i), y * frequency * Math.pow(lacunarity, i)));
        return total;
    }

    @Override
    public void setSeed(long s) {
        this.seed = s;

        createSamplers();
    }

    @Override
    public int numInputs() {
        return 0;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        throw new IllegalArgumentException("Index out of bounds for module with 0 inputs!");
    }

    @Override
    public void setParameter(int index, double value) {
        switch (index){
            case 0:
                setNumOctaves((int) value);
                break;
            case 1:
                frequency = value;
                break;
            case 2:
                persistence = value;
                break;
            case 3:
                lacunarity = value;
                break;
            default:
                throw new IllegalArgumentException("Index out of bounds for module with 4 parameters!");
        }
    }

    @Override
    public double getParameter(int index) {
        switch (index){
            case 0:
                return octaves;
            case 1:
                return frequency;
            case 2:
                return persistence;
            case 3:
                return lacunarity;
            default:
                throw new IllegalArgumentException("Index out of bounds for module with 4 parameters!");
        }
    }
}
