package me.salamander.noisetest.gui.panels;

import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.gui.ColorSamplers;
import me.salamander.noisetest.gui.Parameter;
import me.salamander.noisetest.gui.util.GUIHelper;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.render.HeightMapRenderer;
import me.salamander.noisetest.render.RenderHelper;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

public class ParameterPanel extends JPanel {
    private JLabel selectedItemLabel = new JLabel("No Module Selected");
    private GUINoiseModule selected = null;
    private HeightMapRenderer renderer = null;

    private JPanel parameterPanel = new JPanel();
    private JPanel previewPanel = new JPanel();

    private JTextField stepField;
    private JComboBox samplerSelector;

    public ParameterPanel(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setMinimumSize(new Dimension(300, 300));
        JLabel label = new JLabel("Parameter Editor");
        //label.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        GUIHelper.setFontSize(label, 30);
        label.setAlignmentX(0.5f);
        add(label);

        selectedItemLabel.setAlignmentX(0.5f);
        GUIHelper.setFontSize(selectedItemLabel, 20);
        add(selectedItemLabel);

        add(Box.createVerticalStrut(50));

        parameterPanel.setLayout(new BoxLayout(parameterPanel, BoxLayout.Y_AXIS));
        //parameterPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        add(parameterPanel);

        createPreviewPanel();

        setBackground(Color.LIGHT_GRAY);
    }

