package me.salamander.noisetest.terra;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.modifier.Turbulence;
import me.salamander.noisetest.util.JsonHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DomainWarp extends TerraModule{
    private final NoiseModule source;
    private final NoiseModule warp;
    private final float amplitude;

    private final Function function = new Function();

    public DomainWarp(JsonNode node) {
        super(node);

        source = TerraLoader.loadFunction(node.get("function"));
        warp = TerraLoader.loadFunction(node.get("warp"));

        amplitude = JsonHelper.getFloat(node, "amplitude", 1);
    }

    @Override
    public float sample(float x, float y) {
        final float x0 = (x + (12148.0f / 65536.0f));
        final float y0 = (y + (56346.0f / 65536.0f));
        final float x1 = (x + 134 + (23436.0f / 65536.0f));
        final float y1 = (y + -68 + (43765.0f / 65536.0f));

        float offsetX = warp.sample(x0, y0) * amplitude;
        float offsetY = warp.sample(x1, y1) * amplitude;

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

    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return function.name() + "(" + vec2Name + ", " + seedName + ")";
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        Set<FunctionInfo> required = new HashSet<>();
        required.add(function);
        return required;
    }

    private final class Function implements FunctionInfo {

        @Override
        public String name() {
            return "warp_" + Math.abs(DomainWarp.this.hashCode());
        }

        @Override
        public String generateCode() {
            if(source instanceof GLSLCompilable compilable && warp instanceof GLSLCompilable warp){
                Map<String, Object> lookup = new HashMap<>();

                lookup.put("name", name());

                lookup.put("turbulencePower", amplitude);

                lookup.put("sampleX", warp.glslExpression("sampleX", "seed"));
                lookup.put("sampleY", warp.glslExpression("sampleY", "seed * 968177961"));

                lookup.put("sample", compilable.glslExpression("distortedPos", "seed + 60874159"));

                return Turbulence.functionCode.evaluate(lookup);
            }else{
                throw new IllegalStateException("Can't compile turbulence source");
            }
        }

        @Override
        public String forwardDeclaration() {
            return "float " + name() + " (vec2, int)";
        }

        @Override
        public Set<FunctionInfo> requiredFunctions() {
            if(source instanceof GLSLCompilable compilable && warp instanceof GLSLCompilable warp){
                Set<FunctionInfo> combined = new HashSet<>(warp.requiredFunctions());
                combined.addAll(compilable.requiredFunctions());
                return combined;
            }else{
                throw new IllegalStateException("Can't compile turbulence source");
            }
        }
    }
}
