package me.salamander.noisetest.modules;

import me.salamander.noisetest.modules.modifier.Add;
import me.salamander.noisetest.modules.modifier.Multiply;
import me.salamander.noisetest.modules.source.Const;

public interface NoiseModule {
    double sample(double x, double y);

    default NoiseModule multiply(double n){
        return new Multiply(this, new Const(n));
    }

    default NoiseModule add(double n){
        return new Add(this, new Const(n));
    }
}
