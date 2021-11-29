package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class GetFieldExpression implements Expression{
    private Expression expression;
    private String field;
    private String desc;

    public GetFieldExpression(Expression expression, String field, String desc){
        this.expression = expression;
        this.field = field;
        this.desc = desc;
    }

    @Override
    public String toGLSL(TranspilationInfo info) {
        return expression.toGLSL(info) + "." + field;
    }

    @Override
    public Type getType() {
        return Type.getType(desc);
    }
}
