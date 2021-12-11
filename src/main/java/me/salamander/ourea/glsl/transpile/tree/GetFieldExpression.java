package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;

public class GetFieldExpression implements Expression{
    private final Expression expression;
    private final String field;
    private final String desc;

    private Field field_ = null; //Cache field

    public GetFieldExpression(Expression expression, String field, String desc){
        this.expression = expression;
        this.field = field;
        this.desc = desc;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return expression.toGLSL(info, 0) + "." + field;
    }

    @Override
    public Type getType() {
        return Type.getType(desc);
    }

    @Override
    public boolean isConstant() {
        return expression.isConstant();
    }

    @Override
    public Object getConstantValue() {
        loadField();
        try {
            return field_.get(expression.getConstantValue());
        }catch (IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    private void loadField(){
        if(field_ == null){
            try {
                Class<?> clazz = expression.getClass();
                while(clazz != null && field_ == null){
                    field_ = clazz.getDeclaredField(field);
                    clazz = clazz.getSuperclass();
                }
                field_.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
