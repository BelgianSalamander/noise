package me.salamander.ourea.glsl.transpile.tree.statement;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.expression.Expression;
import org.objectweb.asm.tree.LabelNode;

public class GotoStatement implements Statement {
    private final LabelNode label;

    public GotoStatement(LabelNode label){
        this.label = label;
    }


    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "[PSEUDO_STATEMENT] Goto " + label.getLabel() + ";\n";
    }

    @Override
    public GotoStatement resolvePrecedingExpression(Expression precedingExpression) {
        return this;
    }
}
