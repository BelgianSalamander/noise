package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

public class CheckerBoard implements GUIModule {
    private double frequency = 1.0;

    public CheckerBoard(){}
    public CheckerBoard(double frequency){this.frequency = frequency;}

    @Override
    public double sample(double x, double y) {
        return (floor(x * frequency) + floor(y * frequency)) % 2 < 1 ? -1 : 1;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    private int floor(double n){
        int value = (int) n;
        return value < n ? value : value - 1;
    }

    @Override
    public void setSeed(long s) {}


    @Override
    public int numInputs() {
        return 0;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        throw new IllegalArgumentException("Index is out of bounds!");
    }

    @Override
    public void setParameter(int index, double value) {
        if(index == 0){
            frequency = value;
        }else {
            throw new IllegalArgumentException("Index '" + index + "' is out of bounds!");
        }
    }

    @Override
    public double getParameter(int index) {
        if(index == 0){
            return frequency;
        }
        throw new IllegalArgumentException("Index '" +index + "' is out of bounds!");
    }
}
