package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class LoadVarExpression implements Expression{
    private final Type type;
    private final int index;

    public LoadVarExpression(Type type, int index) {
        this.type = type;
        this.index = index;
    }

    @Override
    public String toGLSL(TranspilationInfo info) {
        return "var" + index;
    }

    @Override
    public Type getType() {
        return type;
    }
}
