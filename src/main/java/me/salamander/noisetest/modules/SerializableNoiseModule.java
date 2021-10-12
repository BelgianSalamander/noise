package me.salamander.noisetest.modules;

import io.github.antiquitymc.nbt.CompoundTag;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;

public interface SerializableNoiseModule extends NoiseModule {


    static float safeSample(SerializableNoiseModule module, float x, float y){
        if(module != null){
            return module.sample(x,y);
        }else{
            return 0.0f;
        }
    }

    Collection<SerializableNoiseModule> getSources();
    void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup);
    void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup);
    String getNodeRegistryName();
}
