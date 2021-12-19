package me.salamander.ourea.gui.modules.parameter;

import me.salamander.ourea.modules.NoiseSampler;

import javax.swing.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DropDownParameter<T extends NoiseSampler, U> extends BasicParameter<T, U>{
    private final U[] values;

    public DropDownParameter(String name, Function<T, U> getter, BiConsumer<T, U> setter, U[] values) {
        super(name, getter, setter);
        this.values = values;
    }

    @Override
    public JComponent getComponent(T sampler) {
        JComboBox<U> comboBox = new JComboBox<>(values);
        comboBox.setSelectedItem(getValue(sampler));
        comboBox.addActionListener(e -> setValue(sampler, (U) comboBox.getSelectedItem()));
        return comboBox;
    }
}
