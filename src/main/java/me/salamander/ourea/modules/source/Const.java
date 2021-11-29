package me.salamander.ourea.modules.source;

import me.salamander.ourea.modules.NoiseSampler;

public class Const implements NoiseSampler {
    private float value;

    public Const(){

    }

    public Const(float value){
        this.value = value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void setSalt(int salt) {

    }

    @Override
    public void setFrequency(float frequency) {

    }

    @Override
    public float sample(float x, float y, int seed) {
        return value;
    }

    @Override
    public float sample(float x, float y, float z, int seed) {
        return value;
    }
}
