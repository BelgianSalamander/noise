package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class StoreVarExpression implements Expression{
    private Expression value;
    private int var;

    public StoreVarExpression(Expression value, int var) {
        this.value = value;
        this.var = var;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "var" + var + " = " + value.toGLSL(info, 0) + ";\n";
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }
}
