package me.salamander.noisetest.terra;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.util.JsonHelper;

public class DomainWarp extends TerraModule{
    private final NoiseModule source;
    private final NoiseModule warp;
    private final float amplitude;

    public DomainWarp(JsonNode node) {
        super(node);

        source = TerraLoader.loadFunction(node.get("function"));
        warp = TerraLoader.loadFunction(node.get("warp"));

        amplitude = JsonHelper.getFloat(node, "amplitude", 1);
    }

    @Override
    public float sample(float x, float y) {
        final float x0 = (float) (x + (12148.0f / 65536.0f));
        final float y0 = (float) (y + (56346.0f / 65536.0f));
        final float x1 = (float) (x + 134 + (23436.0f / 65536.0f));
        final float y1 = (float) (y + -68 + (43765.0f / 65536.0f));

        float offsetX = (float) warp.sample(x0, y0) * amplitude;
        float offsetY = (float) warp.sample(x1, y1) * amplitude;

        return source.sample(x + offsetX, y + offsetY);
    }

    @Override
    public void setSeed(long s) {
        source.setSeed(s + salt);
        warp.setSeed(s + salt);
    }

    @Override
    public long getSeed() {
        return source.getSeed() - salt;
    }
}
