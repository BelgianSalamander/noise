package me.salamander.ourea.gui;

import me.salamander.ourea.gui.modules.GUINoiseModule;
import me.salamander.ourea.gui.modules.Modules;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class NoiseGUI extends JFrame {
    private final ModulePanel modulePanel;
    private final ParameterPanel parameterPanel;

    public NoiseGUI(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;

        modulePanel = new ModulePanel(this);
        modulePanel.addModule(Modules.PERLIN);
        modulePanel.addModule(Modules.CONST);
        modulePanel.addModule(Modules.BINARY);
        add(modulePanel, constraints);

        constraints.weightx = 0.06;
        constraints.gridx = 1;
        parameterPanel = new ParameterPanel(this);
        add(parameterPanel, constraints);

        setTitle("Ourea");
        setSize(800, 600);
        setVisible(true);
    }

    void selectModule(@Nullable GUINoiseModule module){
        parameterPanel.displayParameters(module);
    }
}
