package me.salamander.noisetest;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.gui.Modules;
import me.salamander.noisetest.gui.NoiseGUI;
import me.salamander.noisetest.gui.components.GradientEditor;
import me.salamander.noisetest.gui.util.GUIHelper;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.combiner.BinaryFunctionType;
import me.salamander.noisetest.modules.combiner.BinaryModule;
import me.salamander.noisetest.modules.combiner.Select;
import me.salamander.noisetest.modules.source.*;
import me.salamander.noisetest.render.HeightMapRenderer;
import me.salamander.noisetest.render.RenderHelper;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class NoiseTest {
    public static void main(String[] args){
        try {
            serializationTest();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void serializationTest() throws IOException {
        NoiseSourceModule perlin = new NoiseSourceModule(NoiseType.PERLIN);
        perlin.setFrequency(0.5);
        perlin.setLacunarity(3.0);

        NoiseSourceModule openSimplex = new NoiseSourceModule(NoiseType.OPEN_SIMPLEX);
        openSimplex.setPersistence(0.2);

        BinaryModule combined = new BinaryModule(BinaryFunctionType.MULTIPLY);
        combined.setInput(0, perlin);
        combined.setInput(1, openSimplex);

        CompoundTag serialized = Modules.serializeNode(combined);

        //Files.createFile(Path.of("noise.sn"));
        FileOutputStream outputStream = new FileOutputStream("noise.sn");
        DataOutput dataOutput = new DataOutputStream(outputStream);

        serialized.write(dataOutput);

        outputStream.close();

        FileInputStream inputStream = new FileInputStream("noise.sn");
        DataInput dataInput = new DataInputStream(inputStream);

        CompoundTag tag = CompoundTag.read(dataInput);
        NoiseModule deserialized = Modules.deserializeNode(tag);
        inputStream.close();
    }

    public static void guiDemo(){
        new NoiseGUI();
    }

    public static void gradientEditorTest(){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Sampler Editor");
        frame.setSize(800, 800);
        frame.add(new GradientEditor());
        frame.setVisible(true);
    }

    private static void renderDemo(){
        NoiseModule baseTerrain = new NoiseSourceModule(NoiseType.PERLIN);
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
