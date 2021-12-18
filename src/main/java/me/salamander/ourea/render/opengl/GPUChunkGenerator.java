package me.salamander.ourea.render.opengl;

import me.salamander.ourea.color.ColorGradient;
import me.salamander.ourea.glsl.GLSLCompiler;
import me.salamander.ourea.modules.NoiseSampler;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.lwjgl.opengl.GL45.*;


public class GPUChunkGenerator extends OpenGL2DRenderer<GPUChunkGenerator.InterleavedTerrainChunk> {
    private static final boolean MEND_NORMALS = true;

    private int noiseGenerator;
    private int normalGenerator;

    private final Queue<InterleavedTerrainChunk> computingHeight = new ArrayDeque<>(16);
    private final Queue<InterleavedTerrainChunk> computingNormals = new ArrayDeque<>(16);

    private int gradientBuffer;

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
            throw new RuntimeException("Couldn't compile noise shader");
        }

        glAttachShader(noiseGenerator, shader);
        glLinkProgram(noiseGenerator);

        status = glGetProgrami(noiseGenerator, GL_LINK_STATUS);
        if(status == GL_FALSE){
            System.err.println("Couldn't link shader");
            System.err.println(glGetProgramInfoLog(noiseGenerator));
            throw new RuntimeException("Couldn't link noise shader");
        }

        glDeleteShader(shader);

        glUseProgram(noiseGenerator);

        glUniform1i(glGetUniformLocation(noiseGenerator, "baseSeed"), seed);
        glUniform1ui(glGetUniformLocation(noiseGenerator, "width"), chunkSize);
        glUniform1f(glGetUniformLocation(noiseGenerator, "step"), step);

        source = getNormalSource();
        shader = glCreateShader(GL_COMPUTE_SHADER);
        glShaderSource(shader, source);
        glCompileShader(shader);

        status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if(status == GL_FALSE){
            System.err.println("Couldn't compile shader");
            System.err.println(glGetShaderInfoLog(shader));
            throw new RuntimeException("Couldn't compile normal shader");
        }

        normalGenerator = glCreateProgram();
        glAttachShader(normalGenerator, shader);
        glLinkProgram(normalGenerator);

        status = glGetProgrami(normalGenerator, GL_LINK_STATUS);
        if(status == GL_FALSE){
            System.err.println("Couldn't link shader");
            System.err.println(glGetProgramInfoLog(normalGenerator));
            throw new RuntimeException("Couldn't link normal shader");
        }

        glDeleteShader(shader);

        glUseProgram(normalGenerator);

        glUniform1ui(glGetUniformLocation(normalGenerator, "tileWidth"), chunkSize);
        glUniform1ui(glGetUniformLocation(normalGenerator, "tileHeight"), chunkSize);
        glUniform1ui(glGetUniformLocation(normalGenerator, "amountPoints"), gradient.size());
        glUniform2f(glGetUniformLocation(normalGenerator, "offset"), 0, 0);

        gradientBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, gradientBuffer);
        FloatBuffer buffer = gradient.writeSelf();
        glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        MemoryUtil.memFree(buffer);
    }

    public long profile(int x, int y){
        int query = glGenQueries();
        glBeginQuery(GL_TIME_ELAPSED, query);

        List<InterleavedTerrainChunk> chunks = new ArrayList<>();

        for(int xi = 0; xi < x; xi++){
            for(int yi = 0; yi < y; yi++){
                chunks.add(launchHeightGenerator(xi, yi));
            }
        }

        glEndQuery(GL_TIME_ELAPSED);

        long[] time = new long[1];

        glGetQueryObjectui64v(query, GL_QUERY_RESULT, time);

        glDeleteQueries(query);

        chunks.forEach(TerrainChunk::delete);

        return time[0];
    }

    @Override
    protected void invalidateGeneratingChunks() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void queueChunk(int x, int z) {
        System.out.println("Queueing chunk " + x + ", " + z);
        computingHeight.add(launchHeightGenerator(x, z));
        //putBakedChunk(x, z, new InterleavedTerrainChunk(x, z, vao, vbo));
    }

    private InterleavedTerrainChunk launchHeightGenerator(int x, int z){
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

        glUniform2f(glGetUniformLocation(noiseGenerator, "startPos"), x * (chunkSize - 1) * step, z * (chunkSize - 1) * step);
        glUniform2f(glGetUniformLocation(noiseGenerator, "offset"), 0, 0);

        //System.out.println("Launching (" + (chunkSize / 32) + ", " + (chunkSize / 32) + ") work groups");
        glDispatchCompute(chunkSize / 32, chunkSize / 32, 1);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, vbo);

        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        return new InterleavedTerrainChunk(x, z, vao, vbo);
    }

    @Override
    protected void postProcess() {
        InterleavedTerrainChunk chunk = computingHeight.poll();
        if(chunk != null) {
            chunk.completedHeight = true;
            queueNormals(chunk);
        }

        InterleavedTerrainChunk normalChunk = computingNormals.poll();
        if(normalChunk != null) {
            putBakedChunk(normalChunk.x, normalChunk.z, normalChunk);
        }

        //This is quite hacky
        glUseProgram(normalGenerator);
        glUniform1i(glGetUniformLocation(normalGenerator, "debug"), window.isKeyPressed(GLFW.GLFW_KEY_Z) ? 1 : 0);
    }

    @Override
    protected void delete() {
        glDeleteProgram(noiseGenerator);
        glDeleteBuffers(gradientBuffer);
    }

    private void queueNormals(InterleavedTerrainChunk chunk){
        glUseProgram(normalGenerator);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, chunk.vbo);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, gradientBuffer);

        if(MEND_NORMALS){
            InterleavedTerrainChunk negativeXChunk = getChunk(chunk.x - 1, chunk.z);
            InterleavedTerrainChunk positiveXChunk = getChunk(chunk.x + 1, chunk.z);
            InterleavedTerrainChunk negativeZChunk = getChunk(chunk.x, chunk.z + 1);
            InterleavedTerrainChunk positiveZChunk = getChunk(chunk.x, chunk.z - 1);

            boolean hasNegativeX = negativeXChunk != null;
            boolean hasPositiveX = positiveXChunk != null;
            boolean hasNegativeZ = negativeZChunk != null;
            boolean hasPositiveZ = positiveZChunk != null;

            if(hasNegativeX){
                hasNegativeX = negativeXChunk.completedHeight;
            }

            if(hasPositiveX){
                hasPositiveX = positiveXChunk.completedHeight;
            }

            if(hasNegativeZ){
                hasNegativeZ = negativeZChunk.completedHeight;
            }

            if(hasPositiveZ){
                hasPositiveZ = positiveZChunk.completedHeight;
            }

            glUniform1i(glGetUniformLocation(normalGenerator, "hasNegativeXData"), hasNegativeX ? 1 : 0);
            glUniform1i(glGetUniformLocation(normalGenerator, "hasPositiveXData"), hasPositiveX ? 1 : 0);
            glUniform1i(glGetUniformLocation(normalGenerator, "hasNegativeYData"), hasNegativeZ ? 1 : 0);
            glUniform1i(glGetUniformLocation(normalGenerator, "hasPositiveYData"), hasPositiveZ ? 1 : 0);

            if(hasPositiveX){
                glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, positiveXChunk.vbo);
            }

            if(hasNegativeX){
                glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, negativeXChunk.vbo);
            }

            if(hasPositiveZ){
                glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, positiveZChunk.vbo);
            }

            if(hasNegativeZ){
                glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 7, negativeZChunk.vbo);
            }
        }

        glDispatchCompute(chunkSize / 32, chunkSize / 32, 1);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, chunk.vbo);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        computingNormals.add(chunk);
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

    private String getNormalSource(){
        try{
            return new String(GPUChunkGenerator.class.getResourceAsStream("/shaders/heightmap/normals.glsl").readAllBytes());
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public class InterleavedTerrainChunk implements TerrainChunk{
        private static final Matrix4f out = new Matrix4f();

        private boolean completedHeight = false;

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

            this.modelMatrix = new Matrix4f().translate(x * (chunkSize - 1), 0, z * (chunkSize - 1));
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
