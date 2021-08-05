package me.salamander.noisetest.modules.combiner;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

public class Multiply implements GUIModule {
    private NoiseModule moduleOne, moduleTwo;

    public Multiply(NoiseModule moduleOne, NoiseModule moduleTwo) {
        this.moduleOne = moduleOne;
        this.moduleTwo = moduleTwo;
    }

    @Override
    public double sample(double x, double y) {
        return NoiseModule.safeSample(moduleOne, x, y) * NoiseModule.safeSample(moduleTwo, x, y);
    }

    public void setModuleOne(NoiseModule moduleOne) {
        this.moduleOne = moduleOne;
    }

    public void setModuleTwo(NoiseModule moduleTwo) {
        this.moduleTwo = moduleTwo;
    }

    @Override
    public void setSeed(long s) {
        if(moduleOne != null) moduleOne.setSeed(s);
        if(moduleTwo != null) moduleTwo.setSeed(s);
    }

    @Override
    public int numInputs() {
        return 2;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        if(index == 0){
            moduleOne = module;
        }else if(index == 1){
            moduleTwo = module;
        }else{
            throw new IllegalArgumentException("Index '" + index + "' is out of bounds for module with two inputs");
        }
    }

    @Override
    public void setParameter(int index, double value) {
        throw new IllegalArgumentException("Index out of bounds for module with zero parameters :)");
    }

    @Override
    public double getParameter(int index) {
        throw new IllegalArgumentException("Index out of bounds for module with zero parameters :)");
    }
}
