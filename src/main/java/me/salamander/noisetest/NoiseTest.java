package me.salamander.noisetest;

import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.gui.NoiseGUI;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.combiner.Select;
import me.salamander.noisetest.modules.modifier.Max;
import me.salamander.noisetest.modules.modifier.Turbulence;
import me.salamander.noisetest.modules.source.*;
import me.salamander.noisetest.render.HeightMapRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class NoiseTest {
    public static void main(String[] args) {
        NoiseGUI gui = new NoiseGUI();
        gui.setVisible(true);
    }

    private static void renderDemo(){
        NoiseModule module = getNoiseSampler((new Random()).nextLong());

        module = new Max(module, new Const(0.0));

        double[][] map = generateNoise(module, 500, 500, 0.01);

        HeightMapRenderer renderer = new HeightMapRenderer(500, 500);
        renderer.setHeightmapData(map, 20.f, ColorGradient.TERRAIN);
        renderer.mainLoop();
    }

    private static NoiseModule rivers(){
        NoiseModule mountainTerrain = (new Ridge()).multiply(2).add(-1);
        Perlin riverSelector = new Perlin();

        Select select = new Select(new Const(-0.5), new Const(0.5), riverSelector);
        select.setThreshold(-0.5);
        select.setEdgeFalloff(0.25);

        return select;
    }

    private static NoiseModule getNoiseSampler(long seed){
        Ridge mountainTerrain = new Ridge(6, seed);
        Billow baseFlatTerrain = new Billow(6, seed);
        baseFlatTerrain.setFrequency(2.0);
        NoiseModule flatTerrain = baseFlatTerrain.multiply(0.25).add(-0.75);

        Perlin terrainType = new Perlin(6, seed);
        terrainType.setFrequency(0.5);
        terrainType.setPersistence(0.25);

        Select selector = new Select(flatTerrain, mountainTerrain, terrainType);
        selector.setThreshold(0.0);
        selector.setEdgeFalloff(0.25);

        Turbulence turbulence = new Turbulence(selector, seed);
        turbulence.setFrequency(0.5);
        turbulence.setTurbulencePower(0.125f);

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

    private static Color safeColor(int r, int g, int b){
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int n){
        if(n < 0) return 0;
        else if(n > 255) return 255;
        else return n;
    }
}
