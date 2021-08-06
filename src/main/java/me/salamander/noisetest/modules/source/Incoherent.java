package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.types.SourceModule;

import java.util.Random;

public class Incoherent extends SourceModule {
    private final Random random = new Random();

    private long seed;

    public Incoherent(){
        seed = (new Random()).nextLong();
    }
    public Incoherent(long seed){this.seed = seed;}

    @Override
    public double sample(double x, double y) {
        random.setSeed((long) (x * 352735782573L + y * 56276574645L + seed * 625627L));
        return random.nextDouble() * 2 - 1;
    }

    @Override
    public void setSeed(long s) {
        this.seed = seed;
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
