package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import org.objectweb.asm.Type;

import java.util.Arrays;

public class IfElseExpression implements Expression {
    private final Condition condition;
    private final Expression[] trueExpressions;
    private final Expression[] falseExpressions;

    public IfElseExpression(Condition condition, Expression[] trueExpressions, Expression[] falseExpressions) {
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
       for(Expression e : trueExpressions) {
           sb.append(e.toGLSL(info, depth + 1));
       }
       sb.append("  ".repeat(depth));
       sb.append("} else {\n");
       for(Expression e : falseExpressions) {
           sb.append(e.toGLSL(info, depth + 1));
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
    public boolean equals(Object obj) {
        if(obj instanceof IfElseExpression other) {
            return condition.equals(other.condition) &&
                    Arrays.equals(trueExpressions, other.trueExpressions) &&
                    Arrays.equals(falseExpressions, other.falseExpressions);
        }
        return false;
    }

    public Expression[] getIfTrue() {
        return trueExpressions;
    }

    public Expression[] getIfFalse() {
        return falseExpressions;
    }
}
