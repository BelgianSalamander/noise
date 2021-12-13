package me.salamander.ourea.glsl.transpile.tree.expression;

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
    public String toGLSL(TranspilationInfo info, int depth) {
        return info.getVarName(index, type);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Object getConstantValue() {
        throw new RuntimeException("Not a constant");
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return this;
    }

    public int getIndex() {
        return index;
    }
}