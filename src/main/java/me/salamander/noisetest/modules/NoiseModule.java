package me.salamander.noisetest.modules;

import me.salamander.noisetest.modules.combiner.Add;
import me.salamander.noisetest.modules.combiner.Multiply;
import me.salamander.noisetest.modules.source.Const;

public interface NoiseModule {
    double sample(double x, double y);

    void setSeed(long s);

    default NoiseModule multiply(double n){
        return new Multiply(this, new Const(n));
    }
    default NoiseModule add(double n){
        return new Add(this, new Const(n));
    }

    static double safeSample(NoiseModule module, double x, double y){
        if(module != null){
            return module.sample(x,y);
        }else{
            return 0.0;
        }
    }
}
