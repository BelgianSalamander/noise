package me.salamander.noisetest.modules.combiner;

import me.salamander.noisetest.modules.NoiseModule;

public class Multiply implements NoiseModule {
    private NoiseModule moduleOne, moduleTwo;

    public Multiply(NoiseModule moduleOne, NoiseModule moduleTwo) {
        this.moduleOne = moduleOne;
        this.moduleTwo = moduleTwo;
    }

    @Override
    public double sample(double x, double y) {
        return moduleOne.sample(x, y) * moduleTwo.sample(x, y);
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
}
