package me.salamander.noisetest;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.glsl.FunctionRegistry;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.glsl.GLSLTranspiler;
import me.salamander.noisetest.gui.Modules;
import me.salamander.noisetest.gui.NoiseGUI;
import me.salamander.noisetest.gui.components.GradientEditor;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.combiner.BinaryFunctionType;
import me.salamander.noisetest.modules.combiner.BinaryModule;
import me.salamander.noisetest.modules.combiner.Select;
import me.salamander.noisetest.modules.modifier.Turbulence;
import me.salamander.noisetest.modules.modifier.Voronoi;
import me.salamander.noisetest.modules.source.*;
import me.salamander.noisetest.noise.PerlinNoise2D;
import me.salamander.noisetest.render.HeightMapRenderer;
import me.salamander.noisetest.render.RenderHelper;
import me.salamander.noisetest.render.api.ComputeShader;
import me.salamander.noisetest.render.api.Window;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

import javax.swing.*;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL45.*;

public class NoiseTest {
    public static void main(String[] args){
        try {
            glslTest();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void glslTest() throws IOException {
        FileOutputStream fout = new FileOutputStream("run/out.glsl");

        CheckerBoard module = new CheckerBoard();
        Ridge other = new Ridge();
        NoiseSourceModule selector = new NoiseSourceModule(NoiseType.SIMPLEX);

        Select select = new Select(module, other, selector);
        select.setEdgeFalloff(0.1);

        Turbulence turbulence = new Turbulence(select);
        Voronoi voronoi = new Voronoi(17847874);
        voronoi.setInput(0, other);

        String shaderSource = GLSLTranspiler.compileModule(voronoi);
        fout.write(shaderSource.getBytes(StandardCharsets.UTF_8));
        fout.close();

        //Create context. Don't have any impls for headless context
        Window window = new Window("Context", 100, 100);

        ComputeShader shader = new ComputeShader(shaderSource);
        shader.bind();

        shader.setUniformUnsignedInt("baseSeed", 56);
        System.out.println("Base Seed: " + 56);
        shader.setUniform("startPos", new Vector2f(0, 0));
        shader.setUniform("step", 0.01f);
        shader.setUniformUnsignedInt("width", 512);

        //Create SSBO (height data)
        int ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, 512 * 512 * 4 * 4, GL_DYNAMIC_READ);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, ssbo);

        long startTime = System.currentTimeMillis();
        shader.run(512 / 32, 512 / 32, 1);

        FloatBuffer heightmapBuffer = MemoryUtil.memAllocFloat(512 * 512 * 4);
        glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, heightmapBuffer);

        System.out.println("Generated heightmap in " + (System.currentTimeMillis() - startTime) + " ms");

        double[][] heightmap = new double[512][512];
        for(int y = 0; y < 512; y++){
            for(int x = 0; x < 512; x++){
                heightmap[x][y] = heightmapBuffer.get();
                heightmapBuffer.get();
                heightmapBuffer.get();
                heightmapBuffer.get();
            }
        }


        HeightMapRenderer renderer = new HeightMapRenderer(512, 512);
        renderer.setHeightScale(20);
        renderer.setDefaultSampler(ColorGradient.DEFAULT);

        renderer.addHeightmap("glsl", heightmap);
        renderer.addHeightmap("cpu", RenderHelper.generateNoise(select, 512, 512, 0.01f));

        renderer.renderAll();
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
        SerializableNoiseModule deserialized = Modules.deserializeNode(tag);
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
        SerializableNoiseModule baseTerrain = new NoiseSourceModule(NoiseType.PERLIN);
        baseTerrain.setSeed(69);

        HeightMapRenderer renderer = new HeightMapRenderer(500, 500);
        renderer.setDefaultStep(0.01f);
        renderer.setHeightScale(40.0f);
        renderer.setDefaultSampler(ColorGradient.TERRAIN);

        double[][] map = RenderHelper.generateNoise(baseTerrain, 500, 500, 0.01);

        renderer.addHeightmap("terrain", RenderHelper.createBufferFromHeightmap(map, 40.0f, ColorGradient.TERRAIN));
        renderer.renderAll();
    }
}
