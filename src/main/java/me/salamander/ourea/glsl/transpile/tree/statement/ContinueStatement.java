package me.salamander.ourea.glsl.transpile.tree.statement;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;

public class ContinueStatement implements Statement {
    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "continue;\n";
    }

    @Override
    public Statement resolvePrecedingExpression(Expression precedingExpression) {
        return this;
    }
}
