package me.salamander.noisetest.glsl;

import me.salamander.noisetest.modules.types.NoiseModule;

public interface GLSLCompilable extends NoiseModule {
    String glslExpression(String vec2Name, String seedName);

    FunctionInfo[] requiredMethods();
}
