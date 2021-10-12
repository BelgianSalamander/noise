package me.salamander.noisetest.noise;

import java.util.Random;

public class Vec2 {
    float x, y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(Random random){
        float angle = (float) (random.nextFloat() * 2 * Math.PI);
        x = (float) Math.cos(angle);
        y = (float) Math.sin(angle);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void normalize(){
        float scale = 1 / length();
        x *= scale;
        y *= scale;

    }

    public Vec2 add(Vec2 other){
        return new Vec2(x + other.x, y + other.y);
    }
    public Vec2 sub(Vec2 other){return new Vec2(x - other.x, y - other.y);}
    public Vec2 add(float scalar){return new Vec2(x + scalar, y + scalar);}
    public Vec2 sub(float scalar){return new Vec2(x - scalar, y - scalar);}
    public Vec2 floor(){return new Vec2(floor(x), floor(y));}

    public float dot(Vec2 other){
        return x * other.x + y * other.y;
    }

    public float length(){
        return (float) Math.sqrt(x * x + y * y);
    }

    public float sum(){
        return x + y;
    }

    public Vec2 fractionalPart(){return new Vec2(fractionalPart(x), fractionalPart(y));}

    public static float fractionalPart(float n){
        float a = n % 1;
        if(a < 0) a++;
        return a;
    }

    private static int floor(float n){
        int v = (int) n;
        return v > n ? v - 1 : v;
    }

    public Vec2 copy(){
        return new Vec2(x, y);
    }

    public float lengthSquared(){
        return x * x + y * y;
    }

    @Override
    public String toString() {
        return "Vec2{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
