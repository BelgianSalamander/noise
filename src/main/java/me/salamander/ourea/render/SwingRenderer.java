package me.salamander.ourea.render;

import me.salamander.ourea.modules.NoiseSampler;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class SwingRenderer {
    private final JFrame frame;
    private float minX = 0;
    private float minY = 0;
    private int width = 500;
    private int height = 500;
    private float scale = 0.01f;

    public SwingRenderer() {
        this.frame = new JFrame("Ourea");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(800, 600);
    }

    static float max = 0;

    public void render(NoiseSampler sampler, long seed) {
        //Generate a buffered image of the noise
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float xCoord = minX + x * scale;
                float yCoord = minY + y * scale;
                //Value is in the range [-1, 1]
                float value = sampler.sample(xCoord, yCoord, seed);
                if(value > max) max = value;
                //Scale to the range [0, 255]
                int scaledValue = (int)(value * 127.5f + 127.5f);
                if(scaledValue < 0) {
                    scaledValue = 0;
                    System.out.println("Value is less than 0!");
                }else if(scaledValue > 255) {
                    scaledValue = 255;
                    System.out.println("Value is greater than 255!");
                }
                //Set the pixel
                image.setRGB(x, y, scaledValue << 16 | scaledValue << 8 | scaledValue);
            }
        }
        //Display the image
        this.frame.getContentPane().removeAll();
        this.frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        this.frame.repaint();
        this.frame.setVisible(true);
        System.out.println("Max value: " + max);
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getMinX() {
        return minX;
    }


    public float getMinY() {
        return minY;
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getScale() {
        return scale;
    }
}
