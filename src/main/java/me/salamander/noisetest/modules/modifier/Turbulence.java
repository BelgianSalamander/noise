package me.salamander.noisetest.modules.modifier;

import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.source.Perlin;

import java.util.Random;

public class Turbulence implements NoiseModule {
    private final Perlin xTurbulence, yTurbulence;
    private NoiseModule source;
    private double turbulencePower = 1.0;

    public Turbulence(NoiseModule source){
        this(source, (new Random()).nextLong());
        this.source = source;
    }

    public Turbulence(NoiseModule source, long seed){
        xTurbulence = new Perlin(3, seed + 3);
        yTurbulence = new Perlin(3, seed * 4723537 ^ 4264);
        this.source = source;
    }

    public Turbulence(NoiseModule source, double turbulencePower){
        this(source);
        this.turbulencePower = turbulencePower;
    }

    public Turbulence(NoiseModule source, long seed, double turbulencePower){
        this(source, seed);
        this.turbulencePower = turbulencePower;
    }

    @Override
    public double sample(double x, double y) {
        final double x0 = x + (12148.0 / 65536.0);
        final double y0 = y + (56346.0 / 65536.0);
        final double x1 = x + (23436.0 / 65536.0);
        final double y1 = y + (43765.0 / 65536.0);

        final double distortedX = x + xTurbulence.sample(x0, y0) * turbulencePower;
        final double distortedY = y + yTurbulence.sample(x1, y1) * turbulencePower;

        return source.sample(distortedX, distortedY);
    }

    public void setTurbulencePower(double turbulencePower) {
        this.turbulencePower = turbulencePower;
    }

    public void setSource(NoiseModule source) {
        this.source = source;
    }

    public void setFrequency(double frequency){
        xTurbulence.setFrequency(frequency);
        yTurbulence.setFrequency(frequency);
    }

    public void setPersistence(double persistence){
        xTurbulence.setPersistence(persistence);
        yTurbulence.setPersistence(persistence);
    }

    public void setLacunarity(double lacunarity){
        xTurbulence.setLacunarity(lacunarity);
        yTurbulence.setLacunarity(lacunarity);
    }
}
