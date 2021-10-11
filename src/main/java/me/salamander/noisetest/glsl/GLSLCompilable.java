package me.salamander.noisetest.glsl;

import me.salamander.noisetest.modules.NoiseModule;

import java.util.Set;

public interface GLSLCompilable extends NoiseModule {
    String glslExpression(String vec2Name, String seedName);

    Set<FunctionInfo> requiredFunctions();
}
