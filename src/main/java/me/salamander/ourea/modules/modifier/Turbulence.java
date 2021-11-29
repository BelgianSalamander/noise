package me.salamander.ourea.modules.modifier;

import me.salamander.ourea.modules.NoiseSampler;

public class Turbulence implements NoiseSampler {
    private NoiseSampler turbulenceX, turbulenceY, turbulenceZ;
    private NoiseSampler source;
    private float power;

    public Turbulence(NoiseSampler turbulenceX, NoiseSampler turbulenceY, NoiseSampler source) {
        this.turbulenceX = turbulenceX;
        this.turbulenceY = turbulenceY;
        this.source = source;
    }

    public Turbulence(NoiseSampler turbulenceX, NoiseSampler turbulenceY, NoiseSampler turbulenceZ, NoiseSampler source) {
        this.turbulenceX = turbulenceX;
        this.turbulenceY = turbulenceY;
        this.turbulenceZ = turbulenceZ;
        this.source = source;
    }

    @Override
    public void setSalt(long salt) {

    }

    @Override
    public void setFrequency(float frequency) {

    }

    @Override
    public float sample(float x, float y, long seed) {
        final float x0 = x + (12148.0f / 65536.0f);
        final float y0 = y + (56346.0f / 65536.0f);
        final float x1 = x + (23436.0f / 65536.0f);
        final float y1 = y + (43765.0f / 65536.0f);

        float sampleX = turbulenceX.sample(x0, y0, seed);
        float sampleY = turbulenceY.sample(x1, y1, seed);

        return source.sample(x + sampleX * power, y + sampleY * power, seed);
    }

    @Override
    public float sample(float x, float y, float z, long seed) {
        final float x0 = x + (12148.0f / 65536.0f);
        final float y0 = y + (56346.0f / 65536.0f);
        final float z0 = z + (81919.0f / 65536.0f);
        final float x1 = x + (23436.0f / 65536.0f);
        final float y1 = y + (43765.0f / 65536.0f);
        final float z1 = z + (71811.0f / 65536.0f);
        final float x2 = x + (92765.0f / 65536.0f);
        final float y2 = y + (11701.0f / 65536.0f);
        final float z2 = z + (4295.0f / 65536.0f);

        float sampleX = turbulenceX.sample(x0, y0, z0, seed);
        float sampleY = turbulenceY.sample(x1, y1, z1, seed);
        float sampleZ = turbulenceZ.sample(x2, y2, z2, seed);

        return source.sample(x + sampleX * power, y + sampleY * power, z + sampleZ * power, seed);
    }

    public void setPower(float power) {
        this.power = power;
    }
}
