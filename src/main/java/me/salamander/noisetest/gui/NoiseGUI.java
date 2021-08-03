package me.salamander.noisetest.gui;

import me.salamander.noisetest.modules.combiner.Select;
import me.salamander.noisetest.modules.modifier.Turbulence;
import me.salamander.noisetest.modules.source.Perlin;

import javax.swing.*;
import java.awt.*;

public class NoiseGUI extends JFrame {
    public NoiseGUI(){
        init();
    }

    private void init(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setTitle("Noise");

        ModuleEditor editor = new ModuleEditor();

        GUINoiseModule testNoiseModule = new GUINoiseModule(() -> new Select(null, null, null));
        editor.add(testNoiseModule);

        GUINoiseModule test2 = new GUINoiseModule(Perlin::new);
        editor.add(test2);

        add(editor);

        setSize(500, 500);
    }
}
