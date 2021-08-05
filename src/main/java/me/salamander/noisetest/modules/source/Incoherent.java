package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;

import java.util.Random;

public class Incoherent implements GUIModule {
    private final Random random = new Random();

    private long seed;

    public Incoherent(){seed = (new Random()).nextLong();}
    public Incoherent(long seed){this.seed = seed;}

    @Override
    public double sample(double x, double y) {
        random.setSeed((long) (x * 352735782578L + y * 56276574645L + seed * 625627L));
        return random.nextDouble() * 2 - 1;
    }

    @Override
    public void setSeed(long s) {
        this.seed = seed;
    }

    @Override
    public int numInputs() {
        return 0;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        throw new IllegalArgumentException("No Inputs!");
    }

    @Override
    public void setParameter(int index, double value) {
        throw new IllegalArgumentException("No Parameters!");
    }

    @Override
    public double getParameter(int index) {
        throw new IllegalArgumentException("No Parameters!");
    }
}
