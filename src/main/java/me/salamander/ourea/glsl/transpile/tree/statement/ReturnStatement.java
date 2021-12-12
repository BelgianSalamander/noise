package me.salamander.ourea.glsl.transpile.tree.statement;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;

public class ReturnStatement implements Statement {
    private final Expression value;

    public ReturnStatement(Expression value) {
        this.value = value;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "return " + value.toGLSL(info, 0) + ";\n";
    }

    @Override
    public ReturnStatement resolvePrecedingExpression(Expression precedingExpression) {
        return new ReturnStatement(value.resolvePrecedingExpression(precedingExpression));
    }
}
