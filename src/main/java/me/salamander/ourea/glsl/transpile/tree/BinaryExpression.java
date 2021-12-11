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
    public boolean isConstant() {
        return left.isConstant() && right.isConstant();
    }

    @Override
    public Object getConstantValue() {
        return operator.apply((Number) left.getConstantValue(), (Number) right.getConstantValue(), left.getType());
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

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() + right.intValue();
                }else if(type == Type.FLOAT_TYPE) {
                    return left.floatValue() + right.floatValue();
                }else if(type == Type.DOUBLE_TYPE) {
                    return left.doubleValue() + right.doubleValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() + right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        },
        SUB{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info, 0) + " - " + right.toGLSL(info, 0);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() - right.intValue();
                }else if(type == Type.FLOAT_TYPE) {
                    return left.floatValue() - right.floatValue();
                }else if(type == Type.DOUBLE_TYPE) {
                    return left.doubleValue() - right.doubleValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() - right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        },
        MUL{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info, 0) + " * " + right.toGLSL(info, 0);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() * right.intValue();
                }else if(type == Type.FLOAT_TYPE) {
                    return left.floatValue() * right.floatValue();
                }else if(type == Type.DOUBLE_TYPE) {
                    return left.doubleValue() * right.doubleValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() * right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        },
        DIV{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info, 0) + " / " + right.toGLSL(info, 0);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() / right.intValue();
                }else if(type == Type.FLOAT_TYPE) {
                    return left.floatValue() / right.floatValue();
                }else if(type == Type.DOUBLE_TYPE) {
                    return left.doubleValue() / right.doubleValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() / right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        },
        MOD{
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return left.toGLSL(info, 0) + " % " + right.toGLSL(info, 0);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() % right.intValue();
                }else if(type == Type.FLOAT_TYPE) {
                    return left.floatValue() % right.floatValue();
                }else if(type == Type.DOUBLE_TYPE) {
                    return left.doubleValue() % right.doubleValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() % right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        }
        ;

        public abstract String apply(Expression left, Expression right, TranspilationInfo info);
        public abstract Number apply(Number left, Number right, Type type);
    }
}
