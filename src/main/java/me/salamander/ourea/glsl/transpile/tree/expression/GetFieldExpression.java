package me.salamander.ourea.glsl.transpile.tree.expression;

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
        Type t = Type.getType(desc);
        if(info.isReferenceField(t, field)){
            return t.getClassName().replace('.', '_').replace('$', '_') + "_constants[" + eval(expression, info) + "." + field + "]";
        }
        return eval(expression, info) + "." + field;
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

    @Override
    public int getPrecedence() {
        return 2;
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return new GetFieldExpression(expression.resolvePrecedingExpression(precedingExpression), field, desc);
    }

    private void loadField(){
        if(field_ == null){
            try {
                Object obj = expression.getConstantValue();
                Class<?> clazz = obj.getClass();
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
