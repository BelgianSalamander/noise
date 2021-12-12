package me.salamander.ourea.glsl.transpile.tree.statement;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;

public class BreakStatement implements Statement {
    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "break;\n";
    }

    @Override
    public BreakStatement resolvePrecedingExpression(Expression precedingExpression) {
        return this;
    }
}
