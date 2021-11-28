package me.salamander.ourea.modules.source;

import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.util.Grad2;
import me.salamander.ourea.util.Grad3;
import me.salamander.ourea.util.MathHelper;

public class PerlinSampler implements NoiseSampler {
    private static final float SCALE2D = 1 / (float) Math.sqrt(2 / 4F);
    private static final float SCALE3D = 1 / (float) Math.sqrt(3 / 4F);

    private long salt = 0;
    private float frequency = 1;

    @Override
    public float sample(float x, float y, long seed) {
        seed += salt;
        x *= frequency;
        y *= frequency;

        int lowX = MathHelper.floor(x);
        int lowY = MathHelper.floor(y);
        int highX = lowX + 1;
        int highY = lowY + 1;

        if(lowX > 3){
            //System.out.println("Poggies");
        }

        Grad2 grad00 = MathHelper.getGradient(lowX, lowY, seed);
        Grad2 grad01 = MathHelper.getGradient(highX, lowY, seed);
        Grad2 grad10 = MathHelper.getGradient(lowX, highY, seed);
        Grad2 grad11 = MathHelper.getGradient(highX, highY, seed);

        return MathHelper.lerp(
                grad00.dot(x - lowX, y - lowY), grad01.dot(x - highX, y - lowY),
                grad10.dot(x - lowX, y - highY), grad11.dot(x - highX, y - highY),
                MathHelper.smoothstep(x - lowX), MathHelper.smoothstep(y - lowY)
        ) * SCALE2D;
    }

    @Override
    public float sample(float x, float y, float z, long seed) {
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

        Grad3 grad000 = MathHelper.getGradient(lowX, lowY, lowZ, seed);
        Grad3 grad001 = MathHelper.getGradient(highX, lowY, lowZ, seed);
        Grad3 grad010 = MathHelper.getGradient(lowX, highY, lowZ, seed);
        Grad3 grad011 = MathHelper.getGradient(highX, highY, lowZ, seed);
        Grad3 grad100 = MathHelper.getGradient(lowX, lowY, highZ, seed);
        Grad3 grad101 = MathHelper.getGradient(highX, lowY, highZ, seed);
        Grad3 grad110 = MathHelper.getGradient(lowX, highY, highZ, seed);
        Grad3 grad111 = MathHelper.getGradient(highX, highY, highZ, seed);

        return MathHelper.lerp(
                grad000.dot(x - lowX, y - lowY, z - lowZ), grad001.dot(x - highX, y - lowY, z - lowZ),
                grad010.dot(x - lowX, y - highY, z - lowZ), grad011.dot(x - highX, y - highY, z - lowZ),
                grad100.dot(x - lowX, y - lowY, z - highZ), grad101.dot(x - highX, y - lowY, z - highZ),
                grad110.dot(x - lowX, y - highY, z - highZ), grad111.dot(x - highX, y - highY, z - highZ),
                MathHelper.smoothstep(x - lowX), MathHelper.smoothstep(y - lowY), MathHelper.smoothstep(z - lowZ)
        ) * SCALE3D;
    }

    @Override
    public void setSalt(long salt) {
        this.salt = salt;
    }

    @Override
    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }
}
