package me.salamander.noisetest.modules.source;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.types.SourceModule;
import me.salamander.noisetest.noise.PerlinNoise2D;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;

public class Ridge extends SourceModule {
    private long seed;
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
            if(weight > 1.0) weight = 1.0;
            else if(weight < 0.0) weight = 0.0;

            total += signal * Math.pow(persistence, i);
        }

        return total * 1.25 - 1.0;
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
    public void setSeed(long s) {
        this.seed = s;

        createSamplers();
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

    @Override
    public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
        seed = tag.getLong("seed");
        octaves = tag.getInt("octaves");
        frequency = tag.getDouble("frequency");
        persistence = tag.getDouble("persistence");
        lacunarity = tag.getDouble("lacunarity");
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        tag.putLong("seed", seed);
        tag.putInt("octaves", octaves);
        tag.putDouble("frequency", frequency);
        tag.putDouble("persistence", persistence);
        tag.putDouble("lacunarity", lacunarity);
    }

    @Override
    public String getNodeRegistryName() {
        return "Ridge";
    }
}
