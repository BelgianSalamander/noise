package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.Parameter;

import java.util.Random;

public class Incoherent implements NoiseModule {
    private final Random random = new Random();

    @Override
    public double sample(double x, double y) {
        random.setSeed((long) (x * 352735782578L + y * 56276574645L));
        return random.nextDouble() * 2 - 1;
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
        throw new IllegalStateException("Tried to set parameter of module with no parameters!");
    }

    @Override
    public double getParameter(int index) {
        throw new IllegalStateException("Tried to get parameter of module with no parameters!");
    }

    @Override
    public String getName() {
        return "Noise";
    }

    @Override
    public String[] inputNames() {
        return new String[0];
    }

    @Override
    public Parameter[] parameters() {
        return new Parameter[0];
    }


}
