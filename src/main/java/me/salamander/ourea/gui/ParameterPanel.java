package me.salamander.ourea.gui;

import me.salamander.ourea.gui.modules.GUINoiseModule;
import me.salamander.ourea.gui.modules.parameter.Parameter;
import me.salamander.ourea.modules.NoiseSampler;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ParameterPanel extends JPanel {
    private GUINoiseModule selectedModule;
    private final JLabel selectedModuleLabel;

    private final JPanel parameters = new JPanel();
    private final Border border;

    private final NoiseGUI parent;

    //TODO: Fix panel changing size when selected module changes
    public ParameterPanel(NoiseGUI parent){
        this.parent = parent;

        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.weightx = 0.1f;
        constraints.weighty = 0.01f;
        constraints.insets = new Insets(10, 0, 10, 0);
        constraints.anchor = GridBagConstraints.NORTH;

        setMinimumSize(new Dimension(300, 300));
        JLabel label = new JLabel("Parameter Editor");
        GUIHelper.setFontSize(label, 30);
        label.setAlignmentX(0.5f);
        add(label, constraints);

        constraints.gridy++;

        this.selectedModuleLabel = new JLabel("No module selected");
        GUIHelper.setFontSize(selectedModuleLabel, 20);
        selectedModuleLabel.setAlignmentX(0.5f);
        add(selectedModuleLabel, constraints);

        constraints.gridy++;
        constraints.weighty = 0.5f;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        parameters.setLayout(new GridBagLayout());
        this.border = BorderFactory.createMatteBorder(5, 5, 5, 5, Color.GRAY);
        add(parameters, constraints);

        setBackground(Color.LIGHT_GRAY);
    }

    public void displayParameters(@Nullable GUINoiseModule module){
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        if(module == selectedModule){
            return;
        }

        if(selectedModule != null){
            selectedModule.setBorder(null);
        }

        selectedModule = module;

        parameters.removeAll();

        if(module == null){
            selectedModuleLabel.setText("No module selected");
            parameters.setBorder(null);

            return;
        }

        parameters.setBorder(border);

        selectedModuleLabel.setText(module.getType().getName());
        selectedModule.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        NoiseSampler sampler = module.getSampler();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.weighty = 0.1f;
        constraints.insets = new Insets(10, 0, 10, 0);
        constraints.anchor = GridBagConstraints.NORTH;

        for(Parameter<?, ?> parameter: module.getType().getParameters()){
            constraints.gridx = 0;
            constraints.weightx = 0.1f;

            JLabel label = new JLabel(parameter.getName() + ": ");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setAlignmentX(0.5f);
            GUIHelper.setFontSize(label, 15);
            parameters.add(label, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0.2f;

            JComponent component = parameter.getComponentRaw(sampler);
            parameters.add(component, constraints);

            constraints.gridy++;
        }

        parameters.setMaximumSize(parameters.getPreferredSize());
    }
}
