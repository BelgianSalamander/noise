package me.salamander.noisetest.render.api;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL45.*;

public class BufferObject {
    private final int bufferHandle;

    public BufferObject(){
        bufferHandle = glGenBuffers();
    }

    public void bind(int bindingPoint){
        glBindBuffer(bindingPoint, bufferHandle);
    }

    public void bindBase(int bindingPoint, int index){
        glBindBufferBase(bindingPoint, index, bufferHandle);
    }

    public void allocate(int bindingPoint, long size, int usage){
        bind(bindingPoint);
        glBufferData(bindingPoint, size, usage);
    }

    public void data(int bindingPoint, float[] data, int usage){
        bind(bindingPoint);
        glBufferData(bindingPoint, data, usage);
    }

    public void subData(int bindingPoint, FloatBuffer data, long offset){
        bind(bindingPoint);
        glBufferSubData(bindingPoint, offset, data);
    }

    public int getBufferHandle() {
        return bufferHandle;
    }
}
