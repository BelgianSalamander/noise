package me.salamander.noisetest.gui;

import io.github.antiquitymc.nbt.*;
import me.salamander.noisetest.gui.panels.GUINoiseModule;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.combiner.*;
import me.salamander.noisetest.modules.combiner.BinaryFunctionType;
import me.salamander.noisetest.modules.modifier.Turbulence;
import me.salamander.noisetest.modules.modifier.Voronoi;
import me.salamander.noisetest.modules.source.*;
import me.salamander.noisetest.modules.combiner.BinaryModule;
import me.salamander.noisetest.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Function;
import java.util.function.Supplier;

import static me.salamander.noisetest.gui.ModuleCategory.*;

public class Modules {
    public static List<Pair<ModuleCategory, List<Pair<String, Supplier<GUINoiseModule>>>>> categories = new ArrayList<>();
    public static Supplier<GUINoiseModule> register(String name, ModuleCategory category, Supplier<GUINoiseModule> supplier){
        categories.get(category.ordinal()).getSecond().add(new Pair<>(name, supplier));
        return supplier;
    }

    private static Map<String, Supplier<? extends NoiseModule>> nodeRegistry = new HashMap<>();
    private static Map<String, Function<? extends GUIModule, GUINoiseModule>> nodeDisplayerRegistry = new HashMap<>();

    //toGUIModule should be null if the NoiseModule does not implement GUIModule
    public static <T extends NoiseModule> void registerNode(Supplier<T> nodeSupplier) {
    	String id = nodeSupplier.get().getNodeRegistryName();
        nodeRegistry.put(id, nodeSupplier);
    }

