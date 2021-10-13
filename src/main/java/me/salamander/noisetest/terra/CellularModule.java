package me.salamander.noisetest.terra;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.glsl.FormattableText;
import me.salamander.noisetest.glsl.FunctionInfo;
import me.salamander.noisetest.glsl.FunctionRegistry;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.source.NoiseSourceModule;
import me.salamander.noisetest.modules.source.NoiseType;
import me.salamander.noisetest.noise.VoronoiSampler;
import me.salamander.noisetest.util.JsonHelper;
import me.salamander.noisetest.util.Util;

import java.io.IOException;
import java.util.*;

public class CellularModule extends TerraModule {
    private static final FormattableText code;

    private final DistanceFunction distanceFunction;
    private final ReturnFunction returnFunction;
    private final NoiseModule lookup;
    private final Function function = new Function();

    private int seed;

    public CellularModule(JsonNode data){
        super(data);

        returnFunction = ReturnFunction.get(JsonHelper.getString(data, "return", "Distance").toUpperCase());
        distanceFunction = DistanceFunction.get(JsonHelper.getString(data, "distance", "EuclideanSq").toUpperCase());

        JsonNode lookupNode = data.get("lookup");
        if(lookupNode == null){
            lookup = new NoiseSourceModule(1, salt, NoiseType.OPEN_SIMPLEX2S);
        }else{
            lookup = TerraLoader.loadFunction(lookupNode);
        }
    }

    @Override
    public float sample(float startX, float startY) {
        float x = (float) (startX * frequency);
        float y = (float) (startY * frequency);

        int baseX = (int) Math.floor(x);
        int baseY = (int) Math.floor(y);

        float bestX = 0;
        float bestY = 0;

        float bestDistance = Float.MAX_VALUE;
        float distanceTwo = Float.MAX_VALUE;
        float distanceThree = Float.MAX_VALUE;

        for(int xo = -1; xo <= 1; xo++){
            int gridX = baseX + xo;

            for(int yo = -1; yo <= 1; yo++){
                int gridY = baseY + yo;

                float jitteredX = gridX + (0.5f * VoronoiSampler.randomfloat(gridX, gridY, seed));
                float jitteredY = gridY + (0.5f * VoronoiSampler.randomfloat(gridX, gridY, seed + 1));
                float distance = distanceFunction.distance(startX, startY, jitteredX / frequency, jitteredY / frequency);

                if(distance < bestDistance){
                    distanceThree = distanceTwo;
                    distanceTwo = bestDistance;
                    bestDistance = distance;

                    bestX = jitteredX;
                    bestY = jitteredY;
                }else if(distance < distanceTwo){
                    distanceThree = distanceTwo;
                    distanceTwo = distance;
                }else if(distance < distanceThree){
                    distanceThree = distance;
                }
            }
        }

        bestX /= frequency;
        bestY /= frequency;

        return returnFunction.lookup(bestDistance, distanceTwo, distanceThree, lookup, bestX, bestY);
    }

    @Override
    public long getSeed() {
        return seed - salt;
    }

    @Override
    public void setSeed(long s) {
        this.seed = seed + salt;
        this.lookup.setSeed(seed + salt);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellularModule that = (CellularModule) o;
        return seed == that.seed && distanceFunction == that.distanceFunction && returnFunction == that.returnFunction && Objects.equals(lookup, that.lookup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distanceFunction, returnFunction, lookup, seed);
    }

