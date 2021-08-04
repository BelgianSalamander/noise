package me.salamander.noisetest.gui;

import javax.swing.*;
import java.awt.*;

public class NoiseGUI extends JFrame {
    private ModulePanel modulePanel;

    public NoiseGUI(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        modulePanel = new ModulePanel();
        add(modulePanel);

        setSize(500, 500);
        setVisible(true);
    }
}
