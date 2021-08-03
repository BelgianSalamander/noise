package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.Parameter;
import me.salamander.noisetest.noise.PerlinNoise2D;

import java.util.Random;

public class Ridge implements NoiseModule {
    private final long seed;
    private PerlinNoise2D[] perlinSamplers;
    private int octaves;
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

    public void setNumOctaves(int octaves){
        if(this.octaves == octaves) return;

        this.octaves = octaves;

        createSamplers();
    }

    @Override
    public int getNumInputs() {
        return 0;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        throw new IllegalStateException("Tried to set input of source module!");
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
                throw new IllegalArgumentException("Index out of bounds");
        }
    }

    @Override
    public double getParameter(int index) {
        return 0;
    }

    @Override
    public String getName() {
        return "Ridge";
    }

    @Override
    public String[] inputNames() {
        return new String[0];
    }

    private static final Parameter[] parameters = new Parameter[]{
            new Parameter(0,"Octaves", 1.f, 10.f),
            new Parameter(1, "Frequency", 0.1f, 5.f),
            new Parameter(2, "Persistence", 0.05f, 0.95f),
            new Parameter(4, "Lacunarity", 1.0f, 5.0f)
    };

    @Override
    public Parameter[] parameters() {
        return parameters;
    }

}