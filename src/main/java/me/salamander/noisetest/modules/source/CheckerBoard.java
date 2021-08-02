package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;

public class CheckerBoard implements NoiseModule {
    private double frequency = 1.0;

    public CheckerBoard(){}
    public CheckerBoard(double frequency){this.frequency = frequency;}

    @Override
    public double sample(double x, double y) {
        return (floor(x) + floor(y)) % (2 * frequency) < frequency ? -1 : 1;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    private int floor(double n){
        int value = (int) n;
        return value < n ? value : value - 1;
    }
}
