package me.salamander.ourea.glsl.transpile.tree;

import me.salamander.ourea.glsl.transpile.TranspilationInfo;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;
import org.objectweb.asm.Type;

public class ContinueStatement implements Statement {
    @Override
    public String toGLSL(TranspilationInfo info, int depth) {
        return "  ".repeat(depth) + "continue;\n";
    }

    @Override
    public Statement resolvePrecedingExpression(Expression precedingExpression) {
        return this;
    }
}
