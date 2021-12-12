package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class ContinueExpression implements Expression {
    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "continue;\n";
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
        throw new RuntimeException("Not a constant");
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return this;
    }
}
