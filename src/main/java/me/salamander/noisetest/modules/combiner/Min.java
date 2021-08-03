package me.salamander.noisetest.modules.combiner;

import me.salamander.noisetest.modules.NoiseModule;

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
}
