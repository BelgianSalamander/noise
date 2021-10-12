package me.salamander.noisetest.terra.source;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.modules.source.NoiseType;
import me.salamander.noisetest.terra.TerraModule;

import java.util.function.DoubleBinaryOperator;

public class SingleNoiseSourceModule extends TerraModule {
    private DoubleBinaryOperator source;
    private final NoiseType type;

    private int seed;

    public SingleNoiseSourceModule(JsonNode node, NoiseType type){
        super(node);

        source = type.apply(salt);
        this.type = type;
    }

    @Override
    public double sample(double x, double y) {
        return source.applyAsDouble(x * frequency, y * frequency);
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
}
