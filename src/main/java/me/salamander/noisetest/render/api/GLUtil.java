package me.salamander.noisetest.render.api;

import me.salamander.noisetest.NoiseTest;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GLUtil {
    private static Window window;
    static ShaderProgram program;

    public static float FOV = (float) Math.toRadians(60.0);

    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    private static boolean initialised = false;

    public static void init(){
        if(initialised) return;
        initialised = true;
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit()){
            throw new IllegalStateException("Could not initialise GLFW!");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    }

    static void createProgram(){
        program = new ShaderProgram(
                loadResource("shaders/vert.glsl"),
                loadResource("shaders/frag.glsl")
        );
    }

    public static String loadResource(String path){
        InputStream is = NoiseTest.class.getClassLoader().getResourceAsStream(path);
        try{
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }catch (IOException e){
            e.printStackTrace();
            throw new IllegalStateException("Could not read shaders!");
        }
    }

    public static String getValue(int glEnum){
        switch (glEnum){
            case GL_VERTEX_SHADER:
                return "vertex shader";
            case GL_FRAGMENT_SHADER:
                return "fragment shader";
            default:
                return "Unimplemented";
        }
    }

    public static Matrix4f getModelMatrix(){
        return (new Matrix4f()).identity();
    }

    public static Matrix4f getProjectionMatrix(Window window){
        float aspectRatio = ((float) window.getWidth()) / window.getHeight();
        return (new Matrix4f()).setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }

    @Deprecated
    public static void test(){
        init();

        window = new Window("Test", 500, 500);
        createProgram();

        Camera camera = new Camera(window);

        program.bind();

        float[] vertices = new float[]{
                 0.0f,  0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                 0.5f, -0.5f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
        };

        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
        verticesBuffer.put(vertices).flip();

        int vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        memFree(verticesBuffer);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 28, 0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 28, 3 * 4);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        int mvpUniform = program.getUniformLocation("u_mvpMatrix");

        long prevTime = System.currentTimeMillis();
        while(!window.shouldClose()){
            long newTime = System.currentTimeMillis();
            float dt = (newTime - prevTime) / 1000.0f;
            prevTime = newTime;

            camera.handleInput(window, dt);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Matrix4f mvp = getProjectionMatrix(window).mul(camera.getViewMatrix()).mul(getModelMatrix());
            Vector4f transformed =  new Vector4f(0.0f, 0.5f, -1.0f, 1.0f).mul(camera.getViewMatrix());
            transformed.div(transformed.w);

            System.out.println(transformed);

            program.setUniform(mvpUniform, mvp);

            glDrawArrays(GL_TRIANGLES, 0, 3);

            float[] debugData1 = new float[3];
            glGetBufferSubData(GL_ARRAY_BUFFER, 0, debugData1);

            System.out.print("Debug Data: ");
            for(int i = 0; i < 3; i++){
                System.out.print(debugData1[i] + " ");
            }
            System.out.println();

            window.swapBuffers();

            glfwPollEvents();
        }
    }

    public static Window getWindow() {
        return window;
    }

    public static ShaderProgram getProgram() {
        return program;
    }
}
