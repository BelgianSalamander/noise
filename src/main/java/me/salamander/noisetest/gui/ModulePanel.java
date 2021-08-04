package me.salamander.noisetest.gui;

import javax.swing.*;
import java.awt.*;

public class ModulePanel extends JPanel {
    public ModulePanel(){
        setLayout(null);
        setBorder(BorderFactory.createLineBorder(Color.RED));

        setMinimumSize(new Dimension(400, 300));
        setSize(500, 500);

        GUINoiseModule module = Modules.PERLIN.get();
        add(module);
        module.setBounds(100, 100, module.getWidth(), module.getHeight());
    }
}
