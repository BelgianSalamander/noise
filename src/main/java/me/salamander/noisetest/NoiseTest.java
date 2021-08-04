package me.salamander.noisetest;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.color.SingleColor;
import me.salamander.noisetest.gui.NoiseGUI;
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
        guiDemo();
    }

    public static void guiDemo(){
        new NoiseGUI();
    }

    private static void renderDemo(){
        NoiseModule baseTerrain = new Perlin();
        Ridge mountainTerrain = new Ridge();
        Select selector = new Select(baseTerrain, mountainTerrain, baseTerrain);
        selector.setThreshold(0.4);
        selector.setEdgeFalloff(0.2);

        NoiseModule head = new Max(new Const(0.0), selector);

        HeightMapRenderer renderer = new HeightMapRenderer(500, 500);
        renderer.setDefaultStep(0.01f);
        renderer.setHeightScale(20.0f);
        renderer.setDefaultSampler(ColorGradient.TERRAIN);
        renderer.addHeightmap("baseTerrain", baseTerrain);
        renderer.addHeightmap("endTerrain", head);
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

    private static Color safeColor(int r, int g, int b){
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int n){
        if(n < 0) return 0;
        else if(n > 255) return 255;
        else return n;
    }
}
