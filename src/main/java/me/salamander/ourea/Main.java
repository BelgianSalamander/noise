package me.salamander.ourea;

import me.salamander.ourea.color.ColorGradient;
import me.salamander.ourea.modules.modifier.BinaryModule;
import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.modules.modifier.Turbulence;
import me.salamander.ourea.modules.modifier.UnaryModule;
import me.salamander.ourea.modules.fractal.FBM;
import me.salamander.ourea.modules.source.OpenSimplex2SSampler;
import me.salamander.ourea.modules.source.PerlinSampler;
import me.salamander.ourea.modules.source.coord.X;
import me.salamander.ourea.modules.source.coord.Y;
import me.salamander.ourea.render.opengl.CPUChunkGenerator;
import me.salamander.ourea.render.opengl.OpenGL2DRenderer;
import org.lwjgl.opengl.GL45;

import java.awt.*;

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

        NoiseSampler mult = new UnaryModule(new BinaryModule(new X(), new Y(), BinaryModule.Operator.ADD), UnaryModule.Operator.SIN);
        NoiseSampler noise = new FBM(new OpenSimplex2SSampler(), 6, 0.5f, 2.0f);
        NoiseSampler sampler = new BinaryModule(mult, noise, BinaryModule.Operator.MUL);

        NoiseSampler turbX = new PerlinSampler();
        NoiseSampler turbY = new PerlinSampler();

        Turbulence turbulence = new Turbulence(turbX, turbY, sampler);
        turbulence.setPower(0.25f);

        OpenGL2DRenderer renderer = new CPUChunkGenerator(
                256, 0.01f, 69420L, OpenGL2DRenderer.ColorMode.TEXTURE_SMOOTH, gradient,
                turbulence
        );
        renderer.setViewDistance(3);
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
