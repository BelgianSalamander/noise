package me.salamander.noisetest.modules.source;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.FunctionRegistry;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.types.SourceModule;
import me.salamander.noisetest.noise.PerlinNoise2D;

import java.util.*;

public class Ridge extends SourceModule implements GLSLCompilable {
    private long seed;
    private PerlinNoise2D[] perlinSamplers;
    private int octaves;
    private float frequency = 1.0f;
    private float persistence = 0.5f;
    private float lacunarity = 2.0f;

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
            perlinSamplers[i] = new PerlinNoise2D(random.nextInt());
        }
    }

    @Override
    public float sample(float x, float y) {
        float total = 0;

        final float offset = 1.0f;
        final float gain = 2.0f;

        float weight = 1.0f;

        for(int i = 0; i < octaves; i++) {
            float signal =  perlinSamplers[i].sample((float)(x * frequency * Math.pow(lacunarity, i)), (float) (y * frequency * Math.pow(lacunarity, i)));

            signal = offset - Math.abs(signal);
            signal *= signal * weight;

            weight = signal * gain;
            if(weight > 1.0) weight = 1.0f;
            else if(weight < 0.0) weight = 0.0f;

            total += signal * Math.pow(persistence, i);
        }

        return total * 1.25f - 1.0f;
    }

    public void setPersistence(float persistence) {
        this.persistence = persistence;
    }

    public void setLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
    }

    public void setFrequency(float frequency) {
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
    public void setParameter(int index, float value) {
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
    public float getParameter(int index) {
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
        frequency = tag.getFloat("frequency");
        persistence = tag.getFloat("persistence");
        lacunarity = tag.getFloat("lacunarity");
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        tag.putLong("seed", seed);
        tag.putInt("octaves", octaves);
        tag.putFloat("frequency", frequency);
        tag.putFloat("persistence", persistence);
        tag.putFloat("lacunarity", lacunarity);
    }

    @Override
    public String getNodeRegistryName() {
        return "Ridge";
    }

    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return "sampleRidge(" + vec2Name + ", " + seedName + ", " + frequency + ", " + persistence + ", " + lacunarity + ", " + octaves + ")";
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        return required;
    }

    private static final Set<FunctionInfo> required = new HashSet<>();
    static{
        required.add(FunctionRegistry.getFunction("sampleRidge"));
    }

    @Override
    public long getSeed() {
        return seed;
    }
}
