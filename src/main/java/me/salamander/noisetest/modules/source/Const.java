package me.salamander.noisetest.modules.source;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.types.SourceModule;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class Const extends SourceModule implements GLSLCompilable {
    private float value = 0;

    public Const(){}
    public Const(float value){this.value = value;}

    @Override
    public float sample(float x, float y) {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void setSeed(long s) { }

    @Override
    public void setParameter(int index, float value) {
        if(index == 0){
            this.value = value;
        }else{
            throw new IllegalArgumentException("Index '" + index + "' out of bounds for module with one parameter");
        }
    }

    @Override
    public float getParameter(int index) {
        if(index == 0){
            return value;
        }else{
            throw new IllegalArgumentException("Index '" + index + "' out of bounds for module with one parameter");
        }
    }

    @Override
    public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
        value = tag.getFloat("value");
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        tag.putFloat("value", value);
    }

    @Override
    public String getNodeRegistryName() {
        return "Const";
    }

    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return String.valueOf(value);
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        return new HashSet<>(0);
    }

    @Override
    public long getSeed() {
        return 0;
    }
}
