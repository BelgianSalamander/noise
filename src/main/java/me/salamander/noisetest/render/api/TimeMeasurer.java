package me.salamander.noisetest.render.api;

public class TimeMeasurer {
    private long previousTime = System.currentTimeMillis();

    public float getDT(){
        long time = System.currentTimeMillis();
        float dt = (time - previousTime) / 1000.f;
        previousTime = time;
        return dt;
    }
}
