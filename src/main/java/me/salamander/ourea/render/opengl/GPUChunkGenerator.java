package me.salamander.ourea.render.opengl;

import me.salamander.ourea.color.ColorGradient;
import me.salamander.ourea.glsl.GLSLCompiler;
import me.salamander.ourea.modules.NoiseSampler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL45.*;


public class GPUChunkGenerator extends OpenGL2DRenderer{
    private int noiseGenerator;

    public GPUChunkGenerator(int chunkSize, float step, int seed, ColorMode colorMode, ColorGradient gradient, NoiseSampler sampler) {
        super(chunkSize, step, seed, colorMode, gradient, sampler);
    }

    @Override
    protected void invalidateGeneratingChunks() {

    }

    @Override
    protected void queueChunk(int x, int z) {

    }

    @Override
    protected void postProcess() {

    }

    @Override
    protected void delete() {

    }

    private String generateSource(NoiseSampler sampler) {
        GLSLCompiler compiler = new GLSLCompiler(sampler, 2);
        compiler.compileMethods();
        String source = compiler.link();

        try{
            Path out = Path.of("run", "generated.glsl");
            out.getParent().toFile().mkdirs();
            out.toFile().createNewFile();
            Files.write(out, source.getBytes());
        }catch (IOException e){
            System.err.println("Couldn't write generated.glsl");
            e.printStackTrace();
        }

        return source;
    }

    @Override
    protected void initialize() {

        noiseGenerator = glCreateProgram();

        String source = generateSource(this.noiseSampler);
        int shader = glCreateShader(GL_COMPUTE_SHADER);
        glShaderSource(shader, source);
        glCompileShader(shader);

        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if(status == GL_FALSE){
            System.err.println("Couldn't compile shader");
            System.err.println(glGetShaderInfoLog(shader));
        }

        glAttachShader(noiseGenerator, shader);
        glLinkProgram(noiseGenerator);

        status = glGetProgrami(noiseGenerator, GL_LINK_STATUS);
        if(status == GL_FALSE){
            System.err.println("Couldn't link shader");
            System.err.println(glGetProgramInfoLog(noiseGenerator));
        }

        glDeleteShader(shader);
    }
}
