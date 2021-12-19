package me.salamander.ourea.gui.modules.parameter;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class LabelledInput<T extends JComponent> extends JPanel {
    private final JLabel label;
    private final T input;
    private final Function<? super T, String> getter;

    public LabelledInput(T input, Function<? super T, String> labelGetter) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        this.label = new JLabel();
        this.input = input;
        this.getter = labelGetter;
        this.add(this.label);
        this.add(this.input);

        updateText();
    }

    public void updateText(){
        this.label.setText(getter.apply(input));
    }
}
