package me.salamander.noisetest.render;

import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.render.api.Camera;
import me.salamander.noisetest.render.api.GLUtil;
import me.salamander.noisetest.render.api.ShaderProgram;
import me.salamander.noisetest.render.api.Window;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.glfw.GLFW.*;

public class HeightMapRenderer {
    private final int vaoID;
    private final int indexVboID, heightAndColorVboID, posVboID;
    private final Window window;
    private final ShaderProgram program;

    private final int width, height;

    public HeightMapRenderer(int width, int height){
        GLUtil.init();

        this.width = width;
        this.height = height;

        window = new Window("Heightmap", 1000, 1000);
        program = new ShaderProgram(
                GLUtil.loadResource("shaders/heightmap/vert.glsl"),
                GLUtil.loadResource("shaders/heightmap/frag.glsl")
        );

        glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        vaoID = glGenVertexArrays();
        indexVboID = glGenBuffers();
        heightAndColorVboID = glGenBuffers();
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
                    int baseIndex = offset;
                    indices[indexOffset] = baseIndex;
                    indices[indexOffset + 1] = baseIndex + 1;
                    indices[indexOffset + 2] = baseIndex + width;

                    indices[indexOffset + 3] = baseIndex + width;
                    indices[indexOffset + 4] = baseIndex + 1;
                    indices[indexOffset + 5] = baseIndex + width + 1;
                }
            }
        }

        System.out.print("Indices: ");
        for(int i = 0; i < 40; i++){
            System.out.print(indices[i] + " ");
        }
        System.out.println();

        for(int i = 0; i < 5; i++){
            System.out.println("##Vertex " + i + "##");
            System.out.println("Position: (" + positions[i * 2] + ", " + positions[i * 2 + 1] + ")");
        }

        glBindVertexArray(vaoID);

        glBindBuffer(GL_INDEX_ARRAY, indexVboID);
        glBufferData(GL_INDEX_ARRAY, indices, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, posVboID);
        glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);

        glBindBuffer(GL_ARRAY_BUFFER, heightAndColorVboID);
        glBufferData(GL_ARRAY_BUFFER, 4 * (1 + 3) * width * height, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(1, 1, GL_FLOAT, false, 16, 0);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 16, 4);
    }

    public void setHeightmapData(double[][] heightmap, ColorSampler sampler){
        if(heightmap.length != width || heightmap[0].length != height){
            throw new IllegalArgumentException("Heightmap does not match renderer's size. Expected (" + width + ", " + height + "). Got (" + heightmap.length + ", " + heightmap[0].length + ").");
        }

        float[] data = new float[width * height * 4];
        int i = 0;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                double heightAtPoint = heightmap[x][y];
                Color colorAtPoint = sampler.sample(heightAtPoint);

                data[i] = (float) heightAtPoint;
                data[i + 1] = colorAtPoint.getRed() / 255.f;
                data[i + 2] = colorAtPoint.getGreen() / 255.f;
                data[i + 3] = colorAtPoint.getBlue() / 255.f;
                i += 4;
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, heightAndColorVboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);
    }

    public void draw(){
        program.bind();

        glBindVertexArray(vaoID);
        glBindBuffer(GL_INDEX_ARRAY, indexVboID);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, (width - 1) * (height - 1) * 2 * 3, GL_INT, 0);
    }

    //Blocking call. Only use for testing purposes
    public void mainLoop(){
        Camera camera = new Camera(window); //Create main camera

        int debugBuffer = glGenBuffers(); //Create debug output buffer
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, debugBuffer);

        glBufferData(GL_SHADER_STORAGE_BUFFER, new float[]{1,2,3,4,5,6,7,8,9,10}, GL_DYNAMIC_READ); //Set output buffer with initial data

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, debugBuffer); //Bind to correct binding point (0)
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0); //Unbind

        float[] debugData = new float[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1}; //Verify initial data
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, debugBuffer);
        glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, debugData);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        System.out.print("Debug Data: ");
        for(int i = 0; i < 10; i++){
            System.out.print(debugData[i] + " ");
        }
        System.out.println();

        int mvpLocation = program.getUniformLocation("mvpMatrix");
        long prevTime = System.currentTimeMillis();
        while(!window.shouldClose()){
            long newTime = System.currentTimeMillis();
            float dt = (newTime - prevTime) / 1000.0f;
            prevTime = newTime;
            camera.handleInput(window, dt);

            glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

            program.setUniform(mvpLocation, GLUtil.getProjectionMatrix(window).mul(camera.getViewMatrix()));
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, debugBuffer);
            draw();

            float[] debugData1 = new float[10];
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, debugBuffer);
            glGetBufferSubData(GL_INDEX_ARRAY, 0, debugData1);
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

            System.out.print("Shader Output: ");
            for(int i = 0; i < 10; i++){
                System.out.print(debugData1[i] + " ");
            }
            System.out.println();

            window.swapBuffers();

            glfwPollEvents();
        }
    }
}
