package me.salamander.noisetest.render;

import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.render.api.*;
import me.salamander.noisetest.render.api.Window;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
    private float heightScale = 0;

    private final int width, height;

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

        glBindBuffer(GL_ARRAY_BUFFER, heightAndColorVboID);
        glBufferData(GL_ARRAY_BUFFER, 4 * (1 + 3 + 3) * width * height, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(1, 1, GL_FLOAT, false, 28, 0);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 28, 4);
        glVertexAttribPointer(3, 3, GL_FLOAT, false, 28, 16);
    }

    public void setHeightmapData(double[][] heightmap, float heightScale, ColorSampler sampler){
        if(heightmap.length != width || heightmap[0].length != height){
            throw new IllegalArgumentException("Heightmap does not match renderer's size. Expected (" + width + ", " + height + "). Got (" + heightmap.length + ", " + heightmap[0].length + ").");
        }

        this.heightScale = heightScale;

        float[] data = new float[width * height * 7];
        int i = 0;
        Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                double heightAtPoint = heightmap[x][y];
                Color colorAtPoint = sampler.sample(heightAtPoint);
                heightAtPoint *= heightScale;

                data[i] = (float) heightAtPoint;
                
                data[i + 1] = colorAtPoint.getRed() / 255.f;
                data[i + 2] = colorAtPoint.getGreen() / 255.f;
                data[i + 3] = colorAtPoint.getBlue() / 255.f;

                Vector3f normalVector = new Vector3f();

                Vector3f vertexNorth = null, vertexEast = null, vertexSouth = null, vertexWest = null;

                if(x != 0){
                    vertexWest = new Vector3f(-1, (float) (heightmap[x-1][y] * heightScale - heightAtPoint), 0);
                }

                if(y != 0){
                    vertexSouth = new Vector3f(0, (float) (heightmap[x][y-1] * heightScale - heightAtPoint), -1);
                }

                if(x != width - 1){
                    vertexEast = new Vector3f(1, (float) (heightmap[x+1][y] * heightScale - heightAtPoint), 0);
                }

                if(y != height - 1){
                    vertexNorth = new Vector3f(0, (float) (heightmap[x][y+1] * heightScale - heightAtPoint), 1);
                }

                Vector3f[] v1 = new Vector3f[]{vertexNorth, vertexEast, vertexSouth, vertexWest};
                Vector3f[] v2 = new Vector3f[]{vertexEast, vertexSouth, vertexWest, vertexNorth};

                for(int index = 0; index < 4; index++){
                    Vector3f vectorOne = v1[index];
                    Vector3f vectorTwo = v2[index];

                    if(vectorOne != null && vectorTwo != null){
                        normalVector.add(vectorOne.cross(vectorTwo, new Vector3f()));
                    }
                }

                normalVector.normalize();
                data[i + 4] = normalVector.x;
                data[i + 5] = normalVector.y;
                data[i + 6] = normalVector.z;
                i += 7;
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, heightAndColorVboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);
    }

    public void draw(){
        program.bind();

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

        int[] data = new int[1024];
        glGetBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, data);

        Vector3f baseLightDirection = (new Vector3f(1.0f, 2.0f, 1.0f)).normalize();

        int mvpLocation = program.getUniformLocation("mvpMatrix");
        int lightDirectionLocation = program.getUniformLocation("lightDirection");
        int doDiffuseLocation = program.getUniformLocation("doDiffuse");
        long prevTime = System.currentTimeMillis();
        while(!window.shouldClose()){
            long newTime = System.currentTimeMillis();
            float dt = (newTime - prevTime) / 1000.0f;
            prevTime = newTime;
            camera.handleInput(window, dt);

            glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

            Matrix4f mvpMatrix = GLUtil.getProjectionMatrix(window).mul(camera.getViewMatrix());
            program.setUniform(mvpLocation, mvpMatrix);
            program.setUniform(lightDirectionLocation, baseLightDirection);
            program.setUniform(doDiffuseLocation, window.isKeyPressed(GLFW_KEY_L));
            draw();

            window.swapBuffers();

            glfwPollEvents();
        }
    }
}
