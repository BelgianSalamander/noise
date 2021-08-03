package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.Parameter;

public class Const implements NoiseModule {
    private double value = 0;

    private static final Parameter[] parameters = new Parameter[]{new Parameter(0, "Value", -1.0, 1.0)};

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
            this.value = value;
        }
        throw new IllegalArgumentException("Index is out of bounds!");
    }

    @Override
    public double getParameter(int index) {
        if(index == 0){
            return this.value;
        }
        throw new IllegalArgumentException("Index is out of bounds!");
    }

    @Override
    public String getName() {
        return "Constant";
    }

    @Override
    public String[] inputNames() {
        return new String[0];
    }

    @Override
    public Parameter[] parameters() {
        return parameters;
    }


}
