package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;

public interface CodeFragment<T extends CodeFragment<T>> {
    String toGLSL(TranspilationInfo info, int depth);

    T resolvePrecedingExpression(Expression precedingExpression);
}
