package me.salamander.noisetest.modules.source;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.types.SourceModule;

import java.util.IdentityHashMap;
import java.util.List;

public class CheckerBoard extends SourceModule {
    private double frequency = 1.0;

    public CheckerBoard(){}
    public CheckerBoard(double frequency){this.frequency = frequency;}

    @Override
    public double sample(double x, double y) {
        return (floor(x * frequency) + floor(y * frequency)) % 2 < 1 ? -1 : 1;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    private int floor(double n){
        int value = (int) n;
        return value < n ? value : value - 1;
    }

    @Override
    public void setSeed(long s) {}

    @Override
    public void setParameter(int index, double value) {
        if(index == 0){
            frequency = value;
        }else {
            throw new IllegalArgumentException("Index '" + index + "' is out of bounds!");
        }
    }

    @Override
    public double getParameter(int index) {
        if(index == 0){
            return frequency;
        }
        throw new IllegalArgumentException("Index '" +index + "' is out of bounds!");
    }

    @Override
    public void readNBT(CompoundTag tag, List<NoiseModule> sourceLookup) {
        frequency = tag.getDouble("frequency");
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<NoiseModule, Integer> indexLookup) {
        tag.putDouble("frequency", frequency);
    }

    @Override
    public String getNodeRegistryName() {
        return "Checkerboard";
    }
}
