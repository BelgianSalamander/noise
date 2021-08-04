package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;

import java.util.Random;

public class Incoherent implements NoiseModule {
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
}
