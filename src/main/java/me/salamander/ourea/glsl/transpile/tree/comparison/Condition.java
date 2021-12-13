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

    public static Condition not(Condition condition) {
        if(condition instanceof BooleanCondition) {
            return new BooleanCondition(!((BooleanCondition) condition).value);
        }else{
            return new Not(condition);
        }
    }

    private static class Not extends Condition {
        private final Condition condition;

        public Not(Condition condition) {
            this.condition = condition;
        }

        @Override
        public String toGLSL(TranspilationInfo info, int depth) {
            return "!" + eval(condition, info);
        }

        @Override
        public Expression resolvePrecedingExpression(Expression precedingExpression) {
            return new Not((Condition) condition.resolvePrecedingExpression(precedingExpression));
        }

        @Override
        public Condition negate() {
            return condition;
        }

        @Override
        public boolean isConstant() {
            return condition.isConstant();
        }

        @Override
        public Object getConstantValue() {
            return !((boolean) condition.getConstantValue());
        }

        @Override
        public int getPrecedence() {
            return 3;
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
