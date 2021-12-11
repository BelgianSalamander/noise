package me.salamander.ourea.glsl.transpile.tree.comparison;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.Expression;
import org.objectweb.asm.Type;

public class TernaryExpression implements Expression {
    private Condition condition;
    private Expression ifTrue;
    private Expression ifFalse;

    public TernaryExpression(Condition condition, Expression ifTrue, Expression ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;

        if(condition.getType() != Type.BOOLEAN_TYPE) {
            throw new RuntimeException("Condition must be a boolean");
        }

        if(ifTrue.getType() != ifFalse.getType()) {
            throw new RuntimeException("If true and if false must have the same type");
        }
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "(" + condition.toGLSL(info, depth) + " ? " + ifTrue.toGLSL(info, depth) + " : " + ifFalse.toGLSL(info, depth) + ")";
    }

    @Override
    public Type getType() {
        return ifTrue.getType();
    }

    @Override
    public boolean isConstant() {
        if(condition.isConstant()){
            boolean conditionValue = (boolean) condition.getConstantValue();
            if(conditionValue) {
                return ifTrue.isConstant();
            }else{
                return ifFalse.isConstant();
            }
        }
        return false;
    }

    @Override
    public Object getConstantValue() {
        if((boolean) condition.getConstantValue()){
            return ifTrue.getConstantValue();
        }else{
            return ifFalse.getConstantValue();
        }
    }
}
