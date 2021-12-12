package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;
import org.objectweb.asm.Type;

import java.util.Arrays;

public class IfElseStatement implements Statement {
    private Condition condition;
    private final Statement[] trueExpressions;
    private final Statement[] falseExpressions;

    public IfElseStatement(Condition condition, Statement[] trueExpressions, Statement[] falseExpressions) {
        this.condition = condition;
        this.trueExpressions = trueExpressions;
        this.falseExpressions = falseExpressions;

        if(condition.getType() != Type.BOOLEAN_TYPE) {
            throw new RuntimeException("Condition must be a boolean");
        }
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
       StringBuilder sb = new StringBuilder();
       sb.append("  ".repeat(depth));
       sb.append("if(");
       sb.append(condition.toGLSL(info, 0));
       sb.append(") {\n");
       for(Statement e : trueExpressions) {
           sb.append(e.toGLSL(info, depth + 1));
       }
       sb.append("  ".repeat(depth));
       sb.append("} else {\n");
       for(Statement e : falseExpressions) {
           sb.append(e.toGLSL(info, depth + 1));
       }
       sb.append("  ".repeat(depth));
       sb.append("}\n");

       return sb.toString();
    }

    @Override
    public IfElseStatement resolvePrecedingExpression(Expression precedingExpression) {
        return new IfElseStatement((Condition) condition.resolvePrecedingExpression(precedingExpression), trueExpressions, falseExpressions);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IfElseStatement other) {
            return condition.equals(other.condition) &&
                    Arrays.equals(trueExpressions, other.trueExpressions) &&
                    Arrays.equals(falseExpressions, other.falseExpressions);
        }
        return false;
    }

    public Statement[] getIfTrue() {
        return trueExpressions;
    }

    public Statement[] getIfFalse() {
        return falseExpressions;
    }
}
