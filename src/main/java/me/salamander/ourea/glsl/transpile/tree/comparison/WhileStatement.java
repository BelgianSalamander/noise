package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;

import java.util.Arrays;
import java.util.Objects;

public class WhileStatement implements Statement {
    private Condition condition;
    private Statement[] body;

    public WhileStatement(Condition condition, Statement[] body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        StringBuilder sb = new StringBuilder();

        sb.append("  ".repeat(depth));
        sb.append("while (");
        sb.append(condition.toGLSL(info, 0));
        sb.append(") {\n");

        for(Statement statement : body) {
            sb.append(statement.toGLSL(info, depth + 1));
        }

        sb.append("  ".repeat(depth));
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhileStatement that = (WhileStatement) o;
        return Objects.equals(condition, that.condition) && Arrays.equals(body, that.body);
    }

    public Statement[] getBody() {
        return body;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public Statement resolvePrecedingExpression(Expression precedingExpression) {
        throw new RuntimeException("This should not be called on this");
    }
}
