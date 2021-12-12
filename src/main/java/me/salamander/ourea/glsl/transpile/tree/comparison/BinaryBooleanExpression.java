package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;

public class BinaryBooleanExpression extends Condition{
    private final Condition left;
    private final Condition right;
    private final Operator operator;

    public BinaryBooleanExpression(Condition left, Condition right, Operator operator){
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return operator.apply(left, right, info);
    }

    @Override
    public boolean isConstant() {
        return left.isConstant() && right.isConstant();
    }

    @Override
    public Object getConstantValue() {
        return operator.apply((Boolean) left.getConstantValue(), (Boolean) right.getConstantValue());
    }

    @Override
    public int getPrecedence() {
        return operator.getPrecedence();
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return new BinaryBooleanExpression((Condition) left.resolvePrecedingExpression(precedingExpression), (Condition) right.resolvePrecedingExpression(precedingExpression), operator);
    }

    @Override
    public Condition negate() {
        //Boolean algebra baby!
        if(operator == Operator.AND){
            return new BinaryBooleanExpression(left.negate(), right.negate(), Operator.OR);
        }else if(operator == Operator.OR){
            return new BinaryBooleanExpression(left.negate(), right.negate(), Operator.AND);
        }
        throw new RuntimeException("Cannot negate this operator");
    }

    public enum Operator {
        AND(12) {
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info) {
                return left.toGLSL(info, 0) + " && " + right.toGLSL(info, 0);
            }

            @Override
            public boolean apply(boolean a, boolean b){
                return a && b;
            }
        },
        OR(14) {
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info) {
                return left.toGLSL(info, 0) + " || " + right.toGLSL(info, 0);

            }

            @Override
            public boolean apply(boolean a, boolean b){
                return a || b;
            }
        };

        private final int precedence;

        Operator(int precedence){
            this.precedence = precedence;
        }

        public int getPrecedence() {
            return precedence;
        }

        public abstract String apply(Expression left, Expression right, TranspilationInfo info);
        public abstract boolean apply(boolean a, boolean b);
    }
}
