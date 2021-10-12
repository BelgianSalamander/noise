package me.salamander.noisetest.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonHelper {
    public static int getInt(JsonNode node, String key, int defaultValue){
        JsonNode sub = node.get(key);

        if(sub == null){
            return defaultValue;
        }else{
            return sub.asInt(defaultValue);
        }
    }

    public static float getFloat(JsonNode node, String key, float defaultValue){
        JsonNode sub = node.get(key);

        if(sub == null){
            return defaultValue;
        }else{
            return (float) sub.asDouble(defaultValue);
        }
    }

    public static String getString(JsonNode node, String key, String defaultValue){
        JsonNode sub = node.get(key);

        if(sub == null){
            return defaultValue;
        }else{
            return sub.asText(defaultValue);
        }
    }
}
