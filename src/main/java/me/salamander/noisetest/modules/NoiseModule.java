package me.salamander.noisetest.modules;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.modules.source.Const;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public interface NoiseModule {
    double sample(double x, double y);
    void setSeed(long s);

    static double safeSample(NoiseModule module, double x, double y){
        if(module != null){
            return module.sample(x,y);
        }else{
            return 0.0;
        }
    }

    Collection<NoiseModule> getSources();
    void readNBT(CompoundTag tag, List<NoiseModule> sourceLookup);
    void writeNBT(CompoundTag tag, IdentityHashMap<NoiseModule, Integer> indexLookup);
    String getNodeRegistryName();
}
