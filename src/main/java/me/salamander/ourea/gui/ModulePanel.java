package me.salamander.ourea.gui;

import me.salamander.ourea.gui.modules.GUINoiseModule;
import me.salamander.ourea.gui.modules.GUINoiseModuleType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class ModulePanel extends JPanel {
    private final Random random = new Random();
    private final NoiseGUI parent;

    public ModulePanel(NoiseGUI parent) {
        this.parent = parent;

        setLayout(null);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                parent.selectModule(null);
            }
        });

        setMinimumSize(new Dimension(400, 300));
        setSize(400, 300);
    }

    public void addModule(GUINoiseModuleType<?> type){
        GUINoiseModule module = type.createInstance();
        module.setModulePanel(this);
        add(module);
        module.setLocation(random.nextInt(getWidth() - module.getWidth()), random.nextInt(getHeight() - module.getHeight()));
    }

    public void select(GUINoiseModule module) {
        parent.selectModule(module);
    }

    public void mouseMovement(MouseEvent e) {
        //Will be of use later
    }
}
