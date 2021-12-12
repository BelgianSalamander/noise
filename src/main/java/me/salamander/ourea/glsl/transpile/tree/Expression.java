package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;


public interface Expression extends CodeFragment<Expression> {
    Type getType();

    boolean isConstant();
    Object getConstantValue();
}
