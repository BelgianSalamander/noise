package me.salamander.noisetest.modules.modifier;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.Parameter;

public class Min implements NoiseModule {
    private NoiseModule moduleOne, moduleTwo;

    public Min(NoiseModule moduleOne, NoiseModule moduleTwo) {
        this.moduleOne = moduleOne;
        this.moduleTwo = moduleTwo;
    }

    @Override
    public double sample(double x, double y) {
        return Math.min(moduleOne.sample(x, y), moduleTwo.sample(x, y));
    }

    public void setModuleOne(NoiseModule moduleOne) {
        this.moduleOne = moduleOne;
    }

    public void setModuleTwo(NoiseModule moduleTwo) {
        this.moduleTwo = moduleTwo;
    }

    @Override
    public int getNumInputs() {
        return 2;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        switch (index){
            case 0:
                moduleOne = module;
                break;
            case 1:
                moduleTwo = module;
                break;
            default:
                throw new IllegalArgumentException("Index '" + index + "' out of bounds for module with two inputs!");
        }
    }

    @Override
    public void setParameter(int index, double value) {
        throw new IllegalStateException("Set parameter should not be called on module with no parameters (Min)");
    }

    @Override
    public double getParameter(int index) {
        throw new IllegalStateException("Get parameter should not be called on module with no parameters (Min)");
    }

    @Override
    public String getName() {
        return "Min";
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
