package me.salamander.noisetest.terra.fractal;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.glsl.FormattableText;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.render.api.GLUtil;
import me.salamander.noisetest.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FBM extends FractalModule {
    private static final FormattableText code;

    private Function glslFunction = new Function();

    public FBM(JsonNode data){
        super(data);
    }

    @Override
    public float sample(float x, float y) {
        float total = 0;

        float currentGain = gain;
        float currentFrequency = 1;

        for(int i = 0; i < octaves; i++){
            total += currentGain * function.sample(x * currentFrequency, y * currentFrequency);

            //Scramble x and y
            x += ((getSeed() ^ 4726474728L) * i) & 0xffff - 0x8000;
            y += ((getSeed() ^ 5467757689L) * i) & 0xffff - 0x8000;

            //Change gain and freq
            currentGain *= gain;
            currentFrequency *= lacunarity;
        }

        return total;
    }



    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return glslFunction.name() + "(" + vec2Name + ", " + seedName + ")";
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        Set<FunctionInfo> required = new HashSet<>(1);
        required.add(glslFunction);
        return required;
    }

    private class Function implements FunctionInfo{
        @Override
        public String name() {
            return "fractal_" + Math.abs(FBM.this.hashCode());
        }

        @Override
        public String generateCode() {
            Map<String, Object> data = new HashMap<>();

            data.put("frequency", frequency);
            data.put("name", name());
            data.put("numOctaves", octaves);
            data.put("persistence", gain);
            data.put("lacunarity", lacunarity);

            data.put("sample", ((GLSLCompilable) function).glslExpression("pos * actualFrequency", "seed"));

            return code.evaluate(data);
        }

        @Override
        public String forwardDeclaration() {
            return "float " + name() + "(vec2 pos, int seed)";
        }

        @Override
        public Set<FunctionInfo> requiredFunctions() {
            return ((GLSLCompilable) function).requiredFunctions();
        }
    }

    static {
        try {
            code = new FormattableText(Util.loadResource("/glsl/extra/fractal.func"));
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read code");
        }
    }
}
