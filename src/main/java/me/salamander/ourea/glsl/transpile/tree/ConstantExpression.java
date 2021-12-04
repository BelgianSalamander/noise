package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class ConstantExpression implements Expression{
    private Object value;

    public ConstantExpression(Object value){
        this.value = value;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        if(value instanceof String){
            return "\"" + value + "\"";
        }else if(value instanceof Integer){
            return value.toString();
        }else if(value instanceof Float){
            return value.toString() + "f";
        }else if(value instanceof Double){
            return value.toString() + "d";
        }else if(value instanceof Long){
            return value.toString() + "l";
        }else{
            throw new RuntimeException("Unsupported constant type: " + value.getClass());
        }
    }

    @Override
    public Type getType() {
        if(value instanceof String){
            return Type.getType(String.class);
        }else if(value instanceof Integer){
            return Type.INT_TYPE;
        }else if(value instanceof Float){
            return Type.FLOAT_TYPE;
        }else if(value instanceof Double){
            return Type.DOUBLE_TYPE;
        }else if(value instanceof Long){
            return Type.LONG_TYPE;
        }else{
            throw new RuntimeException("Unsupported constant type: " + value.getClass());
        }
    }
}
