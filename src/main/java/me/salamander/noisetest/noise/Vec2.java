package me.salamander.noisetest.noise;

import java.util.Random;

public class Vec2 {
    double x, y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(Random random){
        double angle = random.nextDouble() * 2 * Math.PI;
        x = Math.cos(angle);
        y = Math.sin(angle);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void normalize(){
        double scale = 1 / length();
        x *= scale;
        y *= scale;

    }

    public Vec2 add(Vec2 other){
        return new Vec2(x + other.x, y + other.y);
    }
    public Vec2 sub(Vec2 other){return new Vec2(x - other.x, y - other.y);}
    public Vec2 add(double scalar){return new Vec2(x + scalar, y + scalar);}
    public Vec2 sub(double scalar){return new Vec2(x - scalar, y - scalar);}
    public Vec2 floor(){return new Vec2(floor(x), floor(y));}

    public double dot(Vec2 other){
        return x * other.x + y * other.y;
    }

    public double length(){
        return Math.sqrt(x * x + y * y);
    }

    public double sum(){
        return x + y;
    }

    public Vec2 fractionalPart(){return new Vec2(fractionalPart(x), fractionalPart(y));}

    public static double fractionalPart(double n){
        double a = n % 1;
        if(a < 0) a++;
        return a;
    }

    private static int floor(double n){
        int v = (int) n;
        return v > n ? v - 1 : v;
    }

    public Vec2 copy(){
        return new Vec2(x, y);
    }

    public double lengthSquared(){
        return x * x + y * y;
    }
}
