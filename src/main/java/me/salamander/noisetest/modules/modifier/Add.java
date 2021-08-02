package me.salamander.noisetest.modules.modifier;

import me.salamander.noisetest.modules.NoiseModule;

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
}
