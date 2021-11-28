package me.salamander.ourea.render.opengl;

import me.salamander.ourea.color.ColorGradient;
import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.render.Camera;
import me.salamander.ourea.render.FirstClicked;
import me.salamander.ourea.render.Window;
import me.salamander.ourea.util.Pos;
import me.salamander.ourea.util.PosMap;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.NVFragmentCoverageToColor;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.glfw.GLFW.*;

public abstract class OpenGL2DRenderer {
    private static final int HEIGHT_BYTES = 4;
    private static final int NORMAL_BYTES = 12;

    protected final int chunkSize;
    protected final float step;
    protected long seed;

    protected final NoiseSampler noiseSampler;
    protected final ColorGradient gradient;

    protected final ColorMode colorMode;

    protected int ebo;
    protected int xzBuffer;

    private Window window;
    private Camera camera;
    private int program;

    private int u_modelView;
    private int u_projection;
    private int u_heightScale;
    private int u_invHeightScale;

    private int viewDistance = 1;

    private boolean regenerateChunks = false;

    private final Map<Pos, TerrainChunk> chunks = new HashMap<>();

    private final int bytesPerChunk;

    private final Random random = new Random();

    private FirstClicked regenerateSeed;

    public OpenGL2DRenderer(int chunkSize, float step, long seed, ColorMode colorMode, ColorGradient gradient, NoiseSampler sampler){
        this.chunkSize = chunkSize;
        this.colorMode = colorMode;
        this.noiseSampler = sampler;
        this.gradient = gradient;
        this.step = step;
        this.seed = seed;

        final int colorBytesPerChunk = (colorMode.useTexture ? 6 : 4) * 4; // Texture RGBA + UV vs just RGBA
        final int bytesPerVertex = NORMAL_BYTES + HEIGHT_BYTES + colorBytesPerChunk;
        bytesPerChunk = bytesPerVertex * chunkSize * chunkSize;
    }

    /**
     * Initializes the OpenGL context among other things this means that there are some methods that can only called from the same thread as this.
     * @see #mainloop()
     */
    public void init(){
        window = new Window("Ourea", 800, 600);
        regenerateSeed = new FirstClicked(window, GLFW_KEY_Q);

        int kilobytesPerChunk = (int) Math.ceil(bytesPerChunk / 1000.f);

        int kilobytesAvailable = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);

        System.out.println("KB per chunk: " + kilobytesPerChunk);
        System.out.println("KB available: " + kilobytesAvailable);

        int chunksAvailable = kilobytesAvailable / kilobytesPerChunk;
        System.out.println("Chunks available: " + chunksAvailable);
        int maxViewDistance = (int) ((Math.sqrt(chunksAvailable) + 1) * 0.5f);
        System.out.println("Max view distance: " + maxViewDistance);

        camera = new Camera(window);

        ebo = glGenBuffers();
        xzBuffer = glGenBuffers();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBindBuffer(GL_ARRAY_BUFFER, xzBuffer);

        //Generate index buffer
        IntBuffer indices = MemoryUtil.memAllocInt((chunkSize - 1) * (chunkSize - 1) * 6);
        for (int x = 0; x < (chunkSize - 1); x++) {
            for (int z = 0; z < (chunkSize - 1); z++) {
                int baseIndex = z * chunkSize + x;
                indices.put(baseIndex);
                indices.put(baseIndex + chunkSize + 1);
                indices.put(baseIndex + chunkSize);

                indices.put(baseIndex + 1);
                indices.put(baseIndex + chunkSize + 1);
                indices.put(baseIndex);
            }
        }
        indices.flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        MemoryUtil.memFree(indices);

        //Generate xz buffer
        FloatBuffer xz = MemoryUtil.memAllocFloat(chunkSize * chunkSize * 2);
        for(int z = 0; z < chunkSize; z++){
            for(int x = 0; x < chunkSize; x++){
                xz.put(x);
                xz.put(z);
            }
        }
        xz.flip();
        glBufferData(GL_ARRAY_BUFFER, xz, GL_STATIC_DRAW);
        MemoryUtil.memFree(xz);

        createShaders();

