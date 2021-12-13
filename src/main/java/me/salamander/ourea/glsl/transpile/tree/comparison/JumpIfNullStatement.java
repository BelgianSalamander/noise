package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;

public class JumpIfNullStatement implements ConditionalJump {
    private final Expression value;
    private final boolean invert;

    public JumpIfNullStatement(Expression value, boolean invert) {
        this.value = value;
        this.invert = invert;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "[PSEUDO_STATEMENT] Jump If Null '" + value.toGLSL(info, depth) + "'";
    }

    @Override
    public Statement resolvePrecedingExpression(Expression precedingExpression) {
        throw new RuntimeException("Should not be called on a JumpIfNullStatement");
    }

    @Override
    public Condition getCondition() {
        Condition baseCondition = new NullCondition(value);
        if (invert) {
            return baseCondition.negate();
        }
        return baseCondition;
    }

    private static class NullCondition extends Condition {
        Expression value;

        public NullCondition(Expression value) {
            this.value = value;
        }

        @Override
        public Condition negate() {
            return Condition.not(this);
        }

        @Override
        public boolean isConstant() {
            return value.isConstant();
        }

        @Override
        public Object getConstantValue() {
            return value.getConstantValue() == null;
        }

        @Override
        public int getPrecedence() {
            return 2;
        }

        @Override
        public String toGLSL(TranspilationInfo info, int depth) {
            return eval(value, info) + ".isNull";
        }

        @Override
        public Expression resolvePrecedingExpression(Expression precedingExpression) {
            return new NullCondition(value.resolvePrecedingExpression(precedingExpression));
        }
    }
}
