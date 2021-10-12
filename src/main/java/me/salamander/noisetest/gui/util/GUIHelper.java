package me.salamander.noisetest.gui.util;

import me.salamander.noisetest.color.ColorSampler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GUIHelper {
    public static void setFontSize(JLabel label, int size){
        label.setFont(new Font(label.getFont().getName(), label.getFont().getStyle(), size));
    }

    public static void displayArray(float[][] array, ColorSampler colorSampler){
        final int width = array.length;
        final int height = array[0].length;
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                float shade = array[i][j];
                g.setColor(colorSampler.sample(shade));
                g.fillRect(i, j, 1, 1);
            }
        }

        JFrame frame = new JFrame("noise");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                Graphics2D g2d = (Graphics2D) g;
                g2d.clearRect(0, 0, getWidth(), getHeight());
                g2d.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR
                );
                g2d.scale(500 / width, 500 / height);
                g2d.drawImage(img, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(500, 500));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
