package me.salamander.noisetest.noise;

import me.salamander.noisetest.modules.NoiseModule;

import java.util.Random;

public class PerlinNoise2D implements NoiseModule {
    private final int GRAD_LENGTH = 1024;
    private final Vec2[] gradients = new Vec2[GRAD_LENGTH];

    private final Random random;

    private long seed;

    public PerlinNoise2D(){
        this.seed = new Random().nextLong();
        random = new Random(seed);
        createGradients();
    }

    public PerlinNoise2D(long seed){
        random = new Random(seed);
        createGradients();

        this.seed = seed;
    }

    @Override
    public float sample(float x, float y){
        int lowX = floor(x);
        int lowY = floor(y);
        int highX = lowX + 1;
        int highY = lowY + 1;

        Vec2 cornerOneOffset = new Vec2(lowX - x, lowY - y);
        Vec2 cornerTwoOffset = new Vec2(lowX - x, highY - y);
        Vec2 cornerThreeOffset = new Vec2(highX - x, lowY - y);
        Vec2 cornerFourOffset = new Vec2(highX - x, highY - y);

        Vec2 cornerOne = getGradient(lowX, lowY);
        Vec2 cornerTwo = getGradient(lowX, highY);
        Vec2 cornerThree = getGradient(highX, lowY);
        Vec2 cornerFour = getGradient(highX, highY);

        return lerp(cornerOne.dot(cornerOneOffset), cornerTwo.dot(cornerTwoOffset), cornerThree.dot(cornerThreeOffset), cornerFour.dot(cornerFourOffset), smoothstep(x - lowX), smoothstep(y - lowY));
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public void setSeed(long s) {
        this.seed = s;
        this.random.setSeed(s);

        createGradients();
    }

    private void createGradients(){
        for(int i = 0; i < GRAD_LENGTH; i++){
            gradients[i] = new Vec2(random);
        }
    }

    private Vec2 getGradient(int x, int y){
        return gradients[Math.abs(x * 153671 ^ 14283467 + y * 1634217 ^ 146258) % GRAD_LENGTH];
    }

    private int floor(double n){
        int result = (int) n;
        return  result < n ? result : result - 1;
    }

    private float lerp(float v00, float v01, float v10, float v11, float tx, float ty){
        return lerp(lerp(v00, v10, tx), lerp(v01, v11, tx), ty);
    }

    private float lerp(float v0, float v1, float t){
        return (1 - t) * v0 + t * v1;
    }

    private static float smoothstep(float n){
        return n * n * n * (n * ( n * 6 - 15) + 10);
    }

    public static final class Billow extends PerlinNoise2D {
	    public Billow(){
		    super();
	    }

	    public Billow(long seed) {
	    	super(seed);
	    }

	    @Override
	    public float sample(float x, float y) {
		    return Math.abs(super.sample(x, y));
	    }
    }
}
