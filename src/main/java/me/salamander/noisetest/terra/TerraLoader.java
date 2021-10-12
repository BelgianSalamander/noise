package me.salamander.noisetest.terra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.source.NoiseSourceModule;
import me.salamander.noisetest.modules.source.NoiseType;
import me.salamander.noisetest.terra.fractal.FBM;
import me.salamander.noisetest.terra.normalize.ClampNormalizerModule;
import me.salamander.noisetest.terra.normalize.LinearNormalizerModule;
import me.salamander.noisetest.terra.source.SingleNoiseSourceModule;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class TerraLoader {
    private static final YAMLMapper YAML_MAPPER = new YAMLMapper();
    private static final Map<String, Function<JsonNode, NoiseModule>> noiseTypes = new HashMap<>();

    public static NoiseModule loadTerraNoise(String yaml) throws JsonProcessingException {
        JsonNode root = YAML_MAPPER.readTree(yaml);

        return loadFunction(root);
    }

    public static NoiseModule loadFunction(JsonNode root) {
        String type = root.get("type").asText();
        System.out.println("Loading: " + type);

        Function<JsonNode, NoiseModule> generator = noiseTypes.get(type.toUpperCase());

        if(generator == null){
            System.out.println("Unknown Noise Type: " + type);
            return new NoiseModule() {
                @Override
                public float sample(float x, float y) {
                    return 0;
                }

                @Override
                public long getSeed() {
                    return 0;
                }
            };
        }else{
            return generator.apply(root);
        }
    }

    public static void registerType(String name, Function<JsonNode, NoiseModule> generator){
        noiseTypes.put(name.toUpperCase(), generator);
    }

    static {
        registerType("FBM", FBM::new);

        registerType("CELLULAR", CellularModule::new);

        registerType("DOMAINWARP", DomainWarp::new);

        registerType("OpenSimplex2S", (node) -> new SingleNoiseSourceModule(node, NoiseType.OPEN_SIMPLEX2S));
        registerType("Perlin", (node) -> new SingleNoiseSourceModule(node, NoiseType.PERLIN));

        registerType("Linear", LinearNormalizerModule::new);
        registerType("Clamp", ClampNormalizerModule::new);
    }
}
