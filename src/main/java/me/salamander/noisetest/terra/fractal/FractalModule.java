package me.salamander.noisetest.terra.fractal;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.terra.TerraLoader;
import me.salamander.noisetest.terra.TerraModule;
import me.salamander.noisetest.util.JsonHelper;

public abstract class FractalModule extends TerraModule {
    protected final int octaves;
    protected final NoiseModule function;
    protected final float gain;
    protected final float lacunarity;
    //TODO: weighted-strength

    public FractalModule(JsonNode data){
        super(data, 1.0f);

        function = TerraLoader.loadFunction(data.get("function"));

        octaves = JsonHelper.getInt(data, "octaves", 3);
        gain = JsonHelper.getFloat(data, "gain", 0.5f);
        lacunarity = JsonHelper.getFloat(data, "lacunarity", 2.0f);
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
