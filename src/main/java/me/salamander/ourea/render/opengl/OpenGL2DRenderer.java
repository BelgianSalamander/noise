package me.salamander.ourea.render.opengl;

import me.salamander.ourea.color.ColorGradient;
import me.salamander.ourea.modules.NoiseSampler;
import me.salamander.ourea.render.Camera;
import me.salamander.ourea.render.FirstClicked;
import me.salamander.ourea.render.Window;
import me.salamander.ourea.util.Pos;
import org.joml.Matrix4f;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.glfw.GLFW.*;

public abstract class OpenGL2DRenderer<T extends TerrainChunk> {
    private static final int HEIGHT_BYTES = 4;
    private static final int NORMAL_BYTES = 12;
    protected static final int[] LOD_STEPS = {1, 2, 4, 8, 16, 32};

    protected final int chunkSize;
    protected final float step;
    protected int seed;

    protected final NoiseSampler noiseSampler;
    protected final ColorGradient gradient;

    protected final ColorMode colorMode;

    protected int[] lodIndices;
    protected int xzBuffer;

    protected Window window;
    private Camera camera;
    private int program;

    private int u_modelView;
    private int u_projection;
    private int u_heightScale;
    private int u_invHeightScale;

    private int viewDistance = 1;

    private boolean regenerateChunks = false;

    private final Map<Pos, T> chunks = new HashMap<>();
    private final Set<Pos> queuedChunks = new HashSet<>();

    private final int bytesPerChunk;

    private final Random random = new Random();

    private FirstClicked regenerateSeed;

    public OpenGL2DRenderer(int chunkSize, float step, int seed, ColorMode colorMode, ColorGradient gradient, NoiseSampler sampler){
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
    public final void init(){
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

        xzBuffer = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, xzBuffer);

        generateIndices();

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

        initialize();
    }

    private void generateIndices() {
        lodIndices = new int[LOD_STEPS.length];

        for (int i = 0; i < lodIndices.length; i++) {
            lodIndices[i] = generateIndicesForStep(LOD_STEPS[i]);
        }
    }

    private int generateIndicesForStep(int lodStep) {
        int neededForEdge = 4 * (chunkSize - 1);

        int insideLength = chunkSize - 2;
        int insidePointsOnSide = insideLength / lodStep;

        int neededForInnerEdge = 4 * (insidePointsOnSide - 1);
        int usedInside = 2 * (insidePointsOnSide - 1) * (insidePointsOnSide - 1);

        int extraSpace = lodStep - 1;
        int shift = 1 + extraSpace / 2;

        int end = shift + insidePointsOnSide * lodStep;

        IntBuffer indices = MemoryUtil.memAllocInt((usedInside + neededForEdge + neededForInnerEdge) * 3);

        for(int y = shift; y < end - lodStep; y += lodStep){
            for(int x = shift; x < end - lodStep; x += lodStep){
                int baseIndex = (y * chunkSize + x);

                indices.put(baseIndex);
                indices.put(baseIndex + lodStep);
                indices.put(baseIndex + chunkSize * lodStep);
                indices.put(baseIndex + chunkSize * lodStep);
                indices.put(baseIndex + lodStep);
                indices.put(baseIndex + chunkSize * lodStep + lodStep);
            }
        }

        //Y edge
        for(int y = 0; y < chunkSize - 1; y++){
            int baseIndex = y * chunkSize;

            float modifiedPos = ((float) y) / lodStep;


            int connectTo = (int) (Math.ceil(modifiedPos) - 1);
            if(connectTo < 0) connectTo = 0;
            else if(connectTo >= insidePointsOnSide) connectTo = insidePointsOnSide - 1;

            int connectToY = shift + connectTo * lodStep;
            int connectToX = shift;

            indices.put(baseIndex);
            indices.put(connectToY * chunkSize + connectToX);
            indices.put(baseIndex + chunkSize);

            int xShift = chunkSize - 1;
            connectToX = end - lodStep;

            indices.put(baseIndex + xShift);
            indices.put(baseIndex + chunkSize + xShift);
            indices.put(connectToY * chunkSize + connectToX);
        }

        //X edge
        for(int x = 0; x < chunkSize - 1; x++){
            int baseIndex = x;
            float modifiedPos = ((float) x) / lodStep;


            int connectTo = (int) (Math.ceil(modifiedPos) - 1);
            if(connectTo < 0) connectTo = 0;
            else if(connectTo >= insidePointsOnSide) connectTo = insidePointsOnSide - 1;

            int connectToX = shift + connectTo * lodStep;
            int connectToY = shift;

            indices.put(baseIndex);
            indices.put(baseIndex + 1);
            indices.put(connectToY * chunkSize + connectToX);

            int yShift = (chunkSize - 1) * chunkSize;
            connectToY = end - lodStep;

            indices.put(baseIndex + yShift);
            indices.put(connectToX + connectToY * chunkSize);
            indices.put(baseIndex + 1 + yShift);
        }

        //Inner Y Edge
        for(int y = shift; y < end - lodStep; y += lodStep){
            int nextY = y + lodStep;

            int connectToY = Math.round((y + nextY) / 2.f) + 1;

            if(lodStep == 1) connectToY--;

            int x = shift;

            indices.put(y * chunkSize + x);
            indices.put(nextY * chunkSize + x);
            indices.put(connectToY * chunkSize);

            x = end - lodStep;

            indices.put(y * chunkSize + x);
            indices.put(connectToY * chunkSize + chunkSize - 1);
            indices.put(nextY * chunkSize + x);
        }

        //Inner X Edge
        for(int x = shift; x < end - lodStep; x += lodStep){
            int nextX = x + lodStep;

            int connectToX = Math.round((x + nextX) / 2.f) + 1;

            if(lodStep == 1) connectToX--;

            int y = shift;

            indices.put(y * chunkSize + x);
            indices.put(connectToX);
            indices.put(y * chunkSize + nextX);

            y = end - lodStep;
            int yShift = (chunkSize - 1) * chunkSize;

            indices.put(y * chunkSize + x);
            indices.put(y * chunkSize + nextX);
            indices.put(connectToX + yShift);
        }

        indices.flip();

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        MemoryUtil.memFree(indices);

        return ebo;
    }

