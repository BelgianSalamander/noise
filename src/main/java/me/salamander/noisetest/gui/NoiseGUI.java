package me.salamander.noisetest.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import me.salamander.noisetest.gui.panels.GUINoiseModule;
import me.salamander.noisetest.gui.panels.ModulePanel;
import me.salamander.noisetest.gui.panels.ModuleSelector;
import me.salamander.noisetest.gui.panels.ParameterPanel;
import me.salamander.noisetest.modules.GUIModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Supplier;

public class NoiseGUI extends JFrame {
    private ModulePanel modulePanel;
    private final ParameterPanel parameterPanel;

    public NoiseGUI(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
	    FlatLightLaf.install();

	    setLayout(new GridBagLayout());
        setSize(1000, 1000);
        setTitle("Noise Editor");

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

        createMenu();

        setVisible(true);
    }

    public void showParameters(GUINoiseModule module){
        parameterPanel.showParameters(module);
    }

    private void createMenu(){
        JMenuBar menuBar = new JMenuBar();

        JMenu editMenu = new JMenu("Edit");
        JMenuItem addModule = new JMenuItem("Add Module");
        addModule.addActionListener(e -> new ModuleSelector(this));
        editMenu.add(addModule);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);
    }

    public void addModule(Supplier<GUINoiseModule> supplier) {
        modulePanel.addModule(supplier);
    }

    public GUINoiseModule getSelected() {
        return parameterPanel.getSelectedComponent();
    }
}
