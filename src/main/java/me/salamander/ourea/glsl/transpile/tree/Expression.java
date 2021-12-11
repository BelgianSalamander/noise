package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;


public interface Expression {
    //If type is void, the expression is a statement

    String toGLSL(TranspilationInfo info, int depth);
    Type getType();

    boolean isConstant();
    Object getConstantValue();

    default boolean isStatement() {
        return getType() == Type.VOID_TYPE;
    }
}
