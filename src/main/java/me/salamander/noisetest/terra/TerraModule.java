package me.salamander.noisetest.terra;

import com.fasterxml.jackson.databind.JsonNode;
import me.salamander.noisetest.glsl.GLSLCompilable;
import me.salamander.noisetest.modules.NoiseModule;

public abstract class TerraModule implements GLSLCompilable {
    protected final int salt;
    protected final float frequency;
    protected final int dimensions;

    protected TerraModule(JsonNode node){
        JsonNode saltElement = node.get("salt");
        if(saltElement == null){
            salt = 0;
        }else{
            salt = saltElement.asInt();
        }

        JsonNode frequencyElement = node.get("frequency");
        if(frequencyElement == null){
            frequency = 0.02f;
        }else{
            frequency = (float) frequencyElement.asDouble();
        }

        JsonNode dimensionsElement = node.get("dimensions");
        if(dimensionsElement == null){
            dimensions = 2;
        }else{
            dimensions = dimensionsElement.asInt();
        }

        if(dimensions != 2){
            throw new IllegalArgumentException("Only 2 dimensions is supported");
        }
    }

    protected TerraModule(JsonNode node, float defaultFrequency){
        JsonNode saltElement = node.get("salt");
        if(saltElement == null){
            salt = 0;
        }else{
            salt = saltElement.asInt();
        }

        JsonNode frequencyElement = node.get("frequency");
        if(frequencyElement == null){
            frequency = defaultFrequency;
        }else{
            frequency = (float) frequencyElement.asDouble();
        }

        JsonNode dimensionsElement = node.get("dimensions");
        if(dimensionsElement == null){
            dimensions = 2;
        }else{
            dimensions = dimensionsElement.asInt();
        }

        if(dimensions != 2){
            throw new IllegalArgumentException("Only 2 dimensions is supported");
        }
    }
}
