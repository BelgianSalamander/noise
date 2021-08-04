package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

public class Const implements GUIModule {
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

    @Override
    public void setSeed(long s) { }


    @Override
    public int numInputs() {
        return 0;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        throw new IllegalArgumentException("Index out of bounds for module with no inputs");
    }

    @Override
    public void setParameter(int index, double value) {
        if(index == 0){
            this.value = value;
        }else{
            throw new IllegalArgumentException("Index '" + index + "' out of bounds for module with one parameter");
        }
    }

    @Override
    public double getParameter(int index) {
        if(index == 0){
            return value;
        }else{
            throw new IllegalArgumentException("Index '" + index + "' out of bounds for module with one parameter");
        }
    }
}
