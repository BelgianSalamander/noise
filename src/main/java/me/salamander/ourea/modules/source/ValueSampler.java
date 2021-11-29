package me.salamander.ourea.modules.source;

import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.util.MathHelper;

public class ValueSampler implements NoiseSampler {
    private int salt;
    private float frequency = 1;

    @Override
    public void setSalt(int salt) {
        this.salt = salt;
    }

    @Override
    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public float sample(float x, float y, int seed) {
        seed += salt;
        x *= frequency;
        y *= frequency;

        int lowX = MathHelper.floor(x);
        int lowY = MathHelper.floor(y);
        int highX = lowX + 1;
        int highY = lowY + 1;

        float v00 = MathHelper.random(lowX, lowY, seed);
        float v01 = MathHelper.random(highX, lowY, seed);
        float v10 = MathHelper.random(lowX, highY, seed);
        float v11 = MathHelper.random(highX, highY, seed);

        return MathHelper.lerp(
                v00, v01, v10, v11, MathHelper.smoothstep(x - lowX), MathHelper.smoothstep(y - lowY)
        ) * 2 - 1;
    }

    @Override
    public float sample(float x, float y, float z, int seed) {
        seed += salt;
        x *= frequency;
        y *= frequency;
        z *= frequency;

        int lowX = MathHelper.floor(x);
        int lowY = MathHelper.floor(y);
        int lowZ = MathHelper.floor(z);
        int highX = lowX + 1;
        int highY = lowY + 1;
        int highZ = lowZ + 1;

        float v000 = MathHelper.random(lowX, lowY, lowZ, seed);
        float v001 = MathHelper.random(highX, lowY, lowZ, seed);
        float v010 = MathHelper.random(lowX, highY, lowZ, seed);
        float v011 = MathHelper.random(highX, highY, lowZ, seed);
        float v100 = MathHelper.random(lowX, lowY, highZ, seed);
        float v101 = MathHelper.random(highX, lowY, highZ, seed);
        float v110 = MathHelper.random(lowX, highY, highZ, seed);
        float v111 = MathHelper.random(highX, highY, highZ, seed);

        return MathHelper.lerp(
                v000, v001, v010, v011, v100, v101, v110, v111, MathHelper.smoothstep(x - lowX), MathHelper.smoothstep(y - lowY), MathHelper.smoothstep(z - lowZ)
        ) * 2 - 1;
    }
}
