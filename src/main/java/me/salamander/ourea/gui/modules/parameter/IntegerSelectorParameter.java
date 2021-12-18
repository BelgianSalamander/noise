package me.salamander.ourea.gui.modules.parameter;

import me.salamander.ourea.modules.NoiseSampler;

import javax.swing.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class IntegerSelectorParameter<T extends NoiseSampler> extends BasicParameter<T, Integer> {
    private final int min;
    private final int max;

    public IntegerSelectorParameter(String name, Function<T, Integer> getter, BiConsumer<T, Integer> setter, int min, int max) {
        super(name, getter, setter);

        this.min = min;
        this.max = max;
    }

    @Override
    public JComponent getComponent(T sampler) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel((int) getValue(sampler), min, max, 1));
        spinner.addChangeListener(e -> setValue(sampler, (int) spinner.getValue()));
        return spinner;
    }
}
