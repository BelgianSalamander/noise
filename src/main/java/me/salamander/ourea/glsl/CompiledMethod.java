package me.salamander.ourea.glsl;

import me.salamander.ourea.glsl.transpile.tree.Expression;

import java.util.Set;

public class CompiledMethod {
    private final MethodInfo info;
    private final Expression[] code;
    private final Set<MethodInfo> dependent;

    public CompiledMethod(MethodInfo info, Expression[] code, Set<MethodInfo> dependent) {
        this.info = info;
        this.code = code;
        this.dependent = dependent;
    }

    public MethodInfo getInfo() {
        return info;
    }

    public Expression[] getCode() {
        return code;
    }

    public Set<MethodInfo> getDependent() {
        return dependent;
    }
}
