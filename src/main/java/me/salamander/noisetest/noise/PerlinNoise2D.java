package me.salamander.noisetest.noise;

import java.util.Random;

public class PerlinNoise2D {
    private final int GRAD_LENGTH = 1024;

    private final Random random;
    private final Vec2[] gradients = new Vec2[GRAD_LENGTH];

    public PerlinNoise2D(){
        random = new Random();
        createGradients();
    }

    public PerlinNoise2D(long seed){
        random = new Random(seed);
        createGradients();
    }

    public double sample(double x, double y){
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

    private double lerp(double v00, double v01, double v10, double v11, double tx, double ty){
        return lerp(lerp(v00, v10, tx), lerp(v01, v11, tx), ty);
    }

    private double lerp(double v0, double v1, double t){
        return (1 - t) * v0 + t * v1;
    }

    private static double smoothstep(double n){
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
	    public double sample(double x, double y) {
		    return Math.abs(super.sample(x, y));
	    }
    }

}
