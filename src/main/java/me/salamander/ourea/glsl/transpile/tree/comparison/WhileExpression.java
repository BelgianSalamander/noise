package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Objects;

public class WhileExpression implements Expression {
    private Condition condition;
    private Expression[] body;

    public WhileExpression(Condition condition, Expression[] body) {
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

        for(Expression statement : body) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhileExpression that = (WhileExpression) o;
        return Objects.equals(condition, that.condition) && Arrays.equals(body, that.body);
    }
}
