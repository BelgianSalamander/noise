package me.salamander.ourea.render;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Observer;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;

public class Window {
    private static ThreadLocal<Boolean> initialized = ThreadLocal.withInitial(() -> false);

    private final String title;
    private int width;
    private int height;

    private float scrollX, scrollY;

    private long windowHandle;
    private boolean resized;

    private int previousWidth, previousHeight, previousX, previousY;
    private boolean fullscreen = false;

    private FirstClicked screenshot;

    public Window(String title, int width, int height){
        this.title = title;
        this.width = width;
        this.height = height;
        this.resized = false;

        init();
    }

    private void init(){
        if(!initialized.get()){
            GLFWErrorCallback.createPrint(System.err).set();

            if(!glfwInit()){
                throw new IllegalStateException("Unable to initialize GLFW");
            }
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_REFRESH_RATE, 1);

        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        if(windowHandle == 0){
            throw new IllegalStateException("Could not create window '" + title + "'");
        }

        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.height = height;
            this.width = width;
            glViewport(0, 0, width, height);
            this.resized = true;
        });

        glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
            scrollX += xoffset;
            scrollY += yoffset;
        });

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        glfwSwapInterval(1);

        glEnable(GL_DEPTH_TEST);
        //glEnable(GL_FRAMEBUFFER_SRGB);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        screenshot = new FirstClicked(this, GLFW_KEY_F2);
    }

    public Matrix4f getProjectionMatrix(){
        return new Matrix4f().perspective((float) Math.toRadians(60), (float) width / height, 0.1f, 10000.f);
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
        if(screenshot.wasClicked()){
            takeScreenshot();
        }
        glfwSwapBuffers(windowHandle);
    }

    private void takeScreenshot() {
        Path dir = Path.of("run/");
        if(!Files.exists(dir)){
            dir.toFile().mkdirs();
        }

        int[] data = new int[width * height];
        glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, data);
        int[] flipped = new int[width * height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                flipped[(height - j - 1) * width + i] = data[i + j * width];
            }
        }
        String fileName = title.replaceAll("\\s+", "_");
        if(Files.exists(dir.resolve(fileName + ".png"))){
            int i = 1;
            while(Files.exists(dir.resolve(fileName + "_" + i + ".png"))){
                i++;
            }
            fileName += "_" + i;
        }
        fileName += ".png";

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, flipped, 0, width);
        try {
            ImageIO.write(image, "png", dir.resolve(fileName).toFile());
        }catch (IOException e){
            e.printStackTrace();
        }
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

    public void show(){
        glfwShowWindow(windowHandle);
    }

    public void hide(){
        glfwHideWindow(windowHandle);
    }

    public void fullscreen(){
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode videoMode = glfwGetVideoMode(monitor);

        previousWidth = width;
        previousHeight = height;
        int[] xTemp = new int[1];
        int[] yTemp = new int[1];
        glfwGetWindowPos(windowHandle, xTemp, yTemp);
        previousX = xTemp[0];
        previousY = yTemp[0];

        glfwSetWindowMonitor(windowHandle, monitor, 0, 0, videoMode.width(), videoMode.height(), 0);
        glfwSwapInterval(1);
    }

    public void exitFullscreen(){
        glfwSetWindowMonitor(windowHandle, 0, previousX, previousY, previousWidth, previousHeight, 0);
    }

    public void toggleFullscreen(){
        fullscreen = !fullscreen;

        if(fullscreen){
            fullscreen();
        }else{
            exitFullscreen();
        }
    }

    public float getScroll() {
        return scrollY;
    }
}
