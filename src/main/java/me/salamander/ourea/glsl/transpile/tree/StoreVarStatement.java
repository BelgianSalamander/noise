package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;
import org.objectweb.asm.Type;

public class StoreVarStatement implements Statement {
    private Expression value;
    private int var;

    public StoreVarStatement(Expression value, int var) {
        this.value = value;
        this.var = var;
    }

    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "var" + var + " = " + value.toGLSL(info, 0) + ";\n";
    }

    @Override
    public Statement resolvePrecedingExpression(Expression precedingExpression) {
        value = value.resolvePrecedingExpression(precedingExpression);
        return this;
    }
}
