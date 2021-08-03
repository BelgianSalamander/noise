package me.salamander.noisetest.modules.source;

import me.salamander.noisetest.modules.NoiseModule;

import java.util.Random;

public class Incoherent implements NoiseModule {
    private final Random random = new Random();

    @Override
    public double sample(double x, double y) {
        random.setSeed((long) (x * 352735782578L + y * 56276574645L));
        return random.nextDouble() * 2 - 1;
    }
}
