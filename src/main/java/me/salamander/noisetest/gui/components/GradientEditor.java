package me.salamander.noisetest.gui.components;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.color.ColorSampler;

import javax.swing.*;
import java.awt.*;

public class GradientEditor extends JComponent {
    public GradientEditor() {
        GradientDrawer drawer = new GradientDrawer(ColorGradient.TERRAIN);
        drawer.setSize(500, 500);
        add(drawer);
    }
}
