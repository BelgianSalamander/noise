package me.salamander.noisetest.gui;

import me.salamander.noisetest.gui.panels.GUINoiseModule;
import me.salamander.noisetest.modules.combiner.Add;
import me.salamander.noisetest.modules.source.CheckerBoard;
import me.salamander.noisetest.modules.source.Const;
import me.salamander.noisetest.modules.source.Perlin;

import java.util.function.Supplier;

public class Modules {
    private static Parameter[] PERLIN_PARAMETERS = new Parameter[]{
            new Parameter("Octaves", 0, 1, 10, 1),
            new Parameter("Frequency", 1, 0.1, 5, 0.1),
            new Parameter("Persistence", 2, 0.1, 1.0, 0.1),
            new Parameter("Lacunarity", 3, 1, 5, 0.1)
    };
    private static Parameter[] CONST_PARAMETERS = new Parameter[]{
            new Parameter("Value", 0, -1, 1, 0.1)
    };
    private static Parameter[] FREQUENCY_ONLY = new Parameter[]{
            new Parameter("Frequency", 0, 0.1, 5, 0.1)
    };
    private static Parameter[] NO_PARAMETERS = new Parameter[0];



    private static String[] NO_INPUTS = new String[0];
    private static String[] ONE_INPUT = new String[]{"Source"};
    private static String[] TWO_INPUTS = new String[]{"Source One", "Source Two"};
    private static String[] SELECT = new String[]{"Source One", "Source Two", "Selector"};


    public static Supplier<GUINoiseModule> PERLIN = () -> new GUINoiseModule("Perlin", new Perlin(), PERLIN_PARAMETERS, NO_INPUTS);
    public static Supplier<GUINoiseModule> CONST = () -> new GUINoiseModule("Const", new Const(), CONST_PARAMETERS, NO_INPUTS);
    public static Supplier<GUINoiseModule> ADD = () -> new GUINoiseModule("Add", new Add(null, null), NO_PARAMETERS, TWO_INPUTS);
    public static Supplier<GUINoiseModule> CHECKERBOARD = () -> new GUINoiseModule("Checkerboard", new CheckerBoard(), FREQUENCY_ONLY, NO_INPUTS);
}
