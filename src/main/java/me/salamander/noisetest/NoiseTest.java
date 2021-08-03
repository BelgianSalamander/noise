package me.salamander.noisetest;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.combiner.Max;
import me.salamander.noisetest.modules.combiner.Select;
import me.salamander.noisetest.modules.modifier.Turbulence;
import me.salamander.noisetest.modules.source.*;
import me.salamander.noisetest.render.HeightMapRenderer;
import me.salamander.noisetest.render.RenderHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class NoiseTest {
    public static void main(String[] args) {
        renderDemo();
    }

    private static void renderDemo(){
        NoiseModule baseTerrain = new Perlin();
        Ridge mountainTerrain = new Ridge();
        Select selector = new Select(baseTerrain, mountainTerrain, baseTerrain);
        selector.setThreshold(0.4);
        selector.setEdgeFalloff(0.2);

        NoiseModule head = new Max(new Const(0.0), selector);

        HeightMapRenderer renderer = new HeightMapRenderer(500, 500);
        renderer.addHeightmap("baseTerrain", baseTerrain, 20.f, ColorGradient.TERRAIN);
        renderer.addHeightmap("mountainTerrain", mountainTerrain, 20.f, ColorGradient.TERRAIN);
        renderer.addHeightmap("selector", selector.getVisualizer(0.5, 1.0), 20.f, ColorGradient.TERRAIN);
        renderer.addHeightmap("finalTerrain", head, 20.f, ColorGradient.TERRAIN);
        renderer.setHeightScale(20.f);
        renderer.renderAll();
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
    private static NoiseModule getEpicSampler(){
        Billow plains = new Billow();
        plains.setFrequency(2.0);

        Perlin oceanBase = new Perlin();
        NoiseModule ocean = oceanBase.multiply(0.4).add(-0.6);

        Perlin oceanOrNot = new Perlin(3);

        Select oceanSelector = new Select(ocean, plains, oceanOrNot);
        oceanSelector.setEdgeFalloff(0.25);

        Turbulence wobbler = new Turbulence(oceanSelector);
        wobbler.setTurbulencePower(0.25);
        wobbler.setFrequency(0.5);

        return wobbler;
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
