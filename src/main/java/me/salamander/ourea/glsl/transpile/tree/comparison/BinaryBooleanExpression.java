package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;

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
        AND {
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info) {
                return left.toGLSL(info, 0) + " && " + right.toGLSL(info, 0);
            }
        },
        OR {
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info) {
                return left.toGLSL(info, 0) + " || " + right.toGLSL(info, 0);

            }
        };

        public abstract String apply(Expression left, Expression right, TranspilationInfo info);
    }
}
