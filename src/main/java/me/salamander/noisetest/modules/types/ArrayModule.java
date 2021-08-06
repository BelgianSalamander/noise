package me.salamander.noisetest.modules.types;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

public abstract class ArrayModule implements GUIModule {
    protected final NoiseModule[] inputs;
    protected final double[] parameters;

    protected final int numInputs, numParameters;

    //Creates the arrays that hold the inputs and parameters
    protected ArrayModule(int numInputs, int numParameters){
        inputs = new NoiseModule[numInputs];
        parameters = new double[numParameters];

        this.numInputs = numInputs;
        this.numParameters = numParameters;
    }

    @Override
    public int numInputs() {
        return numInputs;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        inputs[index] = module;
    }

    @Override
    public void setParameter(int index, double value) {
        parameters[index] = value;
    }

    @Override
    public double getParameter(int index) {
        return parameters[index];
    }

    @Override
    public abstract double sample(double x, double y);

    @Override
    public abstract void setSeed(long s);
}
