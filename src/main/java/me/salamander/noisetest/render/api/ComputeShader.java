package me.salamander.noisetest.render.api;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.lang.ref.Cleaner;
import java.nio.FloatBuffer;

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
    }

    public int getUniformLocation(String name){
        int uniformLocation = glGetUniformLocation(program, name);

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

    private void setUniform(int uniformLocation, float value) {
        if(uniformLocation < 0) return;
        
        glUniform1f(uniformLocation, value);
    }

    public void bind() {
        glUseProgram(program);
    }
}
