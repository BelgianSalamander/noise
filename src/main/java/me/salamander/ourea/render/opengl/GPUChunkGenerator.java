package me.salamander.ourea.render.opengl;

import me.salamander.ourea.color.ColorGradient;
import me.salamander.ourea.glsl.GLSLCompiler;
import me.salamander.ourea.modules.NoiseSampler;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;

import static org.lwjgl.opengl.GL45.*;


public class GPUChunkGenerator extends OpenGL2DRenderer{
    private int noiseGenerator;
    private final Queue<InterleavedTerrainChunk> chunks = new ArrayDeque<>(16);

    public GPUChunkGenerator(int chunkSize, float step, int seed, ColorMode colorMode, ColorGradient gradient, NoiseSampler sampler) {
        super(chunkSize, step, seed, colorMode, gradient, sampler);
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
            throw new RuntimeException("Couldn't compile shader");
        }

        glAttachShader(noiseGenerator, shader);
        glLinkProgram(noiseGenerator);

        status = glGetProgrami(noiseGenerator, GL_LINK_STATUS);
        if(status == GL_FALSE){
            System.err.println("Couldn't link shader");
            System.err.println(glGetProgramInfoLog(noiseGenerator));
            throw new RuntimeException("Couldn't link shader");
        }

        glDeleteShader(shader);

        glUseProgram(noiseGenerator);

        glUniform1i(glGetUniformLocation(noiseGenerator, "baseSeed"), seed);
        glUniform1ui(glGetUniformLocation(noiseGenerator, "width"), chunkSize);
        glUniform1f(glGetUniformLocation(noiseGenerator, "step"), step);
    }

    @Override
    protected void invalidateGeneratingChunks() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void queueChunk(int x, int z) {
        System.out.println("Queueing chunk " + x + ", " + z);

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, vbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, chunkSize * chunkSize * 32L, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, xzBuffer);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(1, 1, GL_FLOAT, false, 32, 28);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 32, 0);
        glVertexAttribPointer(3, 3, GL_FLOAT, false, 32, 16);

        glUseProgram(noiseGenerator);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, vbo);

        glUniform2f(glGetUniformLocation(noiseGenerator, "startPos"), z * (chunkSize - 1) * step, x * (chunkSize - 1) * step);
        glUniform2f(glGetUniformLocation(noiseGenerator, "offset"), 0, 0);

        System.out.println("Launching (" + (chunkSize / 32) + ", " + (chunkSize / 32) + ") work groups");
        glDispatchCompute(chunkSize / 32, chunkSize / 32, 1);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, vbo);

        /*FloatBuffer fb = MemoryUtil.memAllocFloat(chunkSize * chunkSize * 8);
        for(int i = 0; i < chunkSize * chunkSize; i++){
            fb.put(1); //Color
            fb.put(0);
            fb.put(0);
            fb.put(1);
            fb.put(0); //Normal
            fb.put(1);
            fb.put(0);
            fb.put(0); //Height
        }

        fb.flip();
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, fb);*/

        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        chunks.add(new InterleavedTerrainChunk(x, z, vao, vbo));
        //putBakedChunk(x, z, new InterleavedTerrainChunk(x, z, vao, vbo));
    }

    @Override
    protected void postProcess() {
        InterleavedTerrainChunk chunk = chunks.poll();
        if(chunk != null) {
            putBakedChunk(chunk.x(), chunk.z(), chunk);
        }
    }

    @Override
    protected void delete() {
        glDeleteProgram(noiseGenerator);
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

    public class InterleavedTerrainChunk implements TerrainChunk{
        private static final Matrix4f out = new Matrix4f();

        private final int x;
        private final int z;
        private final int vao;
        private final int vbo;
        private final Matrix4f modelMatrix;

        public InterleavedTerrainChunk(int x, int z, int vao, int vbo){
            this.x = x;
            this.z = z;
            this.vao = vao;
            this.vbo = vbo;

            this.modelMatrix = new Matrix4f().translate(z * (chunkSize - 1), 0, x * (chunkSize - 1));
        }

        @Override
        public int x() {
            return x;
        }

        @Override
        public int z() {
            return z;
        }

        @Override
        public void delete() {
            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glDeleteBuffers(vbo);
            glDeleteVertexArrays(vao);
        }

        @Override
        public void draw(Matrix4f viewMatrix, int indexBuffer) {
            setModelView(viewMatrix.mul(modelMatrix, out));
            glBindVertexArray(vao);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
            glDrawElements(GL_TRIANGLES, (chunkSize - 1) * (chunkSize - 1) * 6, GL_UNSIGNED_INT, 0);
        }
    }
}
