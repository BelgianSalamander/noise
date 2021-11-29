package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class ReturnExpression implements Expression {
    private final Expression value;

    public ReturnExpression(Expression value) {
        this.value = value;
    }

    @Override
    public String toGLSL(TranspilationInfo info) {
        return "return " + value.toGLSL(info) + ";";
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }
}
