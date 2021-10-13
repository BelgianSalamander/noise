package me.salamander.noisetest;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.glsl.GLSLTranspiler;
import me.salamander.noisetest.gui.Modules;
import me.salamander.noisetest.gui.NoiseGUI;
import me.salamander.noisetest.gui.components.GradientEditor;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.combiner.BinaryFunctionType;
import me.salamander.noisetest.modules.combiner.BinaryModule;
import me.salamander.noisetest.modules.source.*;
import me.salamander.noisetest.render.HeightMapGenerator;
import me.salamander.noisetest.render.HeightMapRenderer;
import me.salamander.noisetest.render.RenderHelper;
import me.salamander.noisetest.render.api.ComputeShader;
import me.salamander.noisetest.render.api.Window;
import me.salamander.noisetest.terra.TerraLoader;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

import javax.swing.*;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL45.*;

public class NoiseTest {
    public static void main(String[] args){
        generatorTest();
    }

    private static void generatorTest(){
        HeightMapGenerator generator = new HeightMapGenerator(512, new Ridge(), ColorGradient.TERRAIN);
        generator.mainloop();
    }

    private static void terraTest() throws IOException {
        FileInputStream fin = new FileInputStream("res/test2.yml");
        NoiseModule noise = TerraLoader.loadTerraNoise(new String(fin.readAllBytes(), StandardCharsets.UTF_8));
        fin.close();

        HeightMapGenerator generator = new HeightMapGenerator(512, (GLSLCompilable) noise, ColorGradient.DEFAULT);
        generator.setStep(1.f);
        generator.mainloop();

        /*HeightMapRenderer renderer = new HeightMapRenderer(512, 512);
        renderer.setDefaultStep(0.01f);
        renderer.setDefaultSampler(ColorGradient.TERRAIN);
        renderer.setHeightScale(20.f);

        renderer.addHeightmap("cpu", noise);

        renderer.renderAll();*/
    }

    private static void queryBlockInfo() throws IOException{
        InputStream fin = NoiseTest.class.getResourceAsStream("/shaders/heightmap/normals.glsl");

        String code = new String(fin.readAllBytes(), StandardCharsets.UTF_8);

        fin.close();

        Window window = new Window("Context", 100, 100);

        ComputeShader program = new ComputeShader(code);

        int numSSB = glGetProgramInterfacei(program.getHandle(), GL_SHADER_STORAGE_BLOCK, GL_ACTIVE_RESOURCES);
        System.out.println(numSSB + " Shader Storage Buffers");

        for(int i = 0; i < numSSB; i++){
            String name = glGetProgramResourceName(program.getHandle(), GL_SHADER_STORAGE_BLOCK, i);
            System.out.println("Buffer Name: " + name);

            int resourceIndex = glGetProgramResourceIndex(program.getHandle(), GL_SHADER_STORAGE_BLOCK, name);
            System.out.println("\tResource Index: " + resourceIndex);

            int[] properties = new int[]{GL_NUM_ACTIVE_VARIABLES};
            int[] length = new int[1];
            int[] values = new int[20];
            glGetProgramResourceiv(program.getHandle(), GL_SHADER_STORAGE_BLOCK, resourceIndex, properties, length, values);
            int amountVariables = values[0];

            int[] vars = new int[amountVariables];
            properties[0] = GL_ACTIVE_VARIABLES;
            glGetProgramResourceiv(program.getHandle(), GL_SHADER_STORAGE_BLOCK, resourceIndex, properties, length, vars);

            System.out.println("\tNum Variables: " + amountVariables);
            System.out.println("Variable Indices: " + String.join(", ", Arrays.stream(vars).mapToObj(String::valueOf).collect(Collectors.toList())));

            for (int j = 0; j < amountVariables; j++) {
                properties[0] = GL_OFFSET;
                glGetProgramResourceiv(program.getHandle(), GL_BUFFER_VARIABLE, vars[j], properties, length, values);

                String variableName = glGetProgramResourceName(program.getHandle(), GL_BUFFER_VARIABLE, vars[j]);

                System.out.println(variableName + " offset = " + values[0]);
            }
        }
    }

    public static void glslTest() throws IOException {
        FileOutputStream fout = new FileOutputStream("run/out.glsl");

        NoiseSourceModule perlin = new NoiseSourceModule(NoiseType.PERLIN);

        String shaderSource = GLSLTranspiler.compileModule(perlin);
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

        float[][] heightmap = new float[512][512];
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
        renderer.addHeightmap("cpu", RenderHelper.generateNoise(perlin, 512, 512, 0.01f));

        renderer.renderAll();
    }

    public static void serializationTest() throws IOException {
        NoiseSourceModule perlin = new NoiseSourceModule(NoiseType.PERLIN);
        perlin.setFrequency(0.5f);
        perlin.setLacunarity(3.0f);

        NoiseSourceModule openSimplex = new NoiseSourceModule(NoiseType.OPEN_SIMPLEX2S);
        openSimplex.setPersistence(0.2f);

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

        float[][] map = RenderHelper.generateNoise(baseTerrain, 500, 500, 0.01f);

        renderer.addHeightmap("terrain", RenderHelper.createBufferFromHeightmap(map, 40.0f, ColorGradient.TERRAIN));
        renderer.renderAll();
    }
}
