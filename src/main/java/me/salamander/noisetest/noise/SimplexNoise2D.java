package me.salamander.noisetest.noise;

import java.util.Arrays;
import java.util.Random;

//This in NOT OpenSimplex. It is 2D Simplex which is NOT patented
public class SimplexNoise2D {
    private final int GRAD_LENGTH = 1024;
    private final double F = 0.5 * (Math.sqrt(3) - 1);
    private final double G = 0.5 * (1 - 1 / Math.sqrt(3));
    private final double R_SQUARED = 0.5;

    private final Random random;
    private final Vec2[] gradients = new Vec2[GRAD_LENGTH];

    public SimplexNoise2D(){
        random = new Random();
        createGradients();
    }

    public SimplexNoise2D(long seed){
        random = new Random(seed);
        createGradients();
    }

    private Vec2 skew(Vec2 pos){
        return pos.add(pos.sum() * F);
    }
    private Vec2 unskew(Vec2 skewedPos){
        return skewedPos.sub(skewedPos.sum() * G);
    }

    private Vec2[] subdivide(Vec2 skewedPos){
        Vec2[] result = new Vec2[3];
        Vec2 floored = skewedPos.floor();
        result[0] = floored.copy();

        Vec2 fractional = skewedPos.fractionalPart();
        if(fractional.getX() > fractional.getY()){
            floored.x++;
            result[1] = floored.copy();
            floored.y++;
            result[2] = floored;
        }else{
            floored.y++;
            result[1] = floored.copy();
            floored.x++;
            result[2] = floored;
        }

        return result;
    }

    public void createGradients(){
        for(int i = 0; i < GRAD_LENGTH; i++){
            gradients[i] = new Vec2(random);
        }
    }

    private Vec2 getGradient(int x, int y){
        return gradients[Math.abs(x * 153671 ^ 14546234 + y * 1623545 ^ 145351) % GRAD_LENGTH];
    }

    private Vec2 getGradient(Vec2 pos){
        return getGradient((int) pos.x, (int) pos.y);
    }

    public double sample(double x, double y){
        Vec2 pos = new Vec2(x, y);
        Vec2 skewedPos = skew(pos);

        Vec2[] vertices = subdivide(skewedPos);
        Vec2[] unskewedVertices = Arrays.stream(vertices).map(this::unskew).toArray(Vec2[]::new);
        Vec2[] gradients = Arrays.stream(vertices).map(this::getGradient).toArray(Vec2[]::new);
        Vec2[] displacement = Arrays.stream(unskewedVertices).map(vec -> pos.sub(vec)).toArray(Vec2[]::new);

        double total = 0;
        for(int i = 0; i < 3; i++){
            double distance = Math.max(0, R_SQUARED - displacement[i].lengthSquared());
            total += distance * gradients[i].dot(displacement[i]);
        }

        return total;
    }
}
