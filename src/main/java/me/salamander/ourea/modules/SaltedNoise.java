package me.salamander.ourea.modules;

public interface SaltedNoise extends NoiseSampler{
    void setSalt(int salt);
    int getSalt();
}
