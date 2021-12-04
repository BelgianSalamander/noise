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
    public String toGLSL(TranspilationInfo info, int depth) {
        return operator.apply(left, right, info);
    }

    public enum Operator {
        ADD{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info, 0) + " + " + right.toGLSL(info, 0);
            }
        },
        SUB{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info, 0) + " - " + right.toGLSL(info, 0);
            }
        },
        MUL{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info, 0) + " * " + right.toGLSL(info, 0);
            }
        },
        DIV{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info, 0) + " / " + right.toGLSL(info, 0);
            }
        },
        MOD{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info, 0) + " % " + right.toGLSL(info, 0);
            }
        }
        ;

        public abstract String apply(Expression left, Expression right, TranspilationInfo info);
    }
}
