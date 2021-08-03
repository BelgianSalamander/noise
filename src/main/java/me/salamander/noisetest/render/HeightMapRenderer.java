package me.salamander.noisetest.render;

import me.salamander.noisetest.NoiseTest;
import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.render.api.*;
import me.salamander.noisetest.render.api.Window;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.glfw.GLFW.*;

public class HeightMapRenderer {
    private final int vaoID;
    private final int indexVboID, posVboID;
    private final Window window;
    private final ShaderProgram program;
    private float heightScale = 0;

    private final int mvpLocation, lightDirectionLocation, doDiffuseLocation;

    private final int width, height;

    private final List<NamedBuffer> buffers = new ArrayList<>();

    public HeightMapRenderer(int width, int height){
        GLUtil.init();

        this.width = width;
        this.height = height;

        window = new Window("Heightmap", 1000, 1000);
        program = new ShaderProgram(
                GLUtil.loadResource("shaders/heightmap/vert.glsl"),
                GLUtil.loadResource("shaders/heightmap/frag.glsl"),
                false //Don't link yet
        );

        program.link();

        doDiffuseLocation = program.getUniformLocation("doDiffuse");
        mvpLocation = program.getUniformLocation("mvpMatrix");
        lightDirectionLocation = program.getUniformLocation("lightDirection");

        glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        vaoID = glGenVertexArrays();
        indexVboID = glGenBuffers();
        posVboID = glGenBuffers();

        int[] indices = new int[2 * 3 * (width - 1) * (height - 1)];
        float[] positions = new float[width * height * 2];

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int offset = 2 * (y * height + x);
                positions[offset] = x;
                positions[offset + 1] = y;

                if(x != (width - 1) && y != (height - 1)){
                    int indexOffset = 6 * (y * (height - 1) + x);
                    int baseIndex = offset / 2;
                    indices[indexOffset] = baseIndex;
                    indices[indexOffset + 1] = baseIndex + 1;
                    indices[indexOffset + 2] = baseIndex + width;

                    indices[indexOffset + 3] = baseIndex + width;
                    indices[indexOffset + 4] = baseIndex + 1;
                    indices[indexOffset + 5] = baseIndex + width + 1;
                }
            }
        }

        glBindVertexArray(vaoID);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, posVboID);
        glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);
    }

    private void addBuffer(String name, BufferObject buffer){
        buffers.add(new NamedBuffer(name, buffer));
    }

    public void addHeightmap(String name, BufferObject buffer){
        addBuffer(name, buffer);
    }

    public void addHeightmap(String name, NoiseModule module, float heightScale, ColorSampler sampler){
        double[][] heightMap = RenderHelper.generateNoise(module, width, height, 0.01f);
        addBuffer(name, RenderHelper.createBufferFromHeightmap(heightMap, heightScale, sampler));
    }

    public void setHeightmapData(BufferObject buffer){
        glBindVertexArray(vaoID);
        buffer.bind(GL_ARRAY_BUFFER);

        glVertexAttribPointer(1, 1, GL_FLOAT, false, 28, 0);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 28, 4);
        glVertexAttribPointer(3, 3, GL_FLOAT, false, 28, 16);
    }
    public void setHeightmapData(double[][] heightmap, float heightScale, ColorSampler sampler){
        BufferObject buffer = RenderHelper.createBufferFromHeightmap(heightmap, heightScale, sampler);
        this.heightScale = heightScale;

        setHeightmapData(buffer);
    }

    public void setHeightScale(float heightScale) {
        this.heightScale = heightScale;
    }

    public void draw(Matrix4f viewMatrix){
        program.bind();

        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        Matrix4f mvpMatrix = GLUtil.getProjectionMatrix(window).mul(viewMatrix);
        program.setUniform(mvpLocation, mvpMatrix);

        glBindVertexArray(vaoID);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVboID);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        glDrawElements(GL_TRIANGLES, (width - 1) * (height - 1) * 2 * 3, GL_UNSIGNED_INT, 0);
    }

    //Blocking call. Only use for testing purposes
    public void mainLoop(){
        Camera camera = new Camera(window, width / 2, heightScale + 5, height / 2); //Create main camera

        program.bind();
        program.setUniform(doDiffuseLocation, true);

        TimeMeasurer dtGetter = new TimeMeasurer();
        while(!window.shouldClose()){
            camera.handleInput(window, dtGetter.getDT());

            draw(camera.getViewMatrix());

            window.swapBuffers();

            glfwPollEvents();
        }
    }

    public void renderAll(){
        Camera camera = new Camera(window, width / 2, heightScale + 5, height / 2);

        program.bind();
        program.setUniform(doDiffuseLocation, true);

        TimeMeasurer dtGetter = new TimeMeasurer();
        FirstClicked switcher = new FirstClicked(window, GLFW_KEY_K);

        int i = 0;
        System.out.println("Rendering: " + buffers.get(i).name);
        setHeightmapData(buffers.get(i).buffer);
        while(!window.shouldClose()){
            camera.handleInput(window, dtGetter.getDT());

            draw(camera.getViewMatrix());

            if(switcher.wasClicked()){
                i++;
                i %= buffers.size();
                System.out.println("Rendering: " + buffers.get(i).name);
                setHeightmapData(buffers.get(i).buffer);
            }

            window.swapBuffers();

            glfwPollEvents();
        }
    }

    private static final record NamedBuffer(String name, BufferObject buffer){ }
}
