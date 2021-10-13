package me.salamander.noisetest.render;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.glsl.GLSLTranspiler;
import me.salamander.noisetest.render.api.*;
import me.salamander.noisetest.util.Pair;
import me.salamander.noisetest.util.Util;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;

public class HeightMapGenerator {
    private final Window window;
    private final Camera camera;
    private final TimeMeasurer dtGetter;

    private final int tileSize;
    private float step = 0.02f;
    private final Map<Pair<Integer, Integer>, TileInfo> tiles = new HashMap<>(); //Stores VAO of tiles;

    private final BufferObject sampler;
    private final BufferObject xzBuffer;

    private final ComputeShader heightProgram;
    private final ComputeShader normalAndColorProgram;

    private final BufferObject[] detailLevels = new BufferObject[LOD_LEVELS];

    private final ShaderProgram renderingShader;
    private final int doDiffuseLocation, modelViewMatrixLocation, projectionMatrixLocation, lightDirectionLocation;

    private final Random random = new Random();

    private final Queue<Runnable> scheduled = new PriorityQueue<>(Comparator.comparingInt(runnable -> random.nextInt()));

    private static final int VIEW_DISTANCE = 2;
    private static final int BATCH_SIZE = 512;
    private static final int STEPS_PER_FRAME = 5;
    private static final int MAX_CACHED_TILES = 150;
    private static final float DISTANCE_BETWEEN = 1;
    private static final int LOD_LEVELS = 6;
    private static final boolean MEND_NORMALS = true;

