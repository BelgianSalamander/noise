package me.salamander.noisetest.render;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.glsl.GLSLTranspiler;
import me.salamander.noisetest.render.api.*;
import me.salamander.noisetest.util.Pair;
import me.salamander.noisetest.util.Util;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL45.*;

public class HeightMapGenerator {
    private final Window window;
    private final Camera camera;
    private final TimeMeasurer dtGetter;

    private final int tileSize;
    private final float step = 0.01f;
    private final Map<Pair<Integer, Integer>, Pair<Integer, BufferObject>> tiles = new HashMap<>(); //Stores VAO of tiles;

    private final BufferObject sampler;
    private final BufferObject indexBuffer;
    private final BufferObject xzBuffer;

    private final ComputeShader heightProgram;
    private final ComputeShader normalAndColorProgram;

    private final ShaderProgram renderingShader;
    private final int doDiffuseLocation, mvpLocation, lightDirectionLocation;

    private static final int VIEW_DISTANCE = 1;

    public HeightMapGenerator(int tileSize, GLSLCompilable compilable, ColorGradient sampler){
        window = new Window("Noise", 500, 500);
        camera = new Camera(window, tileSize / 2, 25, tileSize / 2);
        dtGetter = new TimeMeasurer();

        this.tileSize = tileSize;

        String source = GLSLTranspiler.compileModule(compilable, "/run/out.glsl");
        this.heightProgram = new ComputeShader(source);
        heightProgram.bind();
        heightProgram.setUniform("step", step);
        heightProgram.setUniformUnsignedInt("width", tileSize);
        setSeed((int) compilable.getSeed());

        try {
            this.normalAndColorProgram = new ComputeShader(Util.loadResource("/shaders/heightmap/normals.glsl"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load normal program");
        }

        normalAndColorProgram.bind();
        System.out.println("Noise and color program handle: " + normalAndColorProgram.getHandle());
        normalAndColorProgram.setUniformUnsignedInt("amountPoints", sampler.numPoints());
        normalAndColorProgram.setUniformUnsignedInt("tileWidth", tileSize);
        normalAndColorProgram.setUniformUnsignedInt("tileHeight", tileSize);
        normalAndColorProgram.setUniform("heightScale", 20.f);
        //glUseProgram(0);

        this.sampler = sampler.toBuffer();

        this.indexBuffer = new BufferObject();
        this.xzBuffer = new BufferObject();

        FloatBuffer xzData = MemoryUtil.memAllocFloat(tileSize * tileSize * 2);
        IntBuffer indexData = MemoryUtil.memAllocInt(2 * 3 * (tileSize - 1) * (tileSize - 1));

        for(int y = 0; y < tileSize; y++){
            for (int x = 0; x < tileSize; x++) {
                xzData.put(x);
                xzData.put(y);

                int baseIndex = (y * tileSize + x);

                if(x != (tileSize - 1) && y != (tileSize - 1)){
                    indexData.put(baseIndex);
                    indexData.put(baseIndex + 1);
                    indexData.put(baseIndex + tileSize);
                    indexData.put(baseIndex + tileSize);
                    indexData.put(baseIndex + 1);
                    indexData.put(baseIndex + tileSize + 1);
                }
            }
        }

        indexData.flip();
        xzData.flip();

        indexBuffer.data(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW);
        xzBuffer.data(GL_ARRAY_BUFFER, xzData, GL_STATIC_DRAW);

        MemoryUtil.memFree(xzData);
        MemoryUtil.memFree(indexData);

        renderingShader = new ShaderProgram(
                GLUtil.loadResource("shaders/heightmap/vert.glsl"),
                GLUtil.loadResource("shaders/heightmap/frag.glsl")
        );
        renderingShader.bind();

        doDiffuseLocation = renderingShader.getUniformLocation("doDiffuse");
        mvpLocation = renderingShader.getUniformLocation("mvpMatrix");
        lightDirectionLocation = renderingShader.getUniformLocation("lightDirection");

        renderingShader.setUniform(doDiffuseLocation, true);
    }

    public void setSeed(int seed){
        heightProgram.bind();
        heightProgram.setUniform("baseSeed", seed);
    }

    private void drawTile(int x, int y, Matrix4f viewProjectionMatrix){
        //System.out.println("Drawing Tile " + x  + ", " + y);
        int vao = getTile(x, y);
        //System.out.println("Fetched Tile");

        glBindVertexArray(vao);
        for (int i = 0; i < 4; i++) {
            glEnableVertexAttribArray(i);
        }

        indexBuffer.bind(GL_ELEMENT_ARRAY_BUFFER);

        Matrix4f modelMatrix = new Matrix4f().translate(x * tileSize - x, 0, y * tileSize - y).scale(1, 20, 1);

        renderingShader.bind();
        renderingShader.setUniform(mvpLocation, viewProjectionMatrix.mul(modelMatrix, new Matrix4f()));

        //System.out.println("Launching draw call");
        glDrawElements(GL_TRIANGLES, (tileSize - 1) * (tileSize - 1) * 2 * 3, GL_UNSIGNED_INT, 0);
        //System.out.println("Finished draw call");
    }

    private void doFrame(){
        camera.handleInput(window, dtGetter.getDT());

        Matrix4f viewProjectionMatrix = GLUtil.getProjectionMatrix(window).mul(camera.getViewMatrix());

        //renderingShader.bind();
        //renderingShader.setUniform(doDiffuseLocation, GLFW.glfwGetKey(window.getWindowHandle(), GLFW.GLFW_KEY_L) == GLFW.GLFW_PRESS);

        int tileX = Math.floorDiv((int) camera.getPosition().x , tileSize);
        int tileY = Math.floorDiv((int) camera.getPosition().z , tileSize);

        for(int xo = -VIEW_DISTANCE; xo <= VIEW_DISTANCE; xo++){
            for (int zo = -VIEW_DISTANCE; zo <= VIEW_DISTANCE; zo++){
                drawTile(tileX + xo, tileY + zo, viewProjectionMatrix);
            }
        }
    }

    private int getTile(int x, int y){
        Pair<Integer, Integer> key = new Pair<>(x, y);
        var value = tiles.get(key);

        if(value == null){
            return generateTile(x, y);
        }else{
            return value.getFirst();
        }
    }

    private int generateTile(int x, int y) {
        System.out.println("Generating tile at " + x + " " + y);

        heightProgram.bind();
        BufferObject heightmapData = new BufferObject();

        heightmapData.allocate(GL_SHADER_STORAGE_BUFFER, 32L * tileSize * tileSize, GL_DYNAMIC_DRAW);
        heightmapData.bindBase(GL_SHADER_STORAGE_BUFFER, 3);

        heightProgram.setUniform("startPos",new Vector2f(x * (tileSize - 1)* 0.01f, y * (tileSize - 1) * 0.01f));

        heightProgram.run(tileSize / 32, tileSize / 32, 1);

        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        normalAndColorProgram.bind();
        heightmapData.bindBase(GL_SHADER_STORAGE_BUFFER, 3);
        sampler.bindBase(GL_SHADER_STORAGE_BUFFER, 2);


        normalAndColorProgram.run(tileSize / 32, tileSize / 32, 1);

        inspectHeightmap(heightmapData);

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        xzBuffer.bind(GL_ARRAY_BUFFER);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);
        heightmapData.bind(GL_ARRAY_BUFFER);
        glVertexAttribPointer(1, 1, GL_FLOAT, false, 32, 28);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 32, 16);
        glVertexAttribPointer(3, 3, GL_FLOAT, false, 32, 0);

        tiles.put(new Pair<>(x, y), new Pair<>(vao, heightmapData));


        return vao;
    }

