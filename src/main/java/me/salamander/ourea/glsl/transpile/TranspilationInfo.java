package me.salamander.ourea.glsl.transpile;

import me.salamander.ourea.glsl.transpile.method.MethodResolver;

import java.util.HashMap;
import java.util.Map;

public class TranspilationInfo {
    private final Map<String, MethodResolver> methodResolvers = new HashMap<>();

    void addMethodResolver(String owner, String name, String desc, MethodResolver resolver) {
        methodResolvers.put(owner + "#" + name + " " + desc, resolver);
    }

    public MethodResolver getMethodResolver(String owner, String name, String desc) {
        return methodResolvers.get(owner + "#" + name + " " + desc);
    }
}
