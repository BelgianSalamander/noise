package me.salamander.noisetest.modules.combiner;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

public class Add implements GUIModule {
    private NoiseModule noiseOne, noiseTwo;

    public Add(NoiseModule noiseOne, NoiseModule noiseTwo) {
        this.noiseOne = noiseOne;
        this.noiseTwo = noiseTwo;
    }

    @Override
    public double sample(double x, double y) {
        return NoiseModule.safeSample(noiseOne, x, y) + NoiseModule.safeSample(noiseTwo, x, y) ;
    }

    public void setNoiseOne(NoiseModule noiseOne) {
        this.noiseOne = noiseOne;
    }

    public void setNoiseTwo(NoiseModule noiseTwo) {
        this.noiseTwo = noiseTwo;
    }

    @Override
    public void setSeed(long s) {
        if(noiseOne != null) noiseOne.setSeed(s);
        if(noiseTwo != null) noiseTwo.setSeed(s);
    }

    @Override
    public int numInputs() {
        return 2;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        if(index == 0){
            noiseOne = module;
        }else if(index == 1){
            noiseTwo = module;
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
