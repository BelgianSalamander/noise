package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import org.objectweb.asm.Type;

public abstract class Condition implements Expression {
    public abstract Condition negate();

    @Override
    public Type getType() {
        return Type.BOOLEAN_TYPE;
    }

    public static Condition of(boolean value) {
        return new BooleanCondition(value);
    }

    public static Condition and(Condition left, Condition right) {
        if(left instanceof BooleanCondition && right instanceof BooleanCondition) {
            return new BooleanCondition(((BooleanCondition) left).value && ((BooleanCondition) right).value);
        }else if(left instanceof BooleanCondition leftBool) {
            if(leftBool.value) {
                return right;
            }else{
                return new BooleanCondition(false);
            }
        }else if(right instanceof BooleanCondition rightBool) {
            if(rightBool.value) {
                return left;
            }else{
                return new BooleanCondition(false);
            }
        }else{
            return new BinaryBooleanExpression(left, right, BinaryBooleanExpression.Operator.AND);
        }
    }

    private static class BooleanCondition extends Condition {
        private final boolean value;

        public BooleanCondition(boolean value) {
            this.value = value;
        }

        @Override
        public Condition negate() {
            return new BooleanCondition(!value);
        }

        @Override
        public String toGLSL(TranspilationInfo info, int depth) {
            return Boolean.toString(value);
        }

        @Override
        public boolean isConstant() {
            return true;
        }

        @Override
        public Object getConstantValue() {
            return value;
        }

        @Override
        public int getPrecedence() {
            return 0;
        }

        @Override
        public Expression resolvePrecedingExpression(Expression precedingExpression) {
            return this;
        }
    }
}
