package me.salamander.noisetest.gui.components;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.color.ColorSampler;

import javax.swing.*;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class GradientEditor extends JPanel {
    private final GradientDrawer drawer;

    PointPanel.SlidingPoint selectedPoint = null;
    public GradientEditor() {
        setLayout(new GridBagLayout());
        drawer = new GradientDrawer(ColorGradient.DEFAULT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 0.8;
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        add(drawer, constraints);

        PointPanel points = new PointPanel();
        constraints.gridy = 1;
        constraints.weighty = 0.2;
        add(points, constraints);

        JColorChooser colorChooser = new JColorChooser();
        colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(selectedPoint != null){
                    selectedPoint.setColor(((ColorSelectionModel) e.getSource()).getSelectedColor());
                    selectedPoint.repaint();
                    drawer.setSampler(points.getColorSampler());
                    drawer.repaint();
                }
            }
        });
        constraints.gridy = 2;
        add(colorChooser, constraints);
    }

    public void setGradient(ColorSampler colorSampler) {
        drawer.setSampler(colorSampler);
        drawer.repaint();
    }
}