    enum DistanceFunction{
        MANHATTAN{
            @Override
            float distance(float x1, float y1, float x2, float y2) {
                return Math.abs(x1 - x2) + Math.abs(y1 - y2);
            }

            @Override
            String glslExpression(String first, String second) {
                return "sum(abs(" + first + " - " + second + "))";
            }
        },
        EUCLIDEAN{
            @Override
            float distance(float x1, float y1, float x2, float y2) {
                return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
            }

            @Override
            String glslExpression(String first, String second) {
                return "length(" + first + " - " + second + ")";
            }
        },
        EUCLIDEANSQ{
            @Override
            float distance(float x1, float y1, float x2, float y2) {
                return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
            }

            @Override
            String glslExpression(String first, String second) {
                return "dot(" + first + " - " + second + ", " + first + " - " + second + ")";
            }
        },
        HYBRID{
            @Override
            float distance(float x1, float y1, float x2, float y2) {
                return ((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + Math.abs(x1 - x2) + Math.abs(y1 - y2)) / 2;
            }

            @Override
            String glslExpression(String first, String second) {
                return "(sum(abs(" + first + " - " + second + ")) + dot(" + first + " - " + second + ", " + first + " - " + second + ")) / 2.f";
            }
        };

        abstract float distance(float x1, float y1, float x2, float y2);

        abstract String glslExpression(String first, String second);

        private static final Map<String, DistanceFunction> valueLookup = new HashMap<>();
        public static DistanceFunction get(String key){
            return valueLookup.get(key);
        }

        static {
            for (DistanceFunction value : values()) {
                valueLookup.put(value.toString(), value);
            }
        }
    }

    enum ReturnFunction{
        CELLVALUE{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return (float) lookup.sample(x, y); //TODO: This is probably wrong. Wiki isn't clear
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return call;
            }
        },
        DISTANCE{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d1;
            }
        },
        DISTANCE2{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d2;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d2;
            }
        },
        DISTANCE3{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return 0;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d3;
            }
        },
        DISTANCE2ADD{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 + d2;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d1 + " + " + d2;
            }
        },
        DISTANCE2SUB{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 - d2;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d1 + " - " + d2;
            }
        },
        DISTANCE2MUL{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 * d2;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d1 + " * " + d2;
            }
        },
        DISTANCE2DIV{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 / d2;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d1 + " / " + d2;
            }
        },
        DISTANCE3ADD{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 + d3;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d1 + " + " + d3;
            }
        },
        DISTANCE3SUB{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 - d3;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d1 + " - " + d3;
            }
        },
        DISTANCE3MUL{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 * d3;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d1 + " * " + d3;
            }
        },
        DISTANCE3DIV{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 / d3;
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return d1 + " / " + d3;
            }

        },
        NOISELOOKUP{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return (float) lookup.sample(x, y);
            }

            @Override
            String glslExpression(String d1, String d2, String d3, String call) {
                return call;
            }
        };

        abstract float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y);
        abstract String glslExpression(String d1, String d2, String d3, String call);

        private static final Map<String, ReturnFunction> valueLookup = new HashMap<>();
        public static ReturnFunction get(String key){
            return valueLookup.get(key);
        }

        static {
            for(ReturnFunction func: values()){
                valueLookup.put(func.toString(), func);
            }
        }
    }

    private class Function implements FunctionInfo{

        @Override
        public String name() {
            return "cellular_" + Math.abs(CellularModule.this.hashCode());
        }

        @Override
        public String generateCode() {
            Map<String, Object> data = new HashMap<>();

            data.put("name", name());
            data.put("frequency", frequency);
            data.put("reverseFrequency", 1 / frequency);
            data.put("distance", distanceFunction.glslExpression("(jitteredPoint * " + (1 / frequency) + ")", "pos"));
            data.put("lookup", returnFunction.glslExpression("bestDistance", "distanceTwo", "distanceThree", ((GLSLCompilable) lookup).glslExpression("best", "seed")));

            return code.evaluate(data);
        }

        @Override
        public String forwardDeclaration() {
            return "float " + name() + "(vec2, int)";
        }

        @Override
        public Set<FunctionInfo> requiredFunctions() {
            Set<FunctionInfo> required = new HashSet<>();
            required.add(FunctionRegistry.getFunction("getVec"));
            if(distanceFunction == DistanceFunction.MANHATTAN || distanceFunction == DistanceFunction.HYBRID){
                required.add(FunctionRegistry.getFunction("sum"));
            }

            if(returnFunction == ReturnFunction.CELLVALUE || returnFunction == ReturnFunction.NOISELOOKUP){
                required.addAll(((GLSLCompilable) lookup).requiredFunctions());
            }
            return required;
        }
    }

    static {
        try{
            code = new FormattableText(Util.loadResource("/glsl/extra/cellular.func"));
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't load cellular function");
        }
    }
}
