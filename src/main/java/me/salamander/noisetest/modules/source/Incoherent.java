package me.salamander.noisetest.modules.source;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.types.SourceModule;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;

public class Incoherent extends SourceModule {
    private final Random random = new Random();

    private long seed;

    public Incoherent(){
        seed = (new Random()).nextLong();
    }
    public Incoherent(long seed){this.seed = seed;}

    @Override
    public double sample(double x, double y) {
        random.setSeed((long) (x * 357621L + y * 562457L + seed * 625627L));
        return random.nextDouble() * 2 - 1;
    }

    @Override
    public void setSeed(long s) {
        this.seed = seed;
    }

    @Override
    public void setParameter(int index, double value) {
        throw new IllegalArgumentException("No Parameters!");
    }

    @Override
    public double getParameter(int index) {
        throw new IllegalArgumentException("No Parameters!");
    }

    @Override
    public void readNBT(CompoundTag tag, List<NoiseModule> sourceLookup) {
        seed = tag.getLong("seed");
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<NoiseModule, Integer> indexLookup) {
        tag.putLong("seed", seed);
    }

    @Override
    public String getNodeRegistryName() {
        return "Incoherent";
    }
}
