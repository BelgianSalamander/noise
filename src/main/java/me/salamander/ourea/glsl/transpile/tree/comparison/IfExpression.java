package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Objects;

public class IfExpression implements Expression {
    private Condition condition;
    private Expression[] statements;

    public IfExpression(Condition condition, Expression... statements) {
        this.condition = condition;
        this.statements = statements;

        if(condition.getType() != Type.BOOLEAN_TYPE) {
            throw new RuntimeException("Condition must be a boolean");
        }
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        StringBuilder sb = new StringBuilder();

        sb.append("  ".repeat(depth));
        sb.append("if (");
        sb.append(condition.toGLSL(info, 0));
        sb.append(") {\n");

        for(Expression statement : statements) {
            sb.append(statement.toGLSL(info, depth + 1));
        }

        sb.append("  ".repeat(depth));
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Object getConstantValue() {
        throw new RuntimeException("Not a constant");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfExpression that = (IfExpression) o;
        return Objects.equals(condition, that.condition) && Arrays.equals(statements, that.statements);
    }

    public Expression[] getBody() {
        return statements;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return new IfExpression((Condition) condition.resolvePrecedingExpression(precedingExpression), statements);
    }
}
