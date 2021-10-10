package me.salamander.noisetest.modules.source;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.types.SourceModule;

import java.util.IdentityHashMap;
import java.util.List;

public class Const extends SourceModule {
    private double value = 0;

    public Const(){}
    public Const(double value){this.value = value;}

    @Override
    public double sample(double x, double y) {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void setSeed(long s) { }

    @Override
    public void setParameter(int index, double value) {
        if(index == 0){
            this.value = value;
        }else{
            throw new IllegalArgumentException("Index '" + index + "' out of bounds for module with one parameter");
        }
    }

    @Override
    public double getParameter(int index) {
        if(index == 0){
            return value;
        }else{
            throw new IllegalArgumentException("Index '" + index + "' out of bounds for module with one parameter");
        }
    }

    @Override
    public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
        value = tag.getDouble("value");
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        tag.putDouble("value", value);
    }

    @Override
    public String getNodeRegistryName() {
        return "Const";
    }
}
