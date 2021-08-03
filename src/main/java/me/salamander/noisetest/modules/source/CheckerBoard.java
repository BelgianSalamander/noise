package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.Parameter;

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

    @Override
    public int getNumInputs() {
        return 0;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        throw new IllegalStateException("Tried to set input of source module!");
    }

    @Override
    public void setParameter(int index, double value) {

        if(index == 0){
            frequency = value;
        }
        throw new IllegalArgumentException("Index is out of bounds!");
    }

    @Override
    public double getParameter(int index) {
        if(index == 0){
            return frequency;
        }
        throw new IllegalArgumentException("Index is out of bounds!");
    }

    @Override
    public String getName() {
        return "Checkerboard";
    }

    @Override
    public String[] inputNames() {
        return new String[0];
    }

    @Override
    public Parameter[] parameters() {
        return parameters;
    }

    private static final Parameter[] parameters = new Parameter[]{
            new Parameter(0, "Frequency", 0.1, 5.0)
    };
}
