package me.salamander.noisetest.render.api;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.lang.ref.Cleaner;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;

public class ShaderProgram implements Cleaner.Cleanable {
    private int program;
    private int vertexShader, fragmentShader;
    private boolean linked = false;

    public ShaderProgram(String vertexSource, String fragmentSource){
        this(vertexSource, fragmentSource, true);
    }

    public ShaderProgram(String vertexSource, String fragmentSource, boolean link){
        program = glCreateProgram();

        vertexShader = compileShader(vertexSource, GL_VERTEX_SHADER);
        fragmentShader = compileShader(fragmentSource, GL_FRAGMENT_SHADER);

        if(link) link();
    }

    public void link(){
        if(linked){
            System.out.println("[WARNING] Program already linked but link() has been called");
            return;
        }

        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
            throw new IllegalStateException("Could not link program: " + glGetProgramInfoLog(program, 4096));
        }

        glDetachShader(program, vertexShader);
        glDetachShader(program, fragmentShader);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        linked = true;
    }

    private int compileShader(String source, int type){
        int shader = glCreateShader(type);

        if(shader == 0){
            throw new IllegalStateException("Could not create shader");
        }

        glShaderSource(shader, source);
        glCompileShader(shader);

        if(glGetShaderi(shader, GL_COMPILE_STATUS) == 0){
            throw new IllegalStateException("Could not compile '" + GLUtil.getValue(type) + "' : " + glGetShaderInfoLog(shader, 4096));
        }

        glAttachShader(program, shader);

        return shader;
    }

    public int getUniformLocation(String name){
        int uniformLocation = glGetUniformLocation(program, name);

        if(uniformLocation < 0){
            throw new IllegalStateException("Could not get uniform '" + name + "'");
        }

        return uniformLocation;
    }

    public void setUniform(int location, Matrix4f value){
        try(MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
    public void setUniform(int location, Matrix3f value){
        try(MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.mallocFloat(9);
            value.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
    public void setUniform(int location, Vector3f value){
        try(MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.mallocFloat(3);
            value.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
    public void setUniform(int location, boolean value){
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

    public void bind(){
        glUseProgram(program);
    }

    public int getHandle(){
        return program;
    }
}
