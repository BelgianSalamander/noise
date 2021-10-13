package me.salamander.noisetest.render.api;

import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.lang.ref.Cleaner;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL45.*;

public class ComputeShader implements Cleaner.Cleanable {
    private int program;

    public ComputeShader(String source){
        int[] invocationsMax = new int[1];

        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, invocationsMax);
        System.out.println("Max Invocations X: " + invocationsMax[0]);

        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 1, invocationsMax);
        System.out.println("Max Invocations Y: " + invocationsMax[0]);

        glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 2, invocationsMax);
        System.out.println("Max Invocations Z: " + invocationsMax[0]);

        program = glCreateProgram();

        int shader = ShaderProgram.compileAndAttachShader(program, source, GL_COMPUTE_SHADER);

        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
            throw new IllegalStateException("Could not link program: " + glGetProgramInfoLog(program, 4096));
        }

        glDetachShader(program, shader);
        glDeleteShader(shader);
    }

    public void run(int numGroupsX, int numGroupsY, int numGroupsZ){
        bind();
        glDispatchCompute(numGroupsX, numGroupsY, numGroupsZ);

        System.out.println("Running " + (numGroupsX * numGroupsY * numGroupsZ) + " work groups!");
    }

    public int getUniformLocation(String name){
        int uniformLocation = glGetUniformLocation(program, name);

        if(uniformLocation < 0){
            System.out.println("Warning: Tried to fetch invalid uniform location ('" + name + "')");
        }

        return uniformLocation;
    }

    public void setUniform(int location, Matrix4f value){
        if(location < 0) return;
        
        try(MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
    public void setUniform(int location, Matrix3f value){
        if(location < 0) return;
        
        try(MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.mallocFloat(9);
            value.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
    public void setUniform(int location, Vector3f value){
        if(location < 0) return;
        
        try(MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.mallocFloat(3);
            value.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
    public void setUniform(int location, boolean value){
        if(location < 0) return;
        
        glUniform1i(location, value ? 1 : 0);
    }

    public void setUniform(String name, Matrix4f value){
        setUniform(getUniformLocation(name), value);
    }
    public void setUniform(String name, Matrix3f value){
        setUniform(getUniformLocation(name), value);
    }

    @Override
    public void clean() {
        glDeleteProgram(program);
    }

    public void setUniformUnsignedInt(String name, int i) {
        setUniformUnsignedInt(getUniformLocation(name), i);
    }

    public void setUniformUnsignedInt(int location, int i){
        if(location < 0) return;
        
        glUniform1ui(location, i);
    }

    public void setUniform(String name, Vector2f value) {
        setUniform(getUniformLocation(name), value);
    }

    private void setUniform(int uniformLocation, Vector2f value) {
        if(uniformLocation < 0) return;
        
        try(MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.mallocFloat(2);
            value.get(fb);
            glUniform2fv(uniformLocation, fb);
        }
    }

    public void setUniform(String name, float value) {
        setUniform(getUniformLocation(name), value);
    }

    public void setUniform(String name, int value){
        setUniform(getUniformLocation(name), value);
    }

    public void setUniform(int uniformLocation, float value) {
        if(uniformLocation < 0) return;
        
        glUniform1f(uniformLocation, value);
    }

    public void setUniform(int uniformLocation, int value) {
        if(uniformLocation < 0) return;

        glUniform1i(uniformLocation, value);
    }

    public void setUniform(int uniformLocation, Vector2i value){
        if(uniformLocation < 0) return;

        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ib = stack.mallocInt(2);
            value.get(ib);
            glUniform2iv(uniformLocation, ib);
        }
    }

    public void bind() {
        glUseProgram(program);
    }

    public int getHandle() {
        return program;
    }

    public void setUniform(String name, Vector2i value) {
        setUniform(getUniformLocation(name), value);
    }

    public void setUniform(String name, boolean b) {
        setUniform(getUniformLocation(name), b);
    }
}