    public void changeSeed(int seed){
        this.seed = seed;
        regenerateChunks = true;
    }

    protected void regenerateChunks(){
        System.out.println("Regenerating chunks");
        invalidateGeneratingChunks();
        chunks.keySet().forEach(
                pos -> queueChunkInternal(pos.x(), pos.y())
        );
    }

    private void queueChunkInternal(int x, int y){
        Pos pos = new Pos(x, y);
        if(!queuedChunks.contains(pos)){
            queuedChunks.add(pos);
            queueChunk(x, y);
        }
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

    protected void setModelView(Matrix4f modelView){
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
            glUseProgram(program);

            Matrix4f viewMatrix = camera.getViewMatrix();

            Matrix4f projectionMatrix = window.getProjectionMatrix();
            setProjectionMatrix(projectionMatrix);

            int x = (int) Math.floor(camera.getPosition().x / (chunkSize - 1));
            int z = (int) Math.floor(camera.getPosition().z / (chunkSize - 1));

            for(TerrainChunk chunk : chunks.values()){
                int LOD = Math.abs(x - chunk.x()) + Math.abs(z - chunk.z()) / 2;
                if(LOD >= lodIndices.length){
                    LOD = lodIndices.length - 1;
                }
                chunk.draw(viewMatrix, lodIndices[LOD]);
            }

            window.swapBuffers();

            glfwPollEvents();
        }

        close();
    }

    public void close(){
        for(TerrainChunk chunk : chunks.values()){
            chunk.delete();
        }

        delete();

        glfwTerminate();
    }

    private void trackInput() {
        if(regenerateSeed.wasClicked()){
            changeSeed(random.nextInt());
        }
    }

    private void trackChunks() {
        int x = (int) Math.floor(camera.getPosition().x / (chunkSize - 1));
        int z = (int) Math.floor(camera.getPosition().z / (chunkSize - 1));

        Set<Pos> toRemove = new HashSet<>(2);
        chunks.keySet().forEach(key -> {
            if (key.x() < x - viewDistance || key.x() > x + viewDistance || key.y() < z - viewDistance || key.y() > z + viewDistance) {
                toRemove.add(key);
            }
        });

        for (int i = -viewDistance; i <= viewDistance; i++) {
            for (int j = -viewDistance; j <= viewDistance; j++) {
                Pos key = new Pos(x + i, z + j);
                if (!chunks.containsKey(key)) {
                    queueChunkInternal(key.x(), key.y());
                }
                toRemove.remove(key);
            }
        }

        toRemove.forEach(this::removeChunk);
    }

    protected void removeChunk(Pos key){
        TerrainChunk chunk = chunks.remove(key);
        if(chunk != null){
            chunk.delete();
        }
    }

    protected void initialize(){

    }

    protected abstract void delete();

    protected void putBakedChunk(int x, int z, T chunk){
        System.out.println("Baked chunk at " + x + ", " + z);
        Pos pos = new Pos(x, z);
        queuedChunks.remove(pos);
        TerrainChunk prev = chunks.put(pos, chunk);
        if(prev != null){
            prev.delete();
        }
    }

    public T getChunk(int x, int z){
        return chunks.get(new Pos(x, z));
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    protected class SplitTerrainChunk implements TerrainChunk{
        private static final Matrix4f epicMatrix = new Matrix4f();

        protected final int x, z;
        protected final Matrix4f modelMatrix;
        protected final int vao;
        protected final int heightBuffer;
        protected final int colorDataBuffer;
        protected final int normalBuffer;
        protected final int texture;

        public SplitTerrainChunk(float x, float z, int vao, int heightBuffer, int colorDataBuffer, int normalBuffer, int texture){
            this.x = (int) x / (chunkSize - 1);
            this.z = (int) z / (chunkSize - 1);
            this.modelMatrix = new Matrix4f().translate(z, 0, x);
            this.vao = vao;
            this.heightBuffer = heightBuffer;
            this.colorDataBuffer = colorDataBuffer;
            this.texture = texture;
            this.normalBuffer = normalBuffer;
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

        @Override
        public void draw(Matrix4f viewMatrix, int lod){
            setModelView(viewMatrix.mul(modelMatrix, epicMatrix));
            glBindVertexArray(vao);

            if(texture != -1){
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, texture);
            }

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lod);
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
