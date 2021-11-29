package me.salamander.ourea.glsl.transpile.method;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;

public interface MethodResolver {
    String toGLSL(String owner, String name, String desc, TranspilationInfo info, Expression[] args);
}
