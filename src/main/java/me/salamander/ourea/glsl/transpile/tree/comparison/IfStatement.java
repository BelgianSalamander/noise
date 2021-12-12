package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Objects;

public class IfStatement implements Statement {
    private Condition condition;
    private Statement[] statements;

    public IfStatement(Condition condition, Statement... statements) {
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

        for(Statement statement : statements) {
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
        IfStatement that = (IfStatement) o;
        return Objects.equals(condition, that.condition) && Arrays.equals(statements, that.statements);
    }

    public Statement[] getBody() {
        return statements;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public IfStatement resolvePrecedingExpression(Expression precedingExpression) {
        return new IfStatement((Condition) condition.resolvePrecedingExpression(precedingExpression), statements);
    }
}
