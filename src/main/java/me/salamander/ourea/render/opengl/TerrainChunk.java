package me.salamander.ourea.render.opengl;

import org.joml.Matrix4f;

public interface TerrainChunk {
    int x();
    int z();

    void delete();
    void draw(Matrix4f viewMatrix, int indexBuffer);
}
