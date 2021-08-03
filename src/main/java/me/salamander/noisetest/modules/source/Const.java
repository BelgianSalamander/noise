package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;

public class Const implements NoiseModule {
    private double value = 0;

    public Const(){}
    public Const(double value){this.value = value;}

    @Override
    public double sample(double x, double y) {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
