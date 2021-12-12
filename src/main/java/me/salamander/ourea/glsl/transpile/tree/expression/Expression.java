package me.salamander.ourea.glsl.transpile.tree.expression;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.CodeFragment;
import org.objectweb.asm.Type;


public interface Expression extends CodeFragment<Expression> {
    Type getType();

    boolean isConstant();
    Object getConstantValue();

    int getPrecedence();

    default String eval(Expression expr, TranspilationInfo info){
        String base = expr.toGLSL(info, 0);
        if(expr.getPrecedence() > getPrecedence()){
            return "(" + base + ")";
        }
        return base;
    }
}
