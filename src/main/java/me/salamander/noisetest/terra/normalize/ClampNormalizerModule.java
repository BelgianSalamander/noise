package me.salamander.noisetest.terra.normalize;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.util.JsonHelper;

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
}