        //Set uniform if required
        if(colorMode.useTexture){
            glUniform1i(glGetUniformLocation(program, "tex"), 0);
        }
    }

    public void changeSeed(long seed){
        this.seed = seed;
        regenerateChunks = true;
    }

    protected void regenerateChunks(){
        System.out.println("Regenerating chunks");
        invalidateGeneratingChunks();
        chunks.keySet().forEach(
                pos -> queueChunk(pos.x(), pos.y())
        );
    }

    private void createShaders() {
        String shaderDir = colorMode.useTexture? "/shaders/texture/" : "/shaders/basic/";
        String vertexShaderSource = loadShader( shaderDir + "vert.glsl");
        String fragmentShaderSource = loadShader(shaderDir + "frag.glsl");

        this.program = glCreateProgram();

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if(glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE){
            System.err.println(glGetShaderInfoLog(vertexShader));
            throw new RuntimeException("Failed to compile vertex shader");
        }
        glAttachShader(program, vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if(glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE){
            System.err.println(glGetShaderInfoLog(fragmentShader));
            throw new RuntimeException("Failed to compile fragment shader");
        }
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);
        if(glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE){
            System.err.println(glGetProgramInfoLog(program));
            throw new RuntimeException("Failed to link program");
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        glUseProgram(program);
        u_heightScale = glGetUniformLocation(program, "heightScale");
        u_invHeightScale = glGetUniformLocation(program, "invHeightScale");
        u_modelView = glGetUniformLocation(program, "modelViewMatrix");
        u_projection = glGetUniformLocation(program, "projectionMatrix");

        setHeightScale(20.0f);
    }

    private void setHeightScale(float scale){
        glUniform1f(u_heightScale, scale);
        glUniform1f(u_invHeightScale, 1f / scale);
    }

    private void setModelView(Matrix4f modelView){
        FloatBuffer buffer = MemoryUtil.memAllocFloat(16);
        modelView.get(buffer);
        glUniformMatrix4fv(u_modelView, false, buffer);
    }

    private void setProjectionMatrix(Matrix4f projection){
        FloatBuffer buffer = MemoryUtil.memAllocFloat(16);
        projection.get(buffer);
        glUniformMatrix4fv(u_projection, false, buffer);
    }

    protected abstract void invalidateGeneratingChunks();
    protected abstract void queueChunk(int x, int z);
    protected abstract void postProcess();

    public void mainloop(){
        window.show();

        glUseProgram(program);

        glEnable(GL_DEPTH_TEST);

        long startTime = System.currentTimeMillis() - 166;
        while (!window.shouldClose()){
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - startTime) / 1000f;
            startTime = currentTime;
            float scroll = window.getScroll();

            setHeightScale((float) Math.pow(20.f, scroll * 0.05f + 1.f));

            if(regenerateChunks){
                regenerateChunks();
                regenerateChunks = false;
            }

            trackChunks();

            postProcess();

            trackInput();

            camera.handleInput(window, deltaTime);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Matrix4f viewMatrix = camera.getViewMatrix();

            Matrix4f projectionMatrix = window.getProjectionMatrix();
            setProjectionMatrix(projectionMatrix);

            for(TerrainChunk chunk : chunks.values()){
                chunk.draw(viewMatrix);
            }

            window.swapBuffers();

            glfwPollEvents();
        }

        for(TerrainChunk chunk : chunks.values()){
            chunk.delete();
        }

        delete();

        glfwTerminate();
    }

    private void trackInput() {
        if(regenerateSeed.wasClicked()){
            changeSeed(random.nextLong());
        }
    }

    private void trackChunks() {
        int x = (int) Math.floor(camera.getPosition().z / (chunkSize - 1));
        int z = (int) Math.floor(camera.getPosition().x / (chunkSize - 1));

        List<Pos> toRemove = new ArrayList<>(2);
        chunks.keySet().forEach(key -> {
            if (key.x() < x - viewDistance || key.x() > x + viewDistance || key.y() < z - viewDistance || key.y() > z + viewDistance) {
                toRemove.add(key);
            }
        });
        toRemove.forEach(chunks::remove);

        for (int i = -viewDistance; i <= viewDistance; i++) {
            for (int j = -viewDistance; j <= viewDistance; j++) {
                Pos key = new Pos(x + i, z + j);
                if (!chunks.containsKey(key)) {
                    queueChunk(key.x(), key.y());
                }
            }
        }
    }

    protected void removeChunk(Pos key){
        TerrainChunk chunk = chunks.remove(key);
        if(chunk != null){
            chunk.delete();
        }
    }

    protected abstract void delete();

    protected void putBakedChunk(int x, int z, TerrainChunk chunk){
        TerrainChunk prev = chunks.put(new Pos(x, z), chunk);
        if(prev != null){
            prev.delete();
        }
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    protected class TerrainChunk{
        private static final Matrix4f epicMatrix = new Matrix4f();

        protected final Matrix4f modelMatrix;
        protected final int vao;
        protected final int heightBuffer;
        protected final int colorDataBuffer;
        protected final int normalBuffer;
        protected final int texture;

        public TerrainChunk(float x, float z, int vao, int heightBuffer, int colorDataBuffer, int normalBuffer, int texture){
            this.modelMatrix = new Matrix4f().translate(z, 0, x);
            this.vao = vao;
            this.heightBuffer = heightBuffer;
            this.colorDataBuffer = colorDataBuffer;
            this.texture = texture;
            this.normalBuffer = normalBuffer;
        }

        public void delete(){
            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glDeleteVertexArrays(vao);
            glDeleteBuffers(heightBuffer);
            glDeleteBuffers(colorDataBuffer);
            glDeleteBuffers(normalBuffer);
            if(texture != -1) {
                glDeleteTextures(texture);
            }
        }

        private void draw(Matrix4f viewMatrix){
            setModelView(viewMatrix.mul(modelMatrix, epicMatrix));
            glBindVertexArray(vao);

            if(texture != -1){
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, texture);
            }

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glDrawElements(GL_TRIANGLES, (chunkSize - 1) * (chunkSize - 1) * 6, GL_UNSIGNED_INT, 0);

            //System.out.println("Drawing chunk " + modelMatrix.m30() / 255 + ", " + modelMatrix.m32() / 255);
        }
    }

    private static String loadShader(String path) {
        try {
            InputStream is = OpenGL2DRenderer.class.getResourceAsStream(path);
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();
            return s;
        }catch (IOException e){
            throw new IllegalStateException(e);
        }
    }

    public enum ColorMode{
        SMOOTH(false, true),
        NOT_INTERPOLATED(false, false),
        TEXTURE_SMOOTH(true, true),
        TEXTURE_NOT_INTERPOLATED(true, false);

        protected final boolean useTexture;
        protected final boolean smooth;

        ColorMode(boolean useTexture, boolean smooth){
            this.useTexture = useTexture;
            this.smooth = smooth;
        }
    }
}
