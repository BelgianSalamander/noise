package me.salamander.noisetest.color;

import java.awt.*;

public class SingleColor implements ColorSampler{
    private final Color color;

    public SingleColor(Color color) {
        this.color = color;
    }

    @Override
    public Color sample(double n) {
        return color;
    }
}