    public static <T extends GUIModule> void registerNode(Supplier<T> nodeSupplier, Function<T, GUINoiseModule> guiSupplier){
        String id = nodeSupplier.get().getNodeRegistryName();
        nodeRegistry.put(id, nodeSupplier);
        nodeDisplayerRegistry.put(id, guiSupplier);
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
    private static Parameter[] VORONOI_PARAMETERS = new Parameter[]{
            new Parameter("Size", 0, 0.1, 5.0, 0.1),
            new Parameter("Relaxation", 1, 0.0, 1.0, 0.1)
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

    static {
        for(ModuleCategory category : ModuleCategory.values()){
            categories.add(new Pair<>(category, new ArrayList<>()));
        }

        // Register Node Suppliers Here (used for deserialisation, to get an instance to call readNBT on)
	    registerNode(() -> new NoiseSourceModule(NoiseType.PERLIN), m -> new GUINoiseModule(m.getNoiseType().toString(), m, PERLIN_PARAMETERS, NO_INPUTS));
        registerNode(() -> new BinaryModule(BinaryFunctionType.ADD), m -> new GUINoiseModule(m.getFunctionType().getNbtIdentifier(), m, NO_PARAMETERS, ONE_INPUT));
        registerNode(() -> new Turbulence(null), m -> new GUINoiseModule("Turbulence", m, TURBULENCE_PARAMETERS, ONE_INPUT));
    }

    /* To serialize a module, all the modules that this module relies on are collected, removing duplicates.
     * Then, each of them write their NBT and to write sources, instead of writing the whole node, they write
     * the index.
     */
    public static CompoundTag serializeNode(NoiseModule module){
        CompoundTag result = new CompoundTag();
        ListTag<CompoundTag> modulesTag = new ListTag<>(TagType.Standard.COMPOUND);
        Set<NoiseModule> modules = new HashSet<>();
        Stack<NoiseModule> toProcess = new Stack<>();
        toProcess.push(module);

        while(!toProcess.isEmpty()){
            NoiseModule top = toProcess.pop();
            modules.add(top);
            toProcess.addAll(top.getSources());
        }

        List<NoiseModule> modulesList = new ArrayList<>(modules);

        IdentityHashMap<NoiseModule, Integer> indexLookup = new IdentityHashMap<>();
        for(int i = 0; i < modulesList.size(); i++){
            indexLookup.put(modulesList.get(i), i);
        }

        for(int i = 0; i < modulesList.size(); i++){
            CompoundTag tag = new CompoundTag();
            NoiseModule currentModule = modulesList.get(i);
            tag.putString("type", currentModule.getNodeRegistryName());

            CompoundTag properties = new CompoundTag();
            currentModule.writeNBT(properties, indexLookup);
            tag.put("properties", properties);

            modulesTag.add(tag);
        }

        result.put("modules", modulesTag);
        result.putInt("head", indexLookup.get(module));

        return result;
    }
    public static NoiseModule deserializeNode(CompoundTag tag){
        int headIndex = tag.getInt("head");
        ListTag<CompoundTag> modulesTag = (ListTag<CompoundTag>) tag.get("modules", TagType.Standard.LIST);

        List<NoiseModule> modules = new ArrayList<>(modulesTag.size());
        for(int i = 0; i < modulesTag.size(); i++){
            NoiseModule module = nodeRegistry.get(modulesTag.get(i).getString("type")).get();
            modules.add(module);
        }

        for(int i = 0; i < modules.size(); i++){
            modules.get(i).readNBT((CompoundTag) modulesTag.get(i).get("properties"), modules);
        }

        return modules.get(headIndex);
    }

    public static Supplier<GUINoiseModule> PERLIN = register("Perlin", SOURCE,() -> new GUINoiseModule("Perlin", new NoiseSourceModule(NoiseType.PERLIN), PERLIN_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> SIMPLEX = register("Simplex", SOURCE, () -> new GUINoiseModule("Simplex", new NoiseSourceModule(NoiseType.SIMPLEX), PERLIN_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> OPENSIMPLEX = register("OpenSimplex", SOURCE, () -> new GUINoiseModule("OpenSimplex", new NoiseSourceModule(NoiseType.OPEN_SIMPLEX), PERLIN_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> BILLOW = register("Billow", SOURCE, () -> new GUINoiseModule("Billow", new NoiseSourceModule(NoiseType.BILLOW), PERLIN_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> RIDGE = register("Ridge", SOURCE, () -> new GUINoiseModule("Ridge", new Ridge(), PERLIN_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> CHECKERBOARD = register("Checkerboard", SOURCE, () -> new GUINoiseModule("Checkerboard", new CheckerBoard(), FREQUENCY_ONLY, NO_INPUTS));
    public static Supplier<GUINoiseModule> CONST = register("Const", SOURCE, () -> new GUINoiseModule("Const", new Const(), CONST_PARAMETERS, NO_INPUTS));
    public static Supplier<GUINoiseModule> INCOHERENT = register("Incoherent", SOURCE, () -> new GUINoiseModule("Incoherent", new Incoherent(), NO_PARAMETERS, NO_INPUTS));

    public static Supplier<GUINoiseModule> ADD = register("Add", COMBINER, () -> new GUINoiseModule("Add", new BinaryModule(BinaryFunctionType.ADD), NO_PARAMETERS, TWO_INPUTS));
    public static Supplier<GUINoiseModule> MULTIPLY = register("Multiply", COMBINER, () -> new GUINoiseModule("Multiply", new BinaryModule(BinaryFunctionType.MULTIPLY), NO_PARAMETERS, TWO_INPUTS));
    public static Supplier<GUINoiseModule> MAX = register("Max", COMBINER, () -> new GUINoiseModule("Max", new BinaryModule(BinaryFunctionType.MAX), NO_PARAMETERS, TWO_INPUTS));
    public static Supplier<GUINoiseModule> MIN = register("Min", COMBINER, () -> new GUINoiseModule("Min", new BinaryModule(BinaryFunctionType.MIN), NO_PARAMETERS, TWO_INPUTS));
    public static Supplier<GUINoiseModule> SELECT = register("Select", COMBINER, () -> new GUINoiseModule("Select", new Select(null, null, null), SELECT_PARAMETERS, SELECT_INPUTS));

    public static Supplier<GUINoiseModule> TURBULENCE = register("Turbulence", MODIFIER, () -> new GUINoiseModule("Turbulence", new Turbulence(null), TURBULENCE_PARAMETERS, ONE_INPUT));
    public static Supplier<GUINoiseModule> VORONOI = register("Voronoi", MODIFIER, () -> new GUINoiseModule("Voronoi", new Voronoi(), VORONOI_PARAMETERS, ONE_INPUT));
}
