package me.salamander.ourea.gui.modules.parameter;

import me.salamander.ourea.modules.NoiseSampler;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FloatSliderParameter<T extends NoiseSampler> extends BasicParameter<T, Float>{
    private final float min;
    private final float max;
    private final int steps;

    public FloatSliderParameter(String name, Function<T, Float> getter, BiConsumer<T, Float> setter, float min, float max, int steps) {
        super(name, getter, setter);
        this.min = min;
        this.max = max;
        this.steps = steps;
    }

    @Override
    public JComponent getComponent(T sampler) {
        float currValue = getValue(sampler);

        JSlider slider = new JSlider(0, steps, valueToSlider(currValue));

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(valueToSlider(min), new JLabel(String.format("%.2f", min)));
        labelTable.put(valueToSlider(max), new JLabel(String.format("%.2f", max)));
        slider.setLabelTable(labelTable);

        slider.setMinorTickSpacing(steps / 50);
        slider.setMajorTickSpacing(steps / 10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        LabelledInput<JSlider> labelled = new LabelledInput<>(slider, (slider1) -> String.format("%.2f", sliderToValue(slider1.getValue())));

        slider.addChangeListener(e -> {
            setValue(sampler, sliderToValue(slider.getValue()));
            labelled.updateText();
        });

        return labelled;
    }

    private int valueToSlider(float value) {
        if(value < min) {
            return 0;
        }else if(value > max) {
            return steps + 1;
        }

        return Math.round((value - min) / (max - min) * (steps));
    }

    private float sliderToValue(int slider) {
        //slider = (value - min) / (max - min) * (steps + 1)
        //slider / (steps + 1) = (value - min) / (max - min)
        //slider * (max - min) / (steps + 1) + min = value
        return (slider * (max - min) / (steps)) + min;
    }
}
