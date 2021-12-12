package me.salamander.ourea.glsl.transpile.tree.expression;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;

public class CompareExpression implements Expression{
    private final Expression left;
    private final Expression right;

    public CompareExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        throw new RuntimeException("Cannot directly turn comparison into GLSL");
    }

    @Override
    public Type getType() {
        return Type.INT_TYPE;
    }

    @Override
    public boolean isConstant() {
        return left.isConstant() && right.isConstant() && left.getConstantValue() instanceof Comparable<?>;
    }

    @Override
    public Object getConstantValue() {
        Object leftValue = left.getConstantValue();
        Object rightValue = right.getConstantValue();

        //Both leftValue and rightValue implement Comparable
        return ((Comparable) leftValue).compareTo(rightValue);
    }

    @Override
    public int getPrecedence() {
        throw new RuntimeException("Cannot directly turn comparison into GLSL so precedence is not applicable");
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return new CompareExpression(left.resolvePrecedingExpression(precedingExpression), right.resolvePrecedingExpression(precedingExpression));
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }
}
