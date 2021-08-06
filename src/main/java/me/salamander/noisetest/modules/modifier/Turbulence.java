package me.salamander.noisetest.modules.modifier;

import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.source.Perlin;
import me.salamander.noisetest.modules.types.ModifierModule;

import java.util.Random;

public class Turbulence extends ModifierModule {
    private final Perlin xTurbulence, yTurbulence;
    private double turbulencePower = 1.0;

    private static final int TURBULENCE_POWER_INDEX = 0, TURBULENCE_FREQUENCY_INDEX = 1;

    public Turbulence(NoiseModule source){
        this(source, (new Random()).nextLong());
        this.source = source;
    }

    public Turbulence(NoiseModule source, long seed){
        super(2);
        initParameters();
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

    private void initParameters(){
        parameters[TURBULENCE_POWER_INDEX] = 1.0;
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

    public void setFrequency(double frequency){
        xTurbulence.setFrequency(frequency);
        yTurbulence.setFrequency(frequency);
    }

    @Override
    public void setParameter(int index, double value) {
        if(index == TURBULENCE_FREQUENCY_INDEX) setFrequency(value);
        else super.setParameter(index, value);
    }

    @Override
    public double getParameter(int index) {
        if(index == TURBULENCE_FREQUENCY_INDEX) return xTurbulence.getFrequency();
        else return super.getParameter(index);
    }
}
