package me.salamander.ourea.glsl;

import me.salamander.ourea.glsl.transpile.tree.Expression;
import me.salamander.ourea.glsl.transpile.tree.statement.Statement;

import java.util.Set;

public class CompiledMethod {
    private final MethodInfo info;
    private final Statement[] code;
    private final Set<MethodInfo> dependent;

    public CompiledMethod(MethodInfo info, Statement[] code, Set<MethodInfo> dependent) {
        this.info = info;
        this.code = code;
        this.dependent = dependent;
    }

    public MethodInfo getInfo() {
        return info;
    }

    public Statement[] getCode() {
        return code;
    }

    public Set<MethodInfo> getDependent() {
        return dependent;
    }
}
