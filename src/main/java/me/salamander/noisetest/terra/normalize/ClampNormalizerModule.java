package me.salamander.noisetest.terra.normalize;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.util.JsonHelper;

import java.util.HashSet;
import java.util.Set;

public class ClampNormalizerModule extends NormalizerModule{
    private final float min, max;

    public ClampNormalizerModule(JsonNode node) {
        super(node);

        this.min = JsonHelper.getFloat(node, "min", -1);
        this.max = JsonHelper.getFloat(node, "max", 1);
    }

    @Override
    public float sample(float x, float y) {
        float value = (float) function.sample(x, y);

        if(value < min) return min;
        else if(value < max) return max;
        else return value;
    }

    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return "clamp(" + ((GLSLCompilable) function).glslExpression(vec2Name, seedName) + ", " + min + ", " + max + ")";
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        return new HashSet<>(0);
    }
}