    private void createPreviewPanel(){
        previewPanel.setLayout(new GridBagLayout());
        //previewPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        add(previewPanel);
        previewPanel.setMaximumSize(new Dimension(1000, 100));

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.PAGE_START;
        previewPanel.add(new JLabel("Step: "), constraints);

        constraints.gridx = 1;
        stepField = new JTextField();
        stepField.setText("0.01");
        stepField.setColumns(10);
        previewPanel.add(stepField, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        previewPanel.add(new JLabel("Sampler: "), constraints);

        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        samplerSelector = new JComboBox();
        for(String name : ColorSamplers.getNames()) samplerSelector.addItem(name);
        previewPanel.add(samplerSelector, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        JButton button = new JButton("Preview");
        button.addActionListener(e -> createPreview());
        button.setAlignmentX(0.5f);
        previewPanel.add(button, constraints);

        constraints.gridx = 1;
        JButton renderButton = new JButton("Render");
        renderButton.addActionListener(e -> render());
        renderButton.setAlignmentX(0.5f);
        previewPanel.add(renderButton, constraints);

        previewPanel.setVisible(false);
    }

    public void showParameters(GUINoiseModule module){
        if(module == selected) return;
        if(selected != null){
            selected.setBorder(null);
        }

        selected = module;

        if(module == null){
            selectedItemLabel.setText("No Module Selected");
            previewPanel.setVisible(false);
            parameterPanel.setVisible(false);
            return;
        }
        parameterPanel.setVisible(true);
        parameterPanel.setBackground(Color.LIGHT_GRAY);

        clearParameters();
        selectedItemLabel.setText(module.getTitle());
        module.setBorder(BorderFactory.createMatteBorder(5, 0, 5, 0, Color.LIGHT_GRAY));
        previewPanel.setVisible(true);
        previewPanel.setBackground(Color.LIGHT_GRAY);

        for(Parameter parameter : module.getParameters()){
            double value = module.getNoiseModule().getParameter(parameter.index());
            String valueAsString = parameter.step() == 1.0 ? Integer.toString((int) value) : String.format("%.2f", value);
            JLabel label = new JLabel(parameter.name() + ": " + valueAsString);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setAlignmentX(0.5f);
            parameterPanel.add(label);
            GUIHelper.setFontSize(label, 15);
            JSlider slider;
            int amountValues;
            boolean isInt = parameter.step() == 1.0;

            if(isInt) { //Is int
                slider = new JSlider((int) parameter.minValue(), (int)  parameter.maxValue(), (int) module.getNoiseModule().getParameter(parameter.index()));

                amountValues = (int) (parameter.maxValue() - parameter.minValue() + 1);
            }else{
                int minValue = (int) (10*parameter.minValue() / parameter.step());
                int maxValue = (int) (10*parameter.maxValue() / parameter.step());

                slider = new JSlider(
                        minValue,
                        maxValue,
                        (int) (10 * module.getNoiseModule().getParameter(parameter.index()) / parameter.step())
                );

                amountValues = (maxValue - minValue)/10 + 1;

                Hashtable labelTables = new Hashtable<>();
                labelTables.put(minValue, new JLabel(Double.toString(parameter.minValue())));
                labelTables.put(maxValue, new JLabel(Double.toString(parameter.maxValue())));
                slider.setLabelTable(labelTables);
            }

            slider.setMinorTickSpacing(isInt ? 1 : 10);
            slider.setMinorTickSpacing(1);
            while (amountValues > 100){
                amountValues /= 2;
            }
            slider.setMajorTickSpacing(amountValues - 1);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);

            slider.setForeground(Color.BLACK);
            slider.addChangeListener(new ParameterChangeListener(module.getNoiseModule(), parameter, label, isInt));
            slider.setBackground(Color.LIGHT_GRAY);

            parameterPanel.add(slider);
        }
    }

    private void clearParameters(){
        parameterPanel.removeAll();
    }

    private void createPreview(){
        if(selected == null) return;

        String stepAsText = stepField.getText();
        float step = 0.01f;
        try{
            step = Float.parseFloat(stepAsText);
        }catch(NullPointerException e){}

        ColorSampler sampler = ColorSamplers.getSampler((String) samplerSelector.getSelectedItem());

        float[][] heightmap = RenderHelper.generateNoise(selected.getNoiseModule(), 500, 500, step);
        GUIHelper.displayArray(heightmap, sampler);
    }
    private void render(){
        if(selected == null) return;

        String stepAsText = stepField.getText();
        double step = 0.01;
        try{
            step = Double.parseDouble(stepAsText);
        }catch(NullPointerException e){}

        ColorSampler sampler = ColorSamplers.getSampler((String) samplerSelector.getSelectedItem());
        if(renderer != null){
            renderer.stop();
        }
        renderer = new HeightMapRenderer(500, 500, false);
        double finalStep = step;
        Thread rendererThread = new Thread(){
            @Override
            public void run() {
                renderer.init();
                renderer.setHeightScale(20.0f);
                renderer.setDefaultSampler(sampler);
                renderer.setDefaultStep((float) finalStep);
                renderer.addHeightmap("main", selected.getNoiseModule());
                renderer.renderAll();
                System.out.println("Rendering ended!");
            }
        };

        rendererThread.start();
    }

    public GUINoiseModule getSelectedComponent() {
        return selected;
    }

    private static class ParameterChangeListener implements ChangeListener{
        private final GUIModule noiseModule;
        private final Parameter parameter;
        private final JLabel label;
        private final boolean isInt;

        private ParameterChangeListener(GUIModule noiseModule, Parameter parameter, JLabel label, boolean isInt) {
            this.noiseModule = noiseModule;
            this.parameter = parameter;
            this.label = label;
            this.isInt = isInt;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            float value = ((JSlider) e.getSource()).getValue() * parameter.step();

            if (!this.isInt) {
            	value *= 0.1;
            }

            noiseModule.setParameter(parameter.index(), value);

            String valueAsString = this.isInt ? Integer.toString((int) value) : String.format("%.2f", value);
            label.setText(parameter.name() + ": " + valueAsString);
        }
    }
    private static class FloatChecker implements KeyListener{
        private boolean backspace = false;
        private final JTextField textField;

        public FloatChecker(JTextField textField){
            this.textField = textField;
        }

        @Override
        public void keyTyped(KeyEvent e) { }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == 8) return;

            char c = e.getKeyChar();

            if(c == '.'){
                if(textField.getText().contains(".")){
                    e.consume();
                    System.out.println("Event Consumed!");
                }
            }

            if(!('0' <= c && c <= '9' || c == '.')){
                e.consume();
                System.out.println("Event Consumed!");
            }
        }

        @Override
        public void keyReleased(KeyEvent e) { }
    }

    private static int clamp(int min, int val, int max) {
    	return val < min ? min : (val > max ? max : val);
    }
}
