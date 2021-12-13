package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import org.objectweb.asm.Type;

public class ActualCompareExpression extends Condition {
    private final Expression left;
    private final Expression right;
    private final JumpIfEqualStatement.Operator operator;

    public ActualCompareExpression(Expression left, Expression right, JumpIfEqualStatement.Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public ActualCompareExpression negate(){
        return new ActualCompareExpression(left, right, operator.getOpposite());
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return left.toGLSL(info, 0) + " " + operator.getSymbol() + " " + right.toGLSL(info, 0);
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN_TYPE;
    }

    @Override
    public boolean isConstant() {
        return left.isConstant() && right.isConstant();
    }

    @Override
    public Object getConstantValue() {
        return operator.apply((Integer) left.getConstantValue(), (Integer) right.getConstantValue());
    }

    @Override
    public int getPrecedence() {
        return operator.getPrecedence();
    }

    @Override
    public Expression resolvePrecedingExpression(Expression precedingExpression) {
        return new ActualCompareExpression(left.resolvePrecedingExpression(precedingExpression), right.resolvePrecedingExpression(precedingExpression), operator);
    }
}
