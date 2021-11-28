package me.salamander.ourea;

import me.salamander.ourea.color.ColorGradient;
import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.modules.fractal.FBM;
import me.salamander.ourea.modules.fractal.Ridge;
import me.salamander.ourea.modules.source.OpenSimplex2SSampler;
import me.salamander.ourea.modules.source.PerlinSampler;
import me.salamander.ourea.render.opengl.CPUChunkGenerator;
import me.salamander.ourea.render.opengl.OpenGL2DRenderer;

import java.awt.*;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        ColorGradient gradient = new ColorGradient();

        gradient.put(-1, Color.BLUE);
        gradient.put(-0.05f, Color.CYAN);
        gradient.put(0, Color.YELLOW);
        gradient.put(0.2f, Color.GREEN);
        gradient.put(0.6f, new Color(0xff0a8c0c));
        gradient.put(0.8f, Color.GRAY);
        gradient.put(1, Color.WHITE);

        NoiseSampler sampler = new Ridge(new OpenSimplex2SSampler(), 6, 0.5f, 2.0f);

        OpenGL2DRenderer renderer = new CPUChunkGenerator(256, 0.01f, 69420L, OpenGL2DRenderer.ColorMode.TEXTURE_SMOOTH, gradient, sampler);
        renderer.setViewDistance(1);
        Runnable runnable = () -> {
            renderer.init();
            renderer.mainloop();
        };
        Thread renderThread = new Thread(runnable);
        renderThread.start();

        try {
            renderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
