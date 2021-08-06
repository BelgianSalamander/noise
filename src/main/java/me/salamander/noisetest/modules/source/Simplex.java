package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.noise.PerlinNoise2D;
import me.salamander.noisetest.noise.SimplexNoise2D;

import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongFunction;

public class Simplex implements GUIModule {
    private long seed;
    private DoubleBinaryOperator[] perlinSamplers;
    private int octaves;
    private double frequency = 1.0;
    private double persistence = 0.5;
    private double lacunarity = 2.0;
    private final LongFunction<DoubleBinaryOperator> noiseConstructor;

    public Simplex() {
        this(6);
    }

	public Simplex(int octaves){
    	this(octaves, new Random().nextLong());
	}

	public Simplex(int octaves, long seed){
    	this(octaves, seed, l -> new SimplexNoise2D(l)::sample);
	}

    protected Simplex(int octaves, long seed, LongFunction<DoubleBinaryOperator> noiseConstructor) {
	    this.seed = seed;
	    this.octaves = 6;
	    perlinSamplers = new DoubleBinaryOperator[octaves];
	    this.noiseConstructor = noiseConstructor;

	    createSamplers(true, noiseConstructor);
    }

    private void createSamplers(boolean regenerate, LongFunction<DoubleBinaryOperator> noiseConstructor) {
        DoubleBinaryOperator[] newSamplers = new DoubleBinaryOperator[octaves];
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

            if(!usedPrevious) {
                newSamplers[i] = noiseConstructor.apply(random.nextLong());
            }
        }

        perlinSamplers = newSamplers;
    }

    @Override
    public double sample(double x, double y) {
        double total = 0;
        for(int i = 0; i < octaves; i++)
            total += Math.pow(persistence, i) * perlinSamplers[i].applyAsDouble(x * frequency * Math.pow(lacunarity, i), y * frequency * Math.pow(lacunarity, i));
        return total;
    }

    @Override
    public void setSeed(long s) {
        this.seed = s;
        createSamplers(true, this.noiseConstructor);
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
        createSamplers(false, this.noiseConstructor);
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

    public double getFrequency() {
        return frequency;
    }
}
