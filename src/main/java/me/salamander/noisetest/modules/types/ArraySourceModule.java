package me.salamander.noisetest.modules.types;

public abstract class ArraySourceModule extends SourceModule{
    protected final double[] parameters;

    public ArraySourceModule(int numParameters){
        parameters = new double[numParameters];
    }

    @Override
    public void setParameter(int index, double value) {
        parameters[index] = value;
    }

    @Override
    public double getParameter(int index) {
        return parameters[index];
    }
}
