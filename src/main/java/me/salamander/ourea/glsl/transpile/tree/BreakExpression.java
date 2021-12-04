package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class BreakExpression implements Expression{
    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "break;\n";
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }
}
