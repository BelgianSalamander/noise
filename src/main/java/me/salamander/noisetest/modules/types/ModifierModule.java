package me.salamander.noisetest.modules.types;

import io.github.antiquitymc.nbt.CompoundTag;
import io.github.antiquitymc.nbt.IntArrayTag;
import io.github.antiquitymc.nbt.LongArrayTag;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.SerializableNoiseModule;

import java.util.*;

public abstract class ModifierModule implements GUIModule {
    protected SerializableNoiseModule source;
    protected final float[] parameters;

    protected ModifierModule(int numParameters){
        parameters = new float[numParameters];
    }

    @Override
    public int numInputs() {
        return 1;
    }

    @Override
    public void setInput(int index, SerializableNoiseModule module) {
        if(index == 0){
            source = module;
        }else{
            throw new IllegalArgumentException("Index out of bounds for modifier with a single input");
        }
    }

    @Override
    public SerializableNoiseModule getInput(int index) {
        if(index == 0){
            return source;
        }else{
            throw new IllegalArgumentException("Index out of bounds for modifier with a single input");
        }
    }

    @Override
    public void setParameter(int index, float value) {
        parameters[index] = value;
    }

    @Override
    public float getParameter(int index) {
        return parameters[index];
    }

    @Override //Should set the seed of the source
    public void setSeed(long s){source.setSeed(s * 46237 ^ 42534);};

    @Override
    public Collection<SerializableNoiseModule> getSources() {
        Collection<SerializableNoiseModule> sources = new ArrayList<>();
        sources.add(source);
        return sources;
    }

    @Override
    public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
        if(tag.containsKey("source")){
            source = sourceLookup.get(tag.getInt("source"));
        }

        Float[] new_params = Arrays.stream(((IntArrayTag) tag.get("parameters")).getValue()).mapToObj(Float::intBitsToFloat).toArray(Float[]::new);
        for(int i = 0; i < new_params.length; i++){
            setParameter(i, new_params[i]);
        }
	}

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        if(source != null){
            tag.putInt("source", indexLookup.get(source));
        }

        int[] ints = new int[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            ints[i] = Float.floatToIntBits(parameters[i]);
        }
        tag.put("parameters", new IntArrayTag(ints));
    }
}
