package me.salamander.noisetest.terra.source;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.modules.source.NoiseType;
import me.salamander.noisetest.terra.TerraModule;
import me.salamander.noisetest.util.FloatBinaryOperator;

import java.util.Set;
import java.util.function.DoubleBinaryOperator;

public class SingleNoiseSourceModule extends TerraModule {
    private FloatBinaryOperator source;
    private final NoiseType type;

    private int seed;

    public SingleNoiseSourceModule(JsonNode node, NoiseType type){
        super(node);

        source = type.apply(salt);
        this.type = type;
    }

    @Override
    public float sample(float x, float y) {
        return source.applyAsFloat(x * frequency, y * frequency);
    }

    @Override
    public void setSeed(long s) {
        source = type.apply(s + salt);
        this.seed = (int) s;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return type.glslCall(vec2Name, seedName, new float[]{1, frequency, 1, 1});
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        return type.required();
    }
}
