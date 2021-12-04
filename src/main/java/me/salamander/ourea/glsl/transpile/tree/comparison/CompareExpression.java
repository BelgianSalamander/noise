package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import org.objectweb.asm.Type;

public class CompareExpression implements Expression{
    private final Expression left;
    private final Expression right;

    public CompareExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        throw new RuntimeException("Cannot directly turn comparison into GLSL");
    }

    @Override
    public Type getType() {
        return Type.INT_TYPE;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }
}
