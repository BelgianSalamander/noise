package me.salamander.noisetest.terra.normalize;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.terra.TerraLoader;
import me.salamander.noisetest.terra.TerraModule;

public abstract class NormalizerModule extends TerraModule {
    protected final NoiseModule function;

    public NormalizerModule(JsonNode node) {
        super(node);
        this.function = TerraLoader.loadFunction(node.get("function"));
    }

    @Override
    public void setSeed(long s) {
        function.setSeed(s + salt);
    }

    @Override
    public long getSeed() {
        return function.getSeed() - salt;
    }
}
