package me.salamander.noisetest.gui;

import me.salamander.noisetest.color.ColorGradient;
import me.salamander.noisetest.color.ColorSampler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ColorSamplers {
    private static final HashMap<String, ColorSampler> samplers = new HashMap<>();
    private static final List<String> samplerNames = new ArrayList<>();

    public static ColorSampler getSampler(String name){
        return samplers.get(name);
    }

    public static List<String> getNames(){
        return samplerNames;
    }

    public static void addSampler(String name, ColorSampler sampler){
        if(samplers.containsKey(name)){
            throw new IllegalStateException("Tried to re-add sampler '" + name + "'");
        }

        samplerNames.add(name);
        samplers.put(name, sampler);
    }

    static {
        addSampler("Terrain", ColorGradient.TERRAIN);
        addSampler("Mountain", ColorGradient.MOUNTAIN);
        addSampler("Gray", ColorGradient.DEFAULT);
    }
}
