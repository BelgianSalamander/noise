package me.salamander.ourea.render.opengl;

import me.salamander.ourea.color.ColorGradient;
import me.salamander.ourea.modules.NoiseSampler;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.concurrent.*;

import static org.lwjgl.opengl.GL45.*;

public class CPUChunkGenerator extends OpenGL2DRenderer{
    private final ExecutorService executor;
    private final CompletionService<ChunkData> completionService;
    private int code = 0;

    public CPUChunkGenerator(int chunkSize, float step, int seed, ColorMode colorMode, ColorGradient gradient, NoiseSampler sampler) {
        super(chunkSize, step, seed, colorMode, gradient, sampler);

        executor = Executors.newFixedThreadPool(5);
        completionService = new ExecutorCompletionService<>(executor);
    }

    @Override
    protected void invalidateGeneratingChunks() {
        code++;
    }

    @Override
    protected void queueChunk(int x, int z) {
        completionService.submit(() -> generateChunk(x, z, code));
    }

    @Override
    protected void postProcess() {
        Future<ChunkData> chunkData = completionService.poll();
        while (chunkData != null) {
            ChunkData data;
            try {
                data = chunkData.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            if(data.code == code) {
                putBakedChunk(data.x, data.z, data.toBakedChunk(this));
            }
            chunkData = completionService.poll();
        }
    }

    private ChunkData generateChunk(int x, int z, int code){
        float startX = x * (chunkSize - 1) * step;
        float startZ = z * (chunkSize - 1) * step;

        int colorDataSize = colorMode.useTexture ? 2 : 4;

        float[][] heightArray = new float[chunkSize][chunkSize];
        FloatBuffer heightData = MemoryUtil.memAllocFloat(chunkSize * chunkSize);
        FloatBuffer colorData = MemoryUtil.memAllocFloat(chunkSize * chunkSize * colorDataSize);
        FloatBuffer textureData = null;
        if(colorMode.useTexture){
            textureData = MemoryUtil.memAllocFloat(chunkSize * chunkSize * 4);
        }

        for(int xi = 0; xi < chunkSize; xi++){
            float xIndex = startX + xi * step;
            for(int zi = 0; zi < chunkSize; zi++){
                float zIndex = startZ + zi * step;

                float height = noiseSampler.sample(xIndex, zIndex, seed);
                //heightData.put(height);
                heightArray[xi][zi] = height;

                if(colorMode.useTexture){
                    gradient.putInto(height, textureData, true, colorMode.smooth);
                    float uvX = (float) xi / (chunkSize - 1);
                    float uvZ = (float) zi / (chunkSize - 1);

                    float temp = uvX;
                    uvX = uvZ;
                    uvZ = temp;

                    colorData.put(uvX);
                    colorData.put(uvZ);
                }else{
                    gradient.putInto(height, colorData, true, colorMode.smooth);
                }
            }
            heightData.put(heightArray[xi]);
        }

        //Compute normals
        FloatBuffer normalData = MemoryUtil.memAllocFloat(chunkSize * chunkSize * 3);
        for(int xi = 0; xi < chunkSize; xi++){
            for(int zi = 0; zi < chunkSize; zi++) {
                /*float currHeight = heightArray[xi][zi];

                float northX = 0, northY = 0, northZ = 0;
                float southX = 0, southY = 0, southZ = 0;
                float eastX = 0, eastY = 0, eastZ = 0;
                float westX = 0, westY = 0, westZ = 0;

                if (xi > 0) {
                    float otherHeight = heightArray[xi - 1][zi];
                    westX = -1;
                    westY = currHeight - otherHeight;
                    westZ = 0;
                } else {
                    normalData.put(0);
                    normalData.put(1);
                    normalData.put(0);
                    continue;
                }

                if (xi < chunkSize - 1) {
                    float otherHeight = heightArray[xi + 1][zi];
                    eastX = 1;
                    eastY = currHeight - otherHeight;
                    eastZ = 0;
                } else {
                    normalData.put(0);
                    normalData.put(1);
                    normalData.put(0);
                    continue;
                }

                if (zi > 0) {
                    float otherHeight = heightArray[xi][zi - 1];
                    southX = 0;
                    southY = currHeight - otherHeight;
                    southZ = -1;
                } else {
                    normalData.put(0);
                    normalData.put(1);
                    normalData.put(0);
                    continue;
                }

                if (zi < chunkSize - 1) {
                    float otherHeight = heightArray[xi][zi + 1];
                    northX = 0;
                    northY = currHeight - otherHeight;
                    northZ = 1;
                } else {
                    normalData.put(0);
                    normalData.put(1);
                    normalData.put(0);
                    continue;
                }

                float normalX = 0;
                float normalY = 0;
                float normalZ = 0;

                //This is a bunch of cross products added together
                normalX += northY * eastZ - eastY * northZ;
                normalX += southY * westZ - westY * southZ;
                normalX += eastY * southZ - southY * eastZ;
                normalX += westY * northZ - northY * westZ;

                normalY += northZ * eastX - eastZ * northX;
                normalY += southZ * westX - westZ * southX;
                normalY += eastZ * southX - southZ * eastX;
                normalY += westZ * northX - northZ * westX;

                normalZ += northX * eastY - eastX * northY;
                normalZ += southX * westY - westX * southY;
                normalZ += eastX * southY - southX * eastY;
                normalZ += westX * northY - northX * westY;

                float normalLength = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
                normalX /= normalLength;
                normalY /= normalLength;
                normalZ /= normalLength;

                normalData.put(normalX);
                normalData.put(normalY);
                normalData.put(normalZ);*/
                float dydx = 0;
                float dzdx = 0;

                if(xi > 0 && xi < chunkSize - 1){
                    dydx = (heightArray[xi + 1][zi] - heightArray[xi - 1][zi]) / 2;
                }else if(xi == 0){
                    dydx = (heightArray[xi + 1][zi] - heightArray[xi][zi]);
                }else{
                    dydx = (heightArray[xi][zi] - heightArray[xi - 1][zi]);
                }

                if(zi > 0 && zi < chunkSize - 1){
                    dzdx = (heightArray[xi][zi + 1] - heightArray[xi][zi - 1]) / 2;
                }else if(zi == 0){
                    dzdx = (heightArray[xi][zi + 1] - heightArray[xi][zi]);
                }else{
                    dzdx = (heightArray[xi][zi] - heightArray[xi][zi - 1]);
                }

                float normalX = dzdx * 4;
                float normalY = 4;
                float normalZ = dydx * 4;

                float normalLength = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
                normalX /= normalLength;
                normalY /= normalLength;
                normalZ /= normalLength;

                normalData.put(normalX);
                normalData.put(normalY);
                normalData.put(normalZ);
            }
        }

        heightData.flip();
        normalData.flip();
        colorData.flip();
        if(textureData != null){
            textureData.flip();
        }

        return new ChunkData(
                code, x, z, heightData, colorData, normalData, textureData
        );
    }

    @Override
    protected void delete() {
        executor.shutdown();
    }

    private record ChunkData(int code, int x, int z, FloatBuffer height, FloatBuffer color, FloatBuffer normal, FloatBuffer textureData){
        public TerrainChunk toBakedChunk(OpenGL2DRenderer renderer){
            int vao = glGenVertexArrays();
            int heightBuffer = glGenBuffers();
            int colorBuffer = glGenBuffers();
            int normalBuffer = glGenBuffers();

            int texture = textureData() != null ? glGenTextures() : -1;

            glBindVertexArray(vao);

            glBindBuffer(GL_ARRAY_BUFFER, renderer.xzBuffer);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);

            glBindBuffer(GL_ARRAY_BUFFER, heightBuffer);
            glBufferData(GL_ARRAY_BUFFER, height, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 1, GL_FLOAT, false, 4, 0);

            glBindBuffer(GL_ARRAY_BUFFER, colorBuffer);
            glBufferData(GL_ARRAY_BUFFER, color, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, textureData() == null ? 4 : 2, GL_FLOAT, false, textureData() == null ? 16 : 8, 0);

            glBindBuffer(GL_ARRAY_BUFFER, normalBuffer);
            glBufferData(GL_ARRAY_BUFFER, normal, GL_STATIC_DRAW);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(3, 3, GL_FLOAT, false, 12, 0);

            if(textureData() != null){
                glBindTexture(GL_TEXTURE_2D, texture);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, renderer.chunkSize, renderer.chunkSize, 0, GL_RGBA, GL_FLOAT, textureData());
            }

            return renderer.new TerrainChunk(x * (renderer.chunkSize - 1), z * (renderer.chunkSize - 1), vao, heightBuffer, colorBuffer, normalBuffer, texture);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
    }
}
