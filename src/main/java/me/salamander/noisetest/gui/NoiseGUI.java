package me.salamander.noisetest.gui;

import me.salamander.noisetest.gui.panels.GUINoiseModule;
import me.salamander.noisetest.gui.panels.ModulePanel;
import me.salamander.noisetest.gui.panels.ParameterPanel;
import me.salamander.noisetest.modules.GUIModule;

import javax.swing.*;
import java.awt.*;

public class NoiseGUI extends JFrame {
    private ModulePanel modulePanel;
    private final ParameterPanel parameterPanel;

    public NoiseGUI(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new GridBagLayout());
        setSize(500, 500);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;

        modulePanel = new ModulePanel();
        add(modulePanel, constraints);

        constraints.weightx = 0.06;
        constraints.gridx = 1;
        parameterPanel = new ParameterPanel();
        add(parameterPanel, constraints);



        setVisible(true);
    }

    public void showParameters(GUINoiseModule module){
        parameterPanel.showParameters(module);
    }
}
