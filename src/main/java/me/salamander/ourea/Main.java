package me.salamander.ourea;

import me.salamander.ourea.color.ColorGradient;
import me.salamander.ourea.glsl.GLSLCompiler;
import me.salamander.ourea.gui.NoiseGUI;
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
import me.salamander.ourea.render.opengl.GPUChunkGenerator;
import me.salamander.ourea.render.opengl.OpenGL2DRenderer;

import java.awt.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        gui();
    }

    private static void gui(){
        NoiseGUI gui = new NoiseGUI();
    }

    public static void profile(){
        NoiseSampler perlin = new PerlinSampler();
        FBM fbm = new FBM(perlin, 6, 0.5f, 2.0f);

        GPUChunkGenerator generator = new GPUChunkGenerator(256, 0.01f, 59584, OpenGL2DRenderer.ColorMode.SMOOTH, new ColorGradient(), fbm);
        generator.init();

        final int X = 100;
        final int Y = 100;
        final int GOES = 100;

        //Times are in nanoseconds
        long[] times = new long[GOES];

        for(int i = 0; i < GOES; i++){
            long duration = generator.profile(X, Y);
            times[i] = duration;
        }

        generator.close();

        //Print total, average, minimum, maximum and standard deviation
        long totalDuration = 0;
        for(long duration : times){
            totalDuration += duration;
        }

        long averageDuration = totalDuration / GOES;

        long minDuration = Long.MAX_VALUE;
        long maxDuration = Long.MIN_VALUE;
        long totalDeviation = 0;
        for(long duration : times){
            if(duration < minDuration){
                minDuration = duration;
            }

            if(duration > maxDuration){
                maxDuration = duration;
            }

            totalDeviation += (duration - averageDuration) * (duration - averageDuration);
        }

        long standardDeviation = (long)Math.sqrt(totalDeviation / GOES);

        System.out.println("Generated " + X + "x" + Y + " chunks " + GOES + " times");
        System.out.println("Total: " + formatNanoseconds(totalDuration));
        System.out.println("Average time per batch: " + formatNanoseconds(averageDuration));
        System.out.println("Minimum time per batch: " + formatNanoseconds(minDuration));
        System.out.println("Maximum time per batch: " + formatNanoseconds(maxDuration));
        //System.out.println("Standard deviation: " + standardDeviation);
        System.out.println("Average time per chunk " + formatNanoseconds(totalDuration / (GOES * X * Y)));
    }

    private static  String formatNanoseconds(long nanoseconds){
        if(nanoseconds < 1000){
            return nanoseconds + " ns";
        }else if(nanoseconds < 1000000){
            return nanoseconds / 1000 + " µs";
        }else if(nanoseconds < 1000000000){
            return nanoseconds / 1000000 + " ms";
        }else if(nanoseconds < 1000000000000L){
            return nanoseconds / 1000000000 + " s";
        }else {
            return nanoseconds / 60000000000L + " ms";
        }
    }

    public static void glslTest(){
        NoiseSampler perlin1 = new PerlinSampler();
        NoiseSampler perlin2 = new PerlinSampler();
        NoiseSampler product = new BinaryModule(perlin1, perlin2, BinaryModule.Operator.MUL);
        NoiseSampler turbulence = new Turbulence(new OpenSimplex2SSampler(), new PerlinSampler(), new FBM(product, 6, 0.5f, 2.0f));

        ColorGradient gradient = new ColorGradient();

        gradient.put(-1, Color.BLUE);
        gradient.put(-0.05f, Color.CYAN);
        gradient.put(0, Color.YELLOW);
        gradient.put(0.2f, Color.GREEN);
        gradient.put(0.6f, new Color(0xff0a8c0c));
        gradient.put(0.8f, Color.GRAY);
        gradient.put(1, Color.WHITE);

        OpenGL2DRenderer<GPUChunkGenerator.InterleavedTerrainChunk> renderer = new GPUChunkGenerator(256, 0.01f, 59584, OpenGL2DRenderer.ColorMode.SMOOTH, gradient, turbulence);
        renderer.init();
        renderer.mainloop();
    }

    public static void CPUTest() {
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
                256, 0.01f, 69420, OpenGL2DRenderer.ColorMode.TEXTURE_SMOOTH, gradient,
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
