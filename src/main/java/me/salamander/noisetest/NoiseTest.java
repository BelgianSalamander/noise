package me.salamander.noisetest;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.gui.NoiseGUI;
import me.salamander.noisetest.gui.util.GUIHelper;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.combiner.Select;
import me.salamander.noisetest.modules.source.*;
import me.salamander.noisetest.render.HeightMapRenderer;
import me.salamander.noisetest.render.RenderHelper;

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

        NoiseModule ocean = new Const(0.0f);
        Select head = new Select(ocean, baseTerrain, baseTerrain);
        head.setEdgeFalloff(0.25);

        HeightMapRenderer renderer = new HeightMapRenderer(500, 500);
        renderer.setDefaultStep(0.01f);
        renderer.setHeightScale(40.0f);
        renderer.setDefaultSampler(ColorGradient.TERRAIN);

        double[][] map = RenderHelper.generateNoise(head, 500, 500, 0.01);

        renderer.addHeightmap("terrain", RenderHelper.createBufferFromHeightmap(map, 40.0f, ColorGradient.TERRAIN));
        renderer.renderAll();
    }
}
