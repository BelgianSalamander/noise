package me.salamander.noisetest.gui;

import me.salamander.noisetest.modules.source.Perlin;

import java.util.function.Supplier;

public class Modules {
    public static Supplier<GUINoiseModule> PERLIN = () -> new GUINoiseModule("Perlin", new Perlin());
}
