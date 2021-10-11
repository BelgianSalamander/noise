package me.salamander.noisetest.color;

import me.salamander.noisetest.render.api.BufferObject;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColorGradient implements ColorSampler {
    public static ColorGradient DEFAULT = new ColorGradient();
    public static ColorGradient TERRAIN = new ColorGradient();
    public static ColorGradient MOUNTAIN = new ColorGradient();

    List<ColorPoint> colorPoints = new ArrayList<>();
    private BufferObject buffer;

    public void addColorPoint(float value, float r, float g, float b){
        colorPoints.add(new ColorPoint(value, new InterpolatableColor(r, g, b)));
    }

    public void addColorPoint(float value, int r, int g, int b){
        colorPoints.add(new ColorPoint(value, new InterpolatableColor((float) r / 255, (float) g / 255, (float) b / 255)));
    }

    public void generate(){
        Collections.sort(colorPoints);
    }

    @Override
    public Color sample(double n){
        int lowerBound = 0;
        int upperBound = colorPoints.size() - 1;

        while (upperBound > lowerBound){
            int testIndex = (lowerBound + upperBound) / 2 + 1;

            double value = colorPoints.get(testIndex).getValue();

            if(value == n){
                lowerBound = upperBound = testIndex;
            }else if(value < n){
                lowerBound = testIndex;
            }else{
                upperBound = testIndex - 1;
            }
        }

        int index = lowerBound;
        if(index == colorPoints.size() - 1){
            return colorPoints.get(index).color.toColor();
        }else{
            ColorPoint lowColor = colorPoints.get(index);
            ColorPoint upperColor = colorPoints.get(index + 1);
            float t = ((float)n - lowColor.value) / (upperColor.value - lowColor.value);
            return lowColor.color.interpolate(upperColor.color, t);
        }
    }

    public void addColorPoint(float n, Color color) {
        colorPoints.add(new ColorPoint(n, new InterpolatableColor(color.getRed() / 255.f, color.getGreen() / 255.f, color.getBlue() / 255.f)));
    }

    public int numPoints() {
        return colorPoints.size();
    }

    private record ColorPoint(float value, InterpolatableColor color) implements Comparable {

        public float getValue() {
            return value;
        }

        public InterpolatableColor getColor() {
            return color;
        }

        @Override
        public int compareTo(@NotNull Object o) {
            return Double.compare(value, ((ColorPoint) o).value);
        }
    }

    private static class InterpolatableColor{
        private final float r, g, b;

        private InterpolatableColor(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public Color interpolate(InterpolatableColor otherColor, float t){
            return new Color(clamp(r * (1 - t) + otherColor.r * t), clamp(g * (1 - t) + otherColor.g * t), clamp(b * (1 - t) + otherColor.b * t));
        }

        public Color toColor(){
            return new Color(r, g, b);
        }
    }

    public BufferObject toBuffer(){
        if(buffer != null) return buffer;

        int floatsNeeded = 4 * colorPoints.size();

        FloatBuffer fb = MemoryUtil.memAllocFloat(floatsNeeded);

        for(ColorPoint point: colorPoints){
            fb.put(point.color.r);
            fb.put(point.color.g);
            fb.put(point.color.b);
            fb.put(point.value);
        }
        fb.flip();

        BufferObject buffer = new BufferObject();
        buffer.data(GL45.GL_SHADER_STORAGE_BUFFER, fb, GL45.GL_STATIC_READ);

        MemoryUtil.memFree(fb);

        this.buffer = buffer;

        return buffer;
    }

    private static float clamp(float f){
        if(f < 0) return 0;
        if(f > 1) return 1;
        return f;
    }

    static {
        DEFAULT.addColorPoint(-1.0f, 0.0f, 0.0f, 0.0f);
        DEFAULT.addColorPoint(1.0f, 1.0f, 1.0f, 1.0f);
        DEFAULT.generate();

        TERRAIN.addColorPoint(-1.0f, 0, 0, 128);
        TERRAIN.addColorPoint(-0.25f, 0, 0, 255);
        TERRAIN.addColorPoint(0, 0, 128, 255);
        TERRAIN.addColorPoint(0.0625f, 240, 240, 64);
        TERRAIN.addColorPoint(0.125f, 32, 160, 0);
        TERRAIN.addColorPoint(0.475f, 224, 224, 0);
        TERRAIN.addColorPoint(0.75f, 128, 128, 128);
        TERRAIN.addColorPoint(1.0f, 255, 255, 255);
        TERRAIN.generate();

        MOUNTAIN.addColorPoint(-1.0f, 32, 160, 0);
        MOUNTAIN.addColorPoint(-0.25f, 224, 224, 0);
        MOUNTAIN.addColorPoint(0.25f, 128, 128, 128);
        MOUNTAIN.addColorPoint(1.0f, 255, 255, 255);
        MOUNTAIN.generate();
    }
}
