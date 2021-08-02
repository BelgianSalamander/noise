package me.salamander.noisetest;

import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.combiner.Select;
import me.salamander.noisetest.modules.modifier.Turbulence;
import me.salamander.noisetest.modules.source.Billow;
import me.salamander.noisetest.modules.source.Const;
import me.salamander.noisetest.modules.source.Perlin;
import me.salamander.noisetest.modules.source.Ridge;
import me.salamander.noisetest.render.HeightMapRenderer;
import me.salamander.noisetest.render.api.GLUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class NoiseTest {
    public static void main(String[] args) {
        NoiseModule testSampler = getNoiseSampler();

        double[][] map = generateNoise(testSampler, 500, 500, 0.01);

        //displayArray(map, ColorGradient.MOUNTAIN);

        HeightMapRenderer renderer = new HeightMapRenderer(500, 500);
        renderer.setHeightmapData(map, ColorGradient.MOUNTAIN);
        renderer.mainLoop();
    }

    private static NoiseModule getNoiseSampler(){
        Ridge mountainTerrain = new Ridge();
        Billow baseFlatTerrain = new Billow();
        baseFlatTerrain.setFrequency(2.0);
        NoiseModule flatTerrain = baseFlatTerrain.multiply(0.25).add(-0.75);

        Perlin terrainType = new Perlin();
        terrainType.setFrequency(0.5);
        terrainType.setPersistence(0.25);

        Select selector = new Select(flatTerrain, mountainTerrain, terrainType);
        selector.setThreshold(0.0);
        selector.setEdgeFalloff(0.25);

        Turbulence turbulence = new Turbulence(selector);
        turbulence.setFrequency(0.5);
        turbulence.setTurbulencePower(0.125);

        return turbulence;
    }

    @NotNull
    private static double[][] generateNoise(@NotNull NoiseModule module, int width, int height, double step){
        double[][] out = new double[width][height];
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                out[x][y] = module.sample(x * step,y * step);
            }
        }
        return out;
    }

    private static void displayArray(double[][] array, ColorSampler colorSampler){
        final int width = array.length;
        final int height = array[0].length;
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                double shade = array[i][j];
                g.setColor(colorSampler.sample(shade));
                g.fillRect(i, j, 1, 1);
            }
        }

        JFrame frame = new JFrame("noise");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    private static void normalize(double[][] array){
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for(double[] a : array){
            for(double n : a){
                if(n < min){
                    min = n;
                }
                if(max < n){
                    max = n;
                }
            }
        }

        double rangeFactor = 2 / (max - min);
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Range: " + (max - min));
        System.out.println("Inverse Range: " + rangeFactor);

        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array[0].length; j++){
                array[i][j] -= min;
                array[i][j] *= rangeFactor;
                array[i][j] -= 1;
            }
        }
    }

    private static Color safeColor(int r, int g, int b){
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int n){
        if(n < 0) return 0;
        else if(n > 255) return 255;
        else return n;
    }
}
