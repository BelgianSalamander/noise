package me.salamander.noisetest.modules.source;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.FunctionRegistry;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.types.SourceModule;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class CheckerBoard extends SourceModule implements GLSLCompilable {
    private float frequency = 1.0f;

    public CheckerBoard(){}
    public CheckerBoard(float frequency){this.frequency = frequency;}

    @Override
    public float sample(float x, float y) {
        return (floor(x * frequency) + floor(y * frequency)) % 2 < 1 ? -1 : 1;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    private int floor(float n){
        int value = (int) n;
        return value < n ? value : value - 1;
    }

    @Override
    public void setSeed(long s) {}

    @Override
    public void setParameter(int index, float value) {
        if(index == 0){
            frequency = value;
        }else {
            throw new IllegalArgumentException("Index '" + index + "' is out of bounds!");
        }
    }

    @Override
    public float getParameter(int index) {
        if(index == 0){
            return frequency;
        }
        throw new IllegalArgumentException("Index '" +index + "' is out of bounds!");
    }

    @Override
    public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
        frequency = tag.getFloat("frequency");
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        tag.putDouble("frequency", frequency);
    }

    @Override
    public String getNodeRegistryName() {
        return "Checkerboard";
    }

    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return "checkerboard(" + vec2Name + ", " + frequency + ")";
    }

    private static final Set<FunctionInfo> requires = new HashSet<>();

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        return requires;
    }

    @Override
    public long getSeed() {
        return 0;
    }

    static {
        requires.add(FunctionRegistry.getFunction("checkerboard"));
    }
}
