package me.salamander.noisetest.modules;

import me.salamander.noisetest.modules.modifier.Add;
import me.salamander.noisetest.modules.modifier.Multiply;
import me.salamander.noisetest.modules.source.Const;

public interface NoiseModule {
    double sample(double x, double y);
    int getNumInputs();
    void setInput(int index, NoiseModule module);

    void setParameter(int index, double value);
    double getParameter(int index);

    String getName();
    String[] inputNames();
    Parameter[] parameters();

    //Handles NullPointerExceptions
    default double safeSample(double x, double y){
        try{
            return sample(x, y);
        }catch(NullPointerException e){
            return 0;
        }
    }

    default NoiseModule multiply(double n){
        return new Multiply(this, new Const(n));
    }
    default NoiseModule add(double n){
        return new Add(this, new Const(n));
    }
}
