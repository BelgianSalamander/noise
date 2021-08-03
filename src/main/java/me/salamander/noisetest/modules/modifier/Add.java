package me.salamander.noisetest.modules.modifier;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.Parameter;

public class Add implements NoiseModule {
    private NoiseModule noiseOne, noiseTwo;

    public Add(NoiseModule noiseOne, NoiseModule noiseTwo) {
        this.noiseOne = noiseOne;
        this.noiseTwo = noiseTwo;
    }

    @Override
    public double sample(double x, double y) {
        return noiseOne.sample(x, y) + noiseTwo.sample(x, y);
    }

    public void setNoiseOne(NoiseModule noiseOne) {
        this.noiseOne = noiseOne;
    }

    public void setNoiseTwo(NoiseModule noiseTwo) {
        this.noiseTwo = noiseTwo;
    }

    @Override
    public int getNumInputs() {
        return 2;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        switch (index){
            case 0:
                noiseOne = module;
                break;
            case 1:
                noiseTwo = module;
                break;
            default:
                throw new IllegalArgumentException("Index '" + index + "' out of bounds for module with two inputs!");
        }
    }

    @Override
    public void setParameter(int index, double value) {
        throw new IllegalStateException("Set parameter should not be called on module with no parameters (Add)");
    }

    @Override
    public double getParameter(int index) {
        throw new IllegalStateException("Get parameter should not be called on module with no parameters (Add)");
    }

    @Override
    public String getName() {
        return "Add";
    }

    @Override
    public String[] inputNames() {
        return inputs;
    }

    @Override
    public Parameter[] parameters() {
        return new Parameter[0];
    }

    private static String[] inputs = new String[]{"Module One", "Module Two"};
}