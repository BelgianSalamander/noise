package me.salamander.noisetest.gui;

import me.salamander.noisetest.modules.NoiseModule;

import javax.swing.*;
import java.awt.*;

public class GUINoiseModule extends JPanel {

    private JLabel titleLabel;

    private final NoiseModule noiseModule;
    private final String title;

    private static final int TITLE_HEIGHT = 40;
    private static final int TITLE_FONT_SIZE = 20;
    private static final int WIDTH = 100;

    public GUINoiseModule(String title, NoiseModule module){
        this.title = title;

        setLayout(null);
        this.noiseModule = module;
        setBackground(Color.GRAY);
        setSize(100, 100);

        createTitle();
    }

    private void createTitle(){
        titleLabel = new JLabel(title);
        titleLabel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), titleLabel.getFont().getStyle(), TITLE_FONT_SIZE));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel);
        titleLabel.setBounds(0, 0, WIDTH, TITLE_HEIGHT);
    }
}
