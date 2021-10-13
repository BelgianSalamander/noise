package me.salamander.noisetest.terra.fractal;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.terra.TerraLoader;
import me.salamander.noisetest.terra.TerraModule;
import me.salamander.noisetest.util.JsonHelper;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FractalModule that = (FractalModule) o;
        return octaves == that.octaves && Float.compare(that.gain, gain) == 0 && Float.compare(that.lacunarity, lacunarity) == 0 && Objects.equals(function, that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(octaves, function, gain, lacunarity);
    }
}
