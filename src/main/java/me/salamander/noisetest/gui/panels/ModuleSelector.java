package me.salamander.noisetest.gui.panels;

import me.salamander.noisetest.gui.ModuleCategory;
import me.salamander.noisetest.gui.Modules;
import me.salamander.noisetest.gui.NoiseGUI;
import me.salamander.noisetest.gui.util.GUIHelper;
import me.salamander.noisetest.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Supplier;

public class ModuleSelector extends JFrame {
    private final NoiseGUI caller;

    public ModuleSelector(NoiseGUI caller){
        setLayout(new GridLayout());
        this.caller = caller;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(300, 200);

        for(Pair<ModuleCategory, List<Pair<String, Supplier<GUINoiseModule>>>> category : Modules.categories){
            add(new CategoryPanel(category));
        }

        setVisible(true);
    }

    private void addModule(Supplier<GUINoiseModule> supplier){
        caller.addModule(supplier);
        dispose();
    }

    private class CategoryPanel extends JPanel{
        public CategoryPanel(Pair<ModuleCategory, List<Pair<String, Supplier<GUINoiseModule>>>> category){
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            JLabel label = new JLabel(category.getFirst().getName());
            add(label);
            GUIHelper.setFontSize(label, 20);

            for(Pair<String, Supplier<GUINoiseModule>> module : category.getSecond()){
                JLabel otherLabel = new JLabel(module.getFirst());
                otherLabel.setMaximumSize(new Dimension(1000, 20));
                otherLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        addModule(module.getSecond());
                    }
                });
                add(otherLabel);
            }
        }
    }
}
