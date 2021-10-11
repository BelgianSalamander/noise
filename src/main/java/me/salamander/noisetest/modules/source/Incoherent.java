package me.salamander.noisetest.modules.source;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.types.SourceModule;

import java.util.*;

public class Incoherent extends SourceModule implements GLSLCompilable {
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
    public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
        seed = tag.getLong("seed");
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        tag.putLong("seed", seed);
    }

    @Override
    public String getNodeRegistryName() {
        return "Incoherent";
    }

    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return "(((" + seedName + " ^ (floatBitsToInt((" + vec2Name + ").x) * 516087743) ^ (floatBitsToInt((" + vec2Name + ").y) * 1462165261)) * 1483279183 & 0xffff) / float(0xffff) * 2 - 1)";
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        return new HashSet<>();
    }

    @Override
    public long getSeed() {
        return seed;
    }
}
