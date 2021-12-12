package me.salamander.ourea.glsl.transpile.tree.expression;

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
    public int getPrecedence() {
        return operator.getPrecedence();
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return new BinaryExpression(left.resolvePrecedingExpression(precedingExpression), right.resolvePrecedingExpression(precedingExpression), operator);
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return operator.apply(left, right, info);
    }

    public enum Operator {
        ADD(5){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " + " + eval(right, info);
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
        SUB(5){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " - " + eval(right, info);
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
        MUL(4){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " * " + eval(right, info);
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
        DIV(4){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " / " + eval(right, info);
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
        MOD(4){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " % " + eval(right, info);
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
        },
        AND(9){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " & " + eval(right, info);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() & right.intValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() & right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        },
        OR(11){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " | " + eval(right, info);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() | right.intValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() | right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        },
        XOR(10){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " ^ " + eval(right, info);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() ^ right.intValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() ^ right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        },
        SHR(6){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " >> " + eval(right, info);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() >> right.intValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() >> right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        },
        SHL(6){
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info){
                return eval(left, info) + " << " + eval(right, info);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if(type == Type.INT_TYPE) {
                    return left.intValue() << right.intValue();
                }else if(type == Type.LONG_TYPE) {
                    return left.longValue() << right.longValue();
                }else{
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        },
        USHR(6) {
            @Override
            public String apply(Expression left, Expression right, TranspilationInfo info) {
                return eval(left, info) + " >>> " + eval(right, info);
            }

            @Override
            public Number apply(Number left, Number right, Type type) {
                if (type == Type.INT_TYPE) {
                    return left.intValue() >>> right.intValue();
                } else if (type == Type.LONG_TYPE) {
                    return left.longValue() >>> right.longValue();
                } else {
                    throw new RuntimeException("Unsupported type: " + type);
                }
            }
        }
        ;

        private final int precedence;

        Operator(int precedence){
            this.precedence = precedence;
        }

        public int getPrecedence(){
            return precedence;
        }

        protected String eval(Expression expr, TranspilationInfo info){
            String base = expr.toGLSL(info, 0);
            if(expr.getPrecedence() > precedence){
                return "(" + base + ")";
            }
            return base;
        }

        public abstract String apply(Expression left, Expression right, TranspilationInfo info);
        public abstract Number apply(Number left, Number right, Type type);
    }
}
