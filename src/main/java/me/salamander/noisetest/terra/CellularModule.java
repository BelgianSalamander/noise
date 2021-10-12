package me.salamander.noisetest.terra;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.source.NoiseSourceModule;
import me.salamander.noisetest.modules.source.NoiseType;
import me.salamander.noisetest.noise.VoronoiSampler;
import me.salamander.noisetest.util.JsonHelper;

import java.util.HashMap;
import java.util.Map;

public class CellularModule extends TerraModule {
    private final DistanceFunction distanceFunction;
    private final ReturnFunction returnFunction;
    private final NoiseModule lookup;

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

        float bestDistance = 100000;
        float distanceTwo = 1000000;
        float distanceThree = 10000000;

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

    enum DistanceFunction{
        MANHATTAN{
            @Override
            float distance(float x1, float y1, float x2, float y2) {
                return Math.abs(x1 - x2) + Math.abs(y1 - y2);
            }
        },
        EUCLIDEAN{
            @Override
            float distance(float x1, float y1, float x2, float y2) {
                return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
            }
        },
        EUCLIDEANSQ{
            @Override
            float distance(float x1, float y1, float x2, float y2) {
                return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
            }
        },
        HYBRID{
            @Override
            float distance(float x1, float y1, float x2, float y2) {
                return ((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + Math.abs(x1 - x2) + Math.abs(y1 - y2)) / 2;
            }
        };

        abstract float distance(float x1, float y1, float x2, float y2);

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
        },
        DISTANCE{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1;
            }
        },
        DISTANCE2{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d2;
            }
        },
        DISTANCE3{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return 0;
            }
        },
        DISTANCE2ADD{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 + d2;
            }
        },
        DISTANCE2SUB{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 - d2;
            }
        },
        DISTANCE2MUL{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 * d2;
            }
        },
        DISTANCE2DIV{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 / d2;
            }
        },
        DISTANCE3ADD{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 + d3;
            }
        },
        DISTANCE3SUB{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 - d3;
            }
        },
        DISTANCE3MUL{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 * d3;
            }
        },
        DISTANCE3DIV{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return d1 / d3;
            }
        },
        NOISELOOKUP{
            @Override
            float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y) {
                return (float) lookup.sample(x, y);
            }
        };

        abstract float lookup(float d1, float d2, float d3, NoiseModule lookup, float x, float y);

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
}
