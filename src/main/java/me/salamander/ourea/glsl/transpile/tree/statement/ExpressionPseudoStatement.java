package me.salamander.ourea.glsl.transpile.tree.statement;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;

public class ExpressionPseudoStatement implements Statement{
    public Expression expression;

    public ExpressionPseudoStatement(Expression expression){
        this.expression = expression;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "[PSEUDO_STATEMENT](" + expression.toGLSL(info, depth) + ")";
    }

    @Override
    public Statement resolvePrecedingExpression(Expression precedingExpression) {
        expression = expression.resolvePrecedingExpression(precedingExpression);
        return this;
    }
}
