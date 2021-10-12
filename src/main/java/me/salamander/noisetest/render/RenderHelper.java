package me.salamander.noisetest.render;

import me.salamander.noisetest.NoiseTest;
import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.color.ColorSampler;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.glsl.GLSLTranspiler;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.render.api.BufferObject;
import me.salamander.noisetest.render.api.ComputeShader;
import me.salamander.noisetest.render.api.Window;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL45.*;

public class RenderHelper {
    private static ComputeShader normalProgram;

    public static BufferObject createBufferFromHeightmap(float[][] heightmap, float heightScale, ColorSampler sampler){
        float[] data = createBufferDataFromHeightmap(heightmap, heightScale, sampler);
        BufferObject buffer = new BufferObject();
        buffer.data(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        return buffer;
    }

    public static BufferObject createBufferOnGPU(GLSLCompilable noiseModule, int width, int height, float heightScale, ColorGradient sampler){
        //Step One: create GLSL program for noiseModule

        String source = GLSLTranspiler.compileModule(noiseModule);

        try {
            FileOutputStream fout = new FileOutputStream("run/out.glsl");
            fout.write(source.getBytes(StandardCharsets.UTF_8));
            fout.close();
        } catch (IOException e) {
            throw new IllegalStateException("Could not create FileOutputStream", e);
        }

        ComputeShader noiseProgram = new ComputeShader(source);

        //Create buffer for program
        BufferObject heightmapDataSSBO = new BufferObject();
        heightmapDataSSBO.allocate(GL_SHADER_STORAGE_BUFFER, 32L * width * height, GL_DYNAMIC_DRAW);
        heightmapDataSSBO.bindBase(GL_SHADER_STORAGE_BUFFER, 3);

        //Set required uniforms
        noiseProgram.setUniform("baseSeed", (int) noiseModule.getSeed());
        noiseProgram.setUniform("startPos", new Vector2f(0, 0));
        noiseProgram.setUniform("step", 0.01f);
        noiseProgram.setUniformUnsignedInt("width", width);

        //Run program!
        noiseProgram.run(width / 32, height / 32, 1);

        //Compute normal data
        createNormalProgram();

        sampler.toBuffer().bindBase(GL_SHADER_STORAGE_BUFFER, 2);

        noiseProgram.setUniformUnsignedInt("amountPoints", sampler.numPoints());
        noiseProgram.setUniformUnsignedInt("tileWidth", width);
        noiseProgram.setUniformUnsignedInt("tileHeight", height);
        noiseProgram.setUniform("heightScale", heightScale);

        normalProgram.run(width / 32, height / 32, 1);

        return heightmapDataSSBO;
    }

    private static void createNormalProgram() {
        if(normalProgram != null) return;

        try {
            InputStream fin = NoiseTest.class.getResourceAsStream("/shaders/heightmap/normals.glsl");

            String code = new String(fin.readAllBytes(), StandardCharsets.UTF_8);

            fin.close();

            normalProgram = new ComputeShader(code);
        }catch (IOException e){
            throw new IllegalStateException("Could not create normal program");
        }
    }

    public static float[] createBufferDataFromHeightmap(float[][] heightmap, float heightScale, ColorSampler sampler){
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

                //Experimental: Diagonal vectors
                Vector3f vectorNorthEast = null, vectorNorthWest = null, vectorSouthEast = null, vectorSouthWest = null;

                if(x != 0 && y != 0){
                    vectorSouthWest = new Vector3f(-1, (float) (heightmap[x-1][y-1] * heightScale - heightAtPoint), -1);
                }

                if(x != 0 && y != height - 1){
                    vectorNorthWest = new Vector3f(-1, (float) (heightmap[x-1][y+1] * heightScale - heightAtPoint), 1);
                }

                if(x != width - 1 && y != 0){
                    vectorSouthEast = new Vector3f(1, (float) (heightmap[x+1][y-1] * heightScale - heightAtPoint), -1);
                }

                if(x != width - 1 && y != width - 1){
                    vectorNorthEast = new Vector3f(1, (float) (heightmap[x+1][y+1] * heightScale - heightAtPoint), 1);
                }

                Vector3f[] v1 = new Vector3f[]{vertexNorth, vertexEast, vertexSouth, vertexWest, vectorNorthEast, vectorSouthEast, vectorSouthWest, vectorNorthWest};
                Vector3f[] v2 = new Vector3f[]{vertexEast, vertexSouth, vertexWest, vertexNorth, vectorSouthEast, vectorSouthWest, vectorNorthWest, vectorNorthEast};

                for(int index = 0; index < 8; index++){
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

    public static float[] createBufferDataFromHeightmap(float[][] heightmap, float heightScale, Color[][] colors){
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

                //Experimental: Diagonal vectors
                Vector3f vectorNorthEast = null, vectorNorthWest = null, vectorSouthEast = null, vectorSouthWest = null;

                if(x != 0 && y != 0){
                    vectorSouthWest = new Vector3f(-1, (float) (heightmap[x-1][y-1] * heightScale - heightAtPoint), -1);
                }

                if(x != 0 && y != height - 1){
                    vectorNorthWest = new Vector3f(-1, (float) (heightmap[x-1][y+1] * heightScale - heightAtPoint), 1);
                }

                if(x != width - 1 && y != 0){
                    vectorSouthEast = new Vector3f(1, (float) (heightmap[x+1][y-1] * heightScale - heightAtPoint), -1);
                }

                if(x != width - 1 && y != width - 1){
                    vectorNorthEast = new Vector3f(1, (float) (heightmap[x+1][y+1] * heightScale - heightAtPoint), 1);
                }

                Vector3f[] v1 = new Vector3f[]{vertexNorth, vertexEast, vertexSouth, vertexWest, vectorNorthEast, vectorSouthEast, vectorSouthWest, vectorNorthWest};
                Vector3f[] v2 = new Vector3f[]{vertexEast, vertexSouth, vertexWest, vertexNorth, vectorSouthEast, vectorSouthWest, vectorNorthWest, vectorNorthEast};

                for(int index = 0; index < 8; index++){
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
    public static float[][] generateNoise(@NotNull NoiseModule module, int width, int height, float step){
        float[][] out = new float[width][height];
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
