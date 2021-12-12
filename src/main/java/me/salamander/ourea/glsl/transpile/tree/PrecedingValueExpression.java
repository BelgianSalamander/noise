package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.CFGNode;
import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class PrecedingValueExpression implements Expression{
    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "[UNRESOLVED PRECEDING VALUE]";
    }

    @Override
    public Type getType() {
        return CFGNode.UNRESOLVED_TYPE;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Object getConstantValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return precedingExpression;
    }
}
