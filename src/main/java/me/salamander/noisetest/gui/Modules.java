package me.salamander.noisetest.gui;

import me.salamander.noisetest.gui.panels.GUINoiseModule;
import me.salamander.noisetest.modules.combiner.*;
import me.salamander.noisetest.modules.modifier.Turbulence;
import me.salamander.noisetest.modules.source.*;
import me.salamander.noisetest.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static me.salamander.noisetest.gui.ModuleCategory.*;

public class Modules {
    public static List<Pair<ModuleCategory, List<Pair<String, Supplier<GUINoiseModule>>>>> categories = new ArrayList<>();
    public static Supplier<GUINoiseModule> register(String name, ModuleCategory category, Supplier<GUINoiseModule> supplier){
        categories.get(category.ordinal()).getSecond().add(new Pair<>(name, supplier));
        return supplier;
    }

    static {
        for(ModuleCategory category : ModuleCategory.values()){
            categories.add(new Pair<>(category, new ArrayList<>()));
        }
    }

    private static Parameter[] PERLIN_PARAMETERS = new Parameter[]{
            new Parameter("Octaves", 0, 1, 10, 1),
            new Parameter("Frequency", 1, 0.1, 5, 0.1),
            new Parameter("Persistence", 2, 0.1, 1.0, 0.1),
            new Parameter("Lacunarity", 3, 1, 5, 0.1)
    };
    private static Parameter[] TURBULENCE_PARAMETERS = new Parameter[]{
            new Parameter("Turbulence Power", 0, 0.1, 5.0, 0.1),
            new Parameter("Frequency", 1, 0.1, 5.0, 0.1)
    };
    private static Parameter[] SELECT_PARAMETERS = new Parameter[]{
            new Parameter("Threshold", 0, -1.0, 1.0, 0.1),
            new Parameter("Edge Falloff", 1, 0.0, 1.0, 0.1)
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
    private static String[] SELECT_INPUTS = new String[]{"Source One", "Source Two", "Selector"};

    public static Supplier<GUINoiseModule> PERLIN = register("Perlin", SOURCE,() -> new GUINoiseModule("Perlin", new Perlin(), PERLIN_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> BILLOW = register("Billow", SOURCE, () -> new GUINoiseModule("Billow", new Billow(), PERLIN_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> RIDGE = register("Ridge", SOURCE, () -> new GUINoiseModule("Ridge", new Ridge(), PERLIN_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> CHECKERBOARD = register("Checkerboard", SOURCE, () -> new GUINoiseModule("Checkerboard", new CheckerBoard(), FREQUENCY_ONLY, NO_INPUTS));
    public static Supplier<GUINoiseModule> CONST = register("Const", SOURCE, () -> new GUINoiseModule("Const", new Const(), CONST_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> INCOHERENT = register("Incoherent", SOURCE, () -> new GUINoiseModule("Incoherent", new Incoherent(), NO_PARAMETERS, NO_INPUTS));

    public static Supplier<GUINoiseModule> ADD = register("Add", COMBINER, () -> new GUINoiseModule("Add", new Add(null, null), NO_PARAMETERS, TWO_INPUTS));
    public static Supplier<GUINoiseModule> MULTIPLY = register("Multiply", COMBINER, () -> new GUINoiseModule("Multiply", new Multiply(null, null), NO_PARAMETERS, TWO_INPUTS));
    public static Supplier<GUINoiseModule> MAX = register("Max", COMBINER, () -> new GUINoiseModule("Max", new Max(null, null), NO_PARAMETERS, TWO_INPUTS));
    public static Supplier<GUINoiseModule> MIN = register("Min", COMBINER, () -> new GUINoiseModule("Min", new Min(null, null), NO_PARAMETERS, TWO_INPUTS));
    public static Supplier<GUINoiseModule> SELECT = register("Select", COMBINER, () -> new GUINoiseModule("Select", new Select(null, null, null), SELECT_PARAMETERS, SELECT_INPUTS));

    public static Supplier<GUINoiseModule> TURBULENCE = register("Turbulence", MODIFIER, () -> new GUINoiseModule("Turbulence", new Turbulence(null), TURBULENCE_PARAMETERS, ONE_INPUT));

}
