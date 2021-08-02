package me.salamander.noisetest.noise;

import java.util.Random;

public class Vec2 {
    private double x, y;

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

    public double dot(Vec2 other){
        return x * other.x + y * other.y;
    }

    public double length(){
        return Math.sqrt(x * x + y * y);
    }
}
