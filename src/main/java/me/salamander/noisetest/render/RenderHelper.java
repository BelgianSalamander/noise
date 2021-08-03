package me.salamander.noisetest.render;

import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.render.api.BufferObject;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.awt.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL45.*;

public class RenderHelper {
    public static BufferObject createBufferFromHeightmap(double[][] heightmap, float heightScale, ColorSampler sampler){
        float[] data = createBufferDataFromHeightmap(heightmap, heightScale, sampler);
        BufferObject buffer = new BufferObject();
        buffer.data(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        return buffer;
    }

    public static float[] createBufferDataFromHeightmap(double[][] heightmap, float heightScale, ColorSampler sampler){
        int width = heightmap.length;
        int height = heightmap[0].length;
        float[] data = new float[width * height * 7];
        int i = 0;
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

        return data;
    }

    public static float[] createBufferDataFromHeightmap(double[][] heightmap, float heightScale, Color[][] colors){
        if(heightmap.length != colors.length || heightmap[0].length != colors[0].length){
            throw new IllegalArgumentException("Heightmap and colors have different dimension");
        }

        int width = heightmap.length;
        int height = heightmap[0].length;

        float[] data = new float[width * height * 7];
        int i = 0;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                double heightAtPoint = heightmap[x][y] * heightScale;
                Color colorAtPoint = colors[x][y];

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
        return data;
    }

    @NotNull
    public static double[][] generateNoise(@NotNull NoiseModule module, int width, int height, double step){
        double[][] out = new double[width][height];
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                out[x][y] = module.sample(x * step,y * step);
            }
        }
        return out;
    }

    /*public static double[][] generateNoiseConcurrent(@NotNull NoiseModule module, int width, int height, double step){
        return Stream.generate(new Supplier<Integer>() {
            int y = 0;

            @Override
            public Integer get() {
                return y++;
            }
        }).parallel().map(y -> {
            return Stream.generate(new Supplier<Double>(){
                int x = 0;
                @Override
                public Double get() {
                    return module.sample((x++) * step, y * step);
                }
            }).mapToDouble(i -> i).toArray();
        }).toArray(double[][]::new);*/
}
