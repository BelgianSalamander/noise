package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LabelNode;

public class GotoExpression implements Expression{
    private final LabelNode label;

    public GotoExpression(LabelNode label){
        this.label = label;
    }


    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "[PSEUDO_STATEMENT] Goto " + label.getLabel() + ";\n";
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Object getConstantValue() {
        throw new RuntimeException("GotoExpression is not constant");
    }
}
