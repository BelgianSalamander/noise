package me.salamander.noisetest.terra.normalize;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.util.JsonHelper;

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
}
