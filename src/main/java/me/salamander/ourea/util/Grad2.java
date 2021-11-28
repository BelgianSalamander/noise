package me.salamander.ourea.util;

public record Grad2(float x, float y) {
    public Grad2 normalized(){
        float f = (float) Math.sqrt(x * x + y * y);
        return new Grad2(x / f, y / f);
    }

    public float dot(Grad2 g){
        return x * g.x + y * g.y;
    }

    public float dot(float x, float y){
        return this.x * x + this.y * y;
    }
}
