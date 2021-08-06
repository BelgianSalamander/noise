package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.types.ArraySourceModule;
import me.salamander.noisetest.noise.PerlinNoise2D;

import java.util.Random;

public class Perlin extends ArraySourceModule {
    private long seed;
    private PerlinNoise2D[] perlinSamplers = null;

    public Perlin(){
        super(4);

        initParameters();

        this.seed = (new Random()).nextLong();

        createSamplers(true);
    }
    public Perlin(int octaves){
        super(4);
        this.seed = (new Random()).nextLong();
        initParameters();
        parameters[0] = octaves;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers(true);
    }
    public Perlin(int octaves, long seed){
        super(4);
        this.seed = seed;
        initParameters();
        parameters[0] = octaves;
        perlinSamplers = new PerlinNoise2D[octaves];

        createSamplers(true);
    }
    private void createSamplers(boolean regenerate){
        int octaves = (int) parameters[0];
        PerlinNoise2D[] newSamplers = new PerlinNoise2D[octaves];
        Random random = new Random(seed);

        for(int i = 0; i < octaves; i++){
            boolean usedPrevious = false;
            if(perlinSamplers != null && !regenerate) {
                if (i < perlinSamplers.length) {
                    newSamplers[i] = perlinSamplers[i];
                    usedPrevious = true;
                    random.nextLong();
                }
            }
            if(!usedPrevious){
                newSamplers[i] = new PerlinNoise2D(random.nextLong());
            }
        }

        perlinSamplers = newSamplers;
    }

    private void initParameters(){
        parameters[0] = 6;
        parameters[1] = 1.0;
        parameters[2] = 0.5;
        parameters[3] = 2.0;
    }

    @Override
    public double sample(double x, double y) {
        int octaves = (int) parameters[0];
        double frequency = parameters[1];
        double persistence = parameters[2];
        double lacunarity = parameters[3];

        double total = 0;
        for(int i = 0; i < octaves; i++)
            total += Math.pow(persistence, i) * perlinSamplers[i].sample(x * frequency * Math.pow(lacunarity, i), y * frequency * Math.pow(lacunarity, i));
        return total;
    }

    @Override
    public void setSeed(long s) {
        this.seed = s;

        createSamplers(true);
    }

    public void setPersistence(double persistence) {
        parameters[2] = persistence;
    }
    public void setLacunarity(double lacunarity) {
        parameters[3] = lacunarity;
    }
    public void setFrequency(double frequency) {
        parameters[1] = frequency;
    }
    public void setNumOctaves(int octaves){
        if(((int) parameters[0]) == octaves) return;

        parameters[0] = octaves;
        createSamplers(false);
    }

    public double getFrequency() {
        return parameters[1];
    }

    @Override
    public void setParameter(int index, double value) {
        if(index == 0) setNumOctaves((int) value);
        else super.setParameter(index, value);
    }
}
