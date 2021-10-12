package me.salamander.noisetest.terra.normalize;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.jshell.spi.ExecutionControl;
import me.salamander.noisetest.util.JsonHelper;

public class NormalNormalizerModule extends NormalizerModule{
    private final float mean;
    private final float standardDeviation;

    public NormalNormalizerModule(JsonNode node) {
        super(node);

        this.mean = JsonHelper.getFloat(node, "mean", 0);
        this.standardDeviation = JsonHelper.getFloat(node, "standard-deviation", 1);
    }

    @Override
    public float sample(float x, float y) {
        throw new AssertionError("Not Implemented");
    }
}