    public HeightMapGenerator(int tileSize, GLSLCompilable compilable, ColorGradient sampler){
        window = new Window("Noise", 500, 500);
        camera = new Camera(window, 0, 25, 0);
        dtGetter = new TimeMeasurer();

        this.tileSize = tileSize;

        String source = GLSLTranspiler.compileModule(compilable, "/run/out.glsl");
        this.heightProgram = new ComputeShader(source);
        heightProgram.bind();
        heightProgram.setUniform("step", step);
        heightProgram.setUniformUnsignedInt("width", tileSize);
        setSeed((int) compilable.getSeed());

        try {
            this.normalAndColorProgram = new ComputeShader(Util.loadResource("/shaders/heightmap/normals.glsl"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load normal program");
        }

        normalAndColorProgram.bind();
        System.out.println("Noise and color program handle: " + normalAndColorProgram.getHandle());
        normalAndColorProgram.setUniformUnsignedInt("amountPoints", sampler.numPoints());
        normalAndColorProgram.setUniformUnsignedInt("tileWidth", tileSize);
        normalAndColorProgram.setUniformUnsignedInt("tileHeight", tileSize);
        normalAndColorProgram.setUniform("heightScale", 20.f);
        //glUseProgram(0);

        this.sampler = sampler.toBuffer();

        this.xzBuffer = new BufferObject();

        FloatBuffer xzData = MemoryUtil.memAllocFloat(tileSize * tileSize * 2);

        for(int y = 0; y < tileSize; y++){
            for (int x = 0; x < tileSize; x++) {
                xzData.put(x * DISTANCE_BETWEEN);
                xzData.put(y * DISTANCE_BETWEEN);
            }
        }

        xzData.flip();
        xzBuffer.data(GL_ARRAY_BUFFER, xzData, GL_STATIC_DRAW);

        MemoryUtil.memFree(xzData);

        renderingShader = new ShaderProgram(
                GLUtil.loadResource("shaders/heightmap/vert.glsl"),
                GLUtil.loadResource("shaders/heightmap/frag.glsl")
        );
        renderingShader.bind();

        doDiffuseLocation = renderingShader.getUniformLocation("doDiffuse");
        modelViewMatrixLocation = renderingShader.getUniformLocation("modelViewMatrix");
        projectionMatrixLocation = renderingShader.getUniformLocation("projectionMatrix");
        lightDirectionLocation = renderingShader.getUniformLocation("lightDirection");

        glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        renderingShader.setUniform(doDiffuseLocation, true);

        for (int i = 0; i < detailLevels.length; i++) {
            detailLevels[i] = generateIndexBufferForLevel(i);
        }
    }

    BufferObject generateIndexBufferForLevel(int level){
        int neededForEdge = 4 * (tileSize - 1);

        int step = 1;
        for(int i = 0; i < level; i++) step *= 2;

        int insideLength = tileSize - 2;
        int insidePointsOnSide = insideLength / step;

        int neededForInnerEdge = 4 * (insidePointsOnSide - 1);
        int usedInside = 2 * (insidePointsOnSide - 1) * (insidePointsOnSide - 1);

        int extraSpace = step - 1;
        int shift = 1 + extraSpace / 2;

        int end = shift + insidePointsOnSide * step;

        IntBuffer indices = MemoryUtil.memAllocInt((usedInside + neededForEdge + neededForInnerEdge) * 3);

        //For Now, only inner;
        for(int x = shift; x < end - step; x += step){
            for(int y = shift; y < end - step; y += step){
                int baseIndex = (y * tileSize + x);

                indices.put(baseIndex);
                indices.put(baseIndex + step);
                indices.put(baseIndex + tileSize * step);
                indices.put(baseIndex + tileSize * step);
                indices.put(baseIndex + step);
                indices.put(baseIndex + tileSize * step + step);
            }
        }

        //Y edge
        for(int y = 0; y < tileSize - 1; y++){
            int baseIndex = y * tileSize;

            float modifiedPos = ((float) y) / step;


            int connectTo = (int) (Math.ceil(modifiedPos) - 1);
            if(connectTo < 0) connectTo = 0;
            else if(connectTo >= insidePointsOnSide) connectTo = insidePointsOnSide - 1;

            int connectToY = shift + connectTo * step;
            int connectToX = shift;

            indices.put(baseIndex);
            indices.put(connectToY * tileSize + connectToX);
            indices.put(baseIndex + tileSize);

            int xShift = tileSize - 1;
            connectToX = end - step;

            indices.put(baseIndex + xShift);
            indices.put(baseIndex + tileSize + xShift);
            indices.put(connectToY * tileSize + connectToX);
        }

        //X edge
        for(int x = 0; x < tileSize - 1; x++){
            int baseIndex = x;
            float modifiedPos = ((float) x) / step;


            int connectTo = (int) (Math.ceil(modifiedPos) - 1);
            if(connectTo < 0) connectTo = 0;
            else if(connectTo >= insidePointsOnSide) connectTo = insidePointsOnSide - 1;

            int connectToX = shift + connectTo * step;
            int connectToY = shift;

            indices.put(baseIndex);
            indices.put(baseIndex + 1);
            indices.put(connectToY * tileSize + connectToX);

            int yShift = (tileSize - 1) * tileSize;
            connectToY = end - step;

            indices.put(baseIndex + yShift);
            indices.put(connectToX + connectToY * tileSize);
            indices.put(baseIndex + 1 + yShift);
        }

        //Inner Y Edge
        for(int y = shift; y < end - step; y += step){
            int nextY = y + step;

            int connectToY = Math.round((y + nextY) / 2.f) + 1;

            if(level == 0) connectToY--;

            int x = shift;

            indices.put(y * tileSize + x);
            indices.put(nextY * tileSize + x);
            indices.put(connectToY * tileSize);

            x = end - step;

            indices.put(y * tileSize + x);
            indices.put(connectToY * tileSize + tileSize - 1);
            indices.put(nextY * tileSize + x);
        }

        //Inner X Edge
        for(int x = shift; x < end - step; x += step){
            int nextX = x + step;

            int connectToX = Math.round((x + nextX) / 2.f) + 1;

            if(level == 0) connectToX--;

            int y = shift;

            indices.put(y * tileSize + x);
            indices.put(connectToX);
            indices.put(y * tileSize + nextX);

            y = end - step;
            int yShift = (tileSize - 1) * tileSize;

            indices.put(y * tileSize + x);
            indices.put(y * tileSize + nextX);
            indices.put(connectToX + yShift);
        }

        indices.flip();

        BufferObject buffer = new BufferObject();
        buffer.data(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        MemoryUtil.memFree(indices);

        return buffer;
    }

    public void setSeed(int seed){
        heightProgram.bind();
        heightProgram.setUniform("baseSeed", seed);
    }

    public void setStep(float step) {
        this.step = step;

        heightProgram.bind();
        heightProgram.setUniform("step", step);
    }

    private void drawTile(int x, int y, Matrix4f viewProjectionMatrix){
        //System.out.println("Drawing Tile " + x  + ", " + y);
        int vao = getTile(x, y);
        //System.out.println("Fetched Tile");

        //Basic culling
        /*Vector3f horizontalForward = new Vector3f(0.0f, 1.0f, 0.0f).cross(camera.getForward()).cross(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f());
        Vector3f vectorToTile = new Vector3f(x * tileSize + tileSize / 2, 0, y * tileSize + tileSize / 2).sub(camera.getPosition());
        if(horizontalForward.dot(vectorToTile) > 0){
            if(((int) camera.getPosition().x) / tileSize != x || ((int) camera.getPosition().z) / tileSize != y)
            return;
        }*/



        glBindVertexArray(vao);
        for (int i = 0; i < 4; i++) {
            glEnableVertexAttribArray(i);
        }

        int cameraTileX = (int) Math.floor(camera.getPosition().x / tileSize / DISTANCE_BETWEEN);
        int cameraTileY = (int) Math.floor(camera.getPosition().z / tileSize / DISTANCE_BETWEEN);

        int dist = Math.max(Math.abs(x - cameraTileX), Math.abs(y - cameraTileY));

        if(dist > LOD_LEVELS - 1) dist = LOD_LEVELS - 1;
        detailLevels[dist].bind(GL_ELEMENT_ARRAY_BUFFER);

        Matrix4f modelMatrix = new Matrix4f().translate(x * (tileSize - 1) * DISTANCE_BETWEEN, 0, y * (tileSize - 1) * DISTANCE_BETWEEN).scale(1, 20, 1);

        renderingShader.bind();
        renderingShader.setUniform(modelViewMatrixLocation, viewProjectionMatrix.mul(modelMatrix, new Matrix4f()));
        //renderingShader.setUniform(doDiffuseLocation, vectorToTile.lengthSquared() < 10000);

        //System.out.println("Launching draw call");
        glDrawElements(GL_TRIANGLES, (tileSize - 1) * (tileSize - 1) * 2 * 3, GL_UNSIGNED_INT, 0);
        //System.out.println("Finished draw call");
    }

    private void tryUnload(){
        if(tiles.size() > MAX_CACHED_TILES){
            List<Pair<Integer, Integer>> tilePos = new ArrayList<>(tiles.keySet());

            int x = (int) (camera.getPosition().x / tileSize);
            int y = (int) (camera.getPosition().z / tileSize);

            Collections.sort(tilePos, (p1, p2) -> {
                int d1 = Math.abs(p1.getFirst() - x) + Math.abs(p1.getSecond() - y);
                int d2 = Math.abs(p2.getFirst() - x) + Math.abs(p2.getSecond() - y);

                return Integer.compare(d1, d2);
            });

            for(int i = MAX_CACHED_TILES; i < tilePos.size(); i++){
                TileInfo tile = tiles.get(tilePos.get(i));

                glDeleteBuffers(tile.heightmapData.getBufferHandle());
                glDeleteVertexArrays(tile.vao);

                tiles.remove(tilePos.get(i));
            }
        }
    }

    private void doFrame(){
        float dt = dtGetter.getDT();
        camera.handleInput(window, dt);


        for(int i = 0; i < STEPS_PER_FRAME; i++){
            if(!scheduled.isEmpty()){
                scheduled.poll().run();
            }else{
                break;
            }
        }

        renderingShader.bind();
        renderingShader.setUniform(projectionMatrixLocation, GLUtil.getProjectionMatrix(window));

        Matrix4f viewMatrix = camera.getViewMatrix();

        int tileX = Math.floorDiv((int) camera.getPosition().x , tileSize);
        int tileY = Math.floorDiv((int) camera.getPosition().z , tileSize);

        for(int xo = -VIEW_DISTANCE; xo <= VIEW_DISTANCE; xo++){
            for (int zo = -VIEW_DISTANCE; zo <= VIEW_DISTANCE; zo++){
                drawTile(tileX + xo, tileY + zo, viewMatrix);
            }
        }

        tryUnload();
    }

    private int getTile(int x, int y){
        Pair<Integer, Integer> key = new Pair<>(x, y);
        var value = tiles.get(key);

        if(value == null){
            return generateTile(x, y).vao;
        }else{
            return value.vao;
        }
    }

    private TileInfo getTile(int x, int y, boolean generateIfAbsent){
        Pair<Integer, Integer> key = new Pair<>(x, y);
        var value = tiles.get(key);

        if(value == null){
            if(generateIfAbsent) {
                return generateTile(x, y);
            }else{
                return null;
            }
        }else{
            return value;
        }
    }

    private TileInfo generateTile(int x, int y) {
        System.out.println("Generating tile at " + x + " " + y);

        TileInfo tile = new TileInfo(x, y);

        for(int xo = 0; xo < tileSize; xo += BATCH_SIZE){
            for(int yo = 0; yo < tileSize; yo += BATCH_SIZE){
                Vector2i offset = new Vector2i(xo, yo);

                int finalXo = xo;
                int finalYo = yo;
                Runnable generateHeightmap = () -> {
                    heightProgram.bind();
                    tile.heightmapData.bindBase(GL_SHADER_STORAGE_BUFFER, 3);
                    heightProgram.setUniform("startPos",new Vector2f(x * (tileSize - 1)* step, y * (tileSize - 1) * step));
                    heightProgram.setUniform("offset", offset);
                    System.out.println("Offset: " + offset);

                    heightProgram.run(BATCH_SIZE / 32, BATCH_SIZE / 32, 1);
                    tile.heightmapData.bind(GL_SHADER_STORAGE_BUFFER);
                    glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

                    tile.completeHeight(finalXo / BATCH_SIZE, finalYo / BATCH_SIZE);
                };

                scheduled.add(generateHeightmap);
            }
        }

        tiles.put(new Pair<>(x, y), tile);

        return tile;
    }

    private void inspectHeightmap(BufferObject bufferObject) {
        bufferObject.bind(GL_SHADER_STORAGE_BUFFER);
        FloatBuffer buffer = MemoryUtil.memAllocFloat(tileSize * tileSize * 32);

        bufferObject.readInto(GL_SHADER_STORAGE_BUFFER, buffer);

        boolean hasNormal = false;
        boolean hasColor = false;
        boolean hasHeight = false;

        while(buffer.hasRemaining()){
            float normal_x = buffer.get();
            float normal_y = buffer.get();
            float normal_z = buffer.get();
            if(buffer.get() != 0){
                System.out.println("Yikes");
            }

            float r = buffer.get();
            float g = buffer.get();
            float b = buffer.get();
            float height = buffer.get();

            if(normal_x != 0 || normal_y != 0 || normal_z != 0){
                hasNormal = true;
            }

            if(r != 0 || g != 0 || b != 0){
                hasColor = true;
            }

            if(height != 0){
                hasHeight = true;
            }
        }

        MemoryUtil.memFree(buffer);

        System.out.println("Has Height: " + hasHeight);
        System.out.println("Has Normal: " + hasNormal);
        System.out.println("Has Color: " + hasColor);
    }

    public void mainloop(){
        window.show();

        while(!window.shouldClose()){
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            doFrame();

            //System.out.println("Swapping Buffers!");
            window.swapBuffers();
            //System.out.println("Swapped Buffers!");

            GLFW.glfwPollEvents();
        }
    }

    private class TileInfo{
        private final int AXIS_BATCHES = tileSize / BATCH_SIZE;
        private int completedTiles = 0;

        private final BufferObject heightmapData;
        private final int vao;

        private boolean completedHeight = false;

        private final int x, y;

        public TileInfo(int x, int y){
            vao = glGenVertexArrays();
            glBindVertexArray(vao);

            heightmapData = new BufferObject();
            heightmapData.allocate(GL_SHADER_STORAGE_BUFFER, 32L * tileSize * tileSize, GL_DYNAMIC_DRAW);

            xzBuffer.bind(GL_ARRAY_BUFFER);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);
            heightmapData.bind(GL_ARRAY_BUFFER);
            glVertexAttribPointer(1, 1, GL_FLOAT, false, 32, 28);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 32, 16);
            glVertexAttribPointer(3, 3, GL_FLOAT, false, 32, 0);

            this.x = x;
            this.y = y;
        }

        public void completeHeight(int x, int y){
            completedTiles++;

            if(completedTiles >= AXIS_BATCHES * AXIS_BATCHES){
                completedHeight = true;
                normalAndColorProgram.bind();
                normalAndColorProgram.setUniform("offset", new Vector2i(0, 0));

                heightmapData.bindBase(GL_SHADER_STORAGE_BUFFER, 3);
                sampler.bindBase(GL_SHADER_STORAGE_BUFFER, 2);

                if(MEND_NORMALS) {
                    TileInfo negativeXTile = getTile(this.x - 1, this.y, false);
                    TileInfo negativeYTile = getTile(this.x, this.y - 1, false);
                    TileInfo positiveXTile = getTile(this.x + 1, this.y, false);
                    TileInfo positiveYTile = getTile(this.x, this.y + 1, false);

                    boolean hasPositiveXData = positiveXTile != null;
                    boolean hasPositiveYData = positiveYTile != null;
                    boolean hasNegativeXData = negativeXTile != null;
                    boolean hasNegativeYData = negativeYTile != null;

                    if (hasPositiveXData) {
                        hasPositiveXData = positiveXTile.completedHeight;
                    }

                    if (hasPositiveYData) {
                        hasPositiveYData = positiveYTile.completedHeight;
                    }

                    if (hasNegativeXData) {
                        hasNegativeXData = negativeXTile.completedHeight;
                    }

                    if (hasNegativeYData) {
                        hasNegativeYData = negativeYTile.completedHeight;
                    }

                    normalAndColorProgram.setUniform("hasPositiveXData", hasPositiveXData);
                    normalAndColorProgram.setUniform("hasPositiveYData", hasPositiveYData);
                    normalAndColorProgram.setUniform("hasNegativeYData", hasNegativeYData);
                    normalAndColorProgram.setUniform("hasNegativeXData", hasNegativeXData);

                    if (hasPositiveXData) {
                        positiveXTile.heightmapData.bindBase(GL_SHADER_STORAGE_BUFFER, 4);
                        System.out.println("Tile has positive X data");
                    }

                    if (hasPositiveYData) {
                        positiveYTile.heightmapData.bindBase(GL_SHADER_STORAGE_BUFFER, 6);
                        System.out.println("Tile has positive Y data");
                    }

                    if (hasNegativeXData) {
                        negativeXTile.heightmapData.bindBase(GL_SHADER_STORAGE_BUFFER, 5);
                        System.out.println("tile has negative X data");
                    }

                    if (hasNegativeYData) {
                        negativeYTile.heightmapData.bindBase(GL_SHADER_STORAGE_BUFFER, 7);
                        System.out.println("Tile has negative Y data");
                    }
                }

                //heightmapData.bind(GL_SHADER_STORAGE_BUFFER);
                //glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

                normalAndColorProgram.run(tileSize / 32, tileSize / 32, 1);
            }
        }

        private void tryCreateNormals(int x, int y) {
            /*if(x < 0 || y < 0 || x >= AXIS_BATCHES || y >= AXIS_BATCHES) return;

            if(scheduledColor[x][y] || calculatedColor[x][y]) return;

            if(canCreateNormals(x, y)){
                scheduled.add(() -> {
                    normalAndColorProgram.bind();
                    normalAndColorProgram.setUniform("offset", new Vector2i(x * BATCH_SIZE, y * BATCH_SIZE));

                    heightmapData.bindBase(GL_SHADER_STORAGE_BUFFER, 3);
                    sampler.bindBase(GL_SHADER_STORAGE_BUFFER, 2);

                    heightmapData.bind(GL_SHADER_STORAGE_BUFFER);
                    glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

                    normalAndColorProgram.run(BATCH_SIZE / 32, BATCH_SIZE / 32, 1);

                    calculatedColor[x][y] = true;
                });

                scheduledColor[x][y] = true;

                if(x != 0){
                    tryCreateNormals(x - 1, y);
                }

                if(y != 0){
                    tryCreateNormals(x, y - 1);
                }

                if(x != 512 / BATCH_SIZE - 1){
                    tryCreateNormals(x + 1, y);
                }

                if(y != 512 / BATCH_SIZE - 1){
                    tryCreateNormals(x, y + 1);
                }
            }*/
        }

        private boolean canCreateNormals(int x, int y) {
            /*if(!createdHeight[x][y]) return false;

            boolean canCreateNormals = true;

            if(x != 0){
                canCreateNormals = (createdHeight[x - 1][y]);
            }

            if(y != 0){
                canCreateNormals = (createdHeight[x][y - 1]);
            }

            if(x != AXIS_BATCHES - 1){
                canCreateNormals = (createdHeight[x + 1][y]);
            }

            if(y != AXIS_BATCHES - 1){
                canCreateNormals = (createdHeight[x][y + 1]);
            }

            return canCreateNormals;*/
            return false;
        }


    }
}
