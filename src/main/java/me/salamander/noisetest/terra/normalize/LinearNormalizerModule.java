package me.salamander.noisetest.terra.normalize;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.util.JsonHelper;

import java.util.HashSet;
import java.util.Set;

public class LinearNormalizerModule extends NormalizerModule{
    private final float min, max;
    private final float divisor;

    public LinearNormalizerModule(JsonNode node) {
        super(node);

        this.min = JsonHelper.getFloat(node, "min", -1);
        this.max = JsonHelper.getFloat(node, "max", 1);

        divisor = 2 / (max - min);
    }

    @Override
    public float sample(float x, float y) {
        return (function.sample(x, y) - min) * divisor - 1;
    }

    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return "(" + ((GLSLCompilable) function).glslExpression(vec2Name, seedName) + " - " + min + ") * " + divisor + " - 1";
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        return new HashSet<>(0);
    }
}
