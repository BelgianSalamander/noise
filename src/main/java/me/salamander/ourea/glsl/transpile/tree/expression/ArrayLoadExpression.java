package me.salamander.ourea.glsl.transpile.tree.expression;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class ArrayLoadExpression implements Expression {
    private final Expression array;
    private final Expression index;

    public ArrayLoadExpression(Expression array, Expression index) {
        this.array = array;
        this.index = index;
    }


    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return eval(array, info) + "[" + index.toGLSL(info, depth) + "]";
    }

    @Override
    public Type getType() {
        return array.getType().getElementType();
    }

    @Override
    public boolean isConstant() {
        return array.isConstant() && index.isConstant();
    }

    @Override
    public Object getConstantValue() {
        return ((Object[]) array.getConstantValue())[(int) index.getConstantValue()];
    }

    @Override
    public int getPrecedence() {
        return 2;
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return new ArrayLoadExpression(array.resolvePrecedingExpression(precedingExpression), index.resolvePrecedingExpression(precedingExpression));
    }
}
