package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class CastExpression implements Expression{
    private final Expression expression;
    private final Type type;

    public CastExpression(Expression expression, Type type) {
        this.expression = expression;
        this.type = type;
    }

    @Override
    public String toGLSL(TranspilationInfo info) {
        if(type == Type.INT_TYPE){
            return "int(" + expression.toGLSL(info) + ")";
        }else if(type == Type.FLOAT_TYPE){
            return "float(" + expression.toGLSL(info) + ")";
        }else if(type == Type.DOUBLE_TYPE){
            return "double(" + expression.toGLSL(info) + ")";
        }else if(type == Type.LONG_TYPE){
            return "long(" + expression.toGLSL(info) + ")";
        }else{
            throw new RuntimeException("Unsupported cast type: " + type);
        }
    }

    @Override
    public Type getType() {
        return type;
    }
}