    private void inspectHeightmap(BufferObject bufferObject) {
        bufferObject.bind(GL_SHADER_STORAGE_BUFFER);
        FloatBuffer buffer = MemoryUtil.memAllocFloat(tileSize * tileSize * 32);

        bufferObject.readInto(GL_SHADER_STORAGE_BUFFER, buffer);

        boolean hasNormal = false;
        boolean hasColor = false;
        boolean hasHeight = false;

        while(buffer.hasRemaining()){
            float normal_x = buffer.get();
            float normal_y = buffer.get();
            float normal_z = buffer.get();
            if(buffer.get() != 0){
                System.out.println("Yikes");
            }

            float r = buffer.get();
            float g = buffer.get();
            float b = buffer.get();
            float height = buffer.get();

            if(normal_x != 0 || normal_y != 0 || normal_z != 0){
                hasNormal = true;
            }

            if(r != 0 || g != 0 || b != 0){
                hasColor = true;
            }

            if(height != 0){
                hasHeight = true;
            }
        }

        MemoryUtil.memFree(buffer);

        System.out.println("Has Height: " + hasHeight);
        System.out.println("Has Normal: " + hasNormal);
        System.out.println("Has Color: " + hasColor);
    }

    public void mainloop(){
        window.show();

        while(!window.shouldClose()){
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            doFrame();

            //System.out.println("Swapping Buffers!");
            window.swapBuffers();
            //System.out.println("Swapped Buffers!");

            GLFW.glfwPollEvents();
        }
    }
}
