package me.salamander.noisetest.render.api;

import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;

public class Window {
    private final String title;
    private int width;
    private int height;

    private long windowHandle;
    private boolean resized;

    public Window(String title, int width, int height){
        this.title = title;
        this.width = width;
        this.height = height;
        this.resized = false;

        init();
    }

    private void init(){
        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        if(windowHandle == 0){
            throw new IllegalStateException("Could not create window '" + title + "'");
        }

        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            System.out.println("Resized window!");
            this.height = height;
            this.width = width;
            glViewport(0, 0, width, height);
            this.resized = true;
        });

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        glfwSwapInterval(1);

        glfwShowWindow(windowHandle);

        glEnable(GL_DEPTH_TEST);
        //glEnable(GL_FRAMEBUFFER_SRGB);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public boolean shouldClose(){
        return glfwWindowShouldClose(windowHandle);
    }

    public void swapBuffers(){
        glfwSwapBuffers(windowHandle);
    }

    public boolean isKeyPressed(int key){
        return glfwGetKey(windowHandle, key) == GLFW_PRESS;
    }

    public double[] getMousePosition(){
        double[] xpos = new double[1];
        double[] ypos = new double[1];
        glfwGetCursorPos(windowHandle, xpos, ypos);

        return new double[]{xpos[0], ypos[0]};
    }
}
