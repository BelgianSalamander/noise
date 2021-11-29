package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class BinaryExpression implements Expression{
    private final Expression left;
    private final Expression right;
    private final Operator operator;

    public BinaryExpression(Expression left, Expression right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;

        if(left.getType() != right.getType()) {
            throw new RuntimeException("Binary expression types do not match. (" + left.getType() + " " + operator + " " + right.getType() + ")");
        }
    }

    @Override
    public Type getType() {
        return left.getType();
    }

    @Override
    public String toGLSL(TranspilationInfo info) {
        return operator.apply(left, right, info);
    }

    public enum Operator {
        ADD{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info) + " + " + right.toGLSL(info);
            }
        },
        SUB{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info) + " - " + right.toGLSL(info);
            }
        },
        MUL{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info) + " * " + right.toGLSL(info);
            }
        },
        DIV{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info) + " / " + right.toGLSL(info);
            }
        },
        MOD{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info) + " % " + right.toGLSL(info);
            }
        }
        ;

        public abstract String apply(Expression left, Expression right, TranspilationInfo info);
    }
}
