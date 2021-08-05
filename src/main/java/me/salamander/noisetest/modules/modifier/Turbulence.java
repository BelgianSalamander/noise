package me.salamander.noisetest.modules.modifier;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.source.Perlin;

import java.util.Random;

public class Turbulence implements GUIModule {
    private final Perlin xTurbulence, yTurbulence;
    private NoiseModule source;
    private double turbulencePower = 1.0;

    private static final String[] inputNames = new String[]{"Source"};

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
        if(source == null) return 0;

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

    @Override
    public void setSeed(long s) {
        if(source != null) source.setSeed(s);
    }


    @Override
    public int numInputs() {
        return 1;
    }

    @Override
    public void setInput(int index, NoiseModule module) {
        if(index == 0){
            source = module;
        }else{
            throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public void setParameter(int index, double value) {
        switch (index){
            case 0:
                turbulencePower = value;
                break;
            case 1:
                setFrequency(value);
                break;
            default:
                throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public double getParameter(int index) {
        if(index == 0){
            return turbulencePower;
        }else if(index == 1){
            return xTurbulence.getFrequency();
        }else{
            throw new IllegalArgumentException("Index out of bounds!");
        }
    }
}
