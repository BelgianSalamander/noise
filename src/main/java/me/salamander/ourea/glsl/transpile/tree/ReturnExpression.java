package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class ReturnExpression implements Expression {
    private final Expression value;

    public ReturnExpression(Expression value) {
        this.value = value;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "return " + value.toGLSL(info, 0) + ";\n";
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Object getConstantValue() {
        throw new RuntimeException("ReturnExpression is not constant");
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return new ReturnExpression(value.resolvePrecedingExpression(precedingExpression));
    }
}
