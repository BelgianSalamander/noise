package me.salamander.noisetest.gui.components;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.color.ColorSampler;

import javax.swing.*;
import java.awt.*;

public class GradientDrawer extends JPanel {
    private ColorSampler sampler = ColorGradient.DEFAULT;

    public GradientDrawer(ColorSampler sampler){
        this.sampler = sampler;
    }

    public void setSampler(ColorSampler sampler) {
        this.sampler = sampler;
    }

    @Override
    public void paintComponent(Graphics g){
        float halfWidth = getWidth() / 2;

        for(int x = 0; x < getWidth(); x++){
            g.setColor(sampler.sample(x / halfWidth - 1));
            g.drawLine(x, 0, x, getHeight());
        }
    }
}
