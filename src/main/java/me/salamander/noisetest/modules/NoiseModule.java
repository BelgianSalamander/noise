package me.salamander.noisetest.modules;

import me.salamander.noisetest.modules.source.Const;

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
}
