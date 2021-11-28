package me.salamander.ourea.util;

public record Grad3(float x, float y, float z){
    public Grad3 normalized(){
        float s = (float) Math.sqrt(x * x + y * y + z * z);
        return new Grad3(x / s, y / s, z / s);
    }

    public float dot(Grad3 g){
        return x * g.x + y * g.y + z * g.z;
    }

    public float dot(float x, float y, float z){
        return this.x * x + this.y * y + this.z * z;
    }
}
