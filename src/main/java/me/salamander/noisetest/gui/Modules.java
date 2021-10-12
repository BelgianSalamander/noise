package me.salamander.noisetest.gui;

import io.github.antiquitymc.nbt.*;
import me.salamander.noisetest.gui.panels.GUINoiseModule;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.combiner.*;
import me.salamander.noisetest.modules.combiner.BinaryFunctionType;
import me.salamander.noisetest.modules.modifier.Turbulence;
import me.salamander.noisetest.modules.modifier.Voronoi;
import me.salamander.noisetest.modules.source.*;
import me.salamander.noisetest.modules.combiner.BinaryModule;
import me.salamander.noisetest.util.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static me.salamander.noisetest.gui.ModuleCategory.*;

public class Modules {
    public static List<Pair<ModuleCategory, List<Pair<String, Supplier<GUINoiseModule>>>>> categories = new ArrayList<>();
    public static Supplier<GUINoiseModule> register(String name, ModuleCategory category, Supplier<GUIModule> supplier){
        Supplier<GUINoiseModule> GUISupplier = () -> createComponent(supplier.get());
        categories.get(category.ordinal()).getSecond().add(new Pair<>(name, GUISupplier));
        return GUISupplier;
    }

    private static Map<String, Supplier<SerializableNoiseModule>> nodeRegistry = new HashMap<>();
    private static Map<String, Function<GUIModule, GUINoiseModule>> nodeDisplayerRegistry = new HashMap<>();

    //toGUIModule should be null if the NoiseModule does not implement GUIModule
    public static void registerNode(Supplier<SerializableNoiseModule> nodeSupplier) {
    	String id = nodeSupplier.get().getNodeRegistryName();
        nodeRegistry.put(id, nodeSupplier);
    }

    public static void registerNode(Supplier<SerializableNoiseModule> nodeSupplier, Function<GUIModule, GUINoiseModule> guiSupplier){
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
	    registerNode(() -> new NoiseSourceModule(NoiseType.PERLIN), m -> new GUINoiseModule(((NoiseSourceModule) m).getNoiseType().toString(), m, PERLIN_PARAMETERS, NO_INPUTS));
        registerNode(() -> new Ridge(), m -> new GUINoiseModule("Ridge", m, PERLIN_PARAMETERS, NO_INPUTS));
        registerNode(() -> new CheckerBoard(), m -> new GUINoiseModule("Checkerboard", m, FREQUENCY_ONLY, NO_INPUTS));
        registerNode(() -> new Const(), m -> new GUINoiseModule("Const", m, CONST_PARAMETERS, NO_INPUTS));
        registerNode(() -> new Incoherent(), m -> new GUINoiseModule("Incoherent", m, NO_PARAMETERS, NO_INPUTS));

        registerNode(() -> new BinaryModule(BinaryFunctionType.ADD), m -> new GUINoiseModule(((BinaryModule) m).getFunctionType().getNbtIdentifier(), m, NO_PARAMETERS, TWO_INPUTS));
        registerNode(() -> new Select(null, null, null), m -> new GUINoiseModule("Select", m, SELECT_PARAMETERS, SELECT_INPUTS));

        registerNode(() -> new Turbulence(null), m -> new GUINoiseModule("Turbulence", m, TURBULENCE_PARAMETERS, ONE_INPUT));
        registerNode(() -> new Voronoi(), m -> new GUINoiseModule("Voronoi", m, VORONOI_PARAMETERS, ONE_INPUT));
    }

    public static GUINoiseModule createComponent(GUIModule module){
        Function<GUIModule, GUINoiseModule> upgrader = nodeDisplayerRegistry.get(module.getNodeRegistryName());
        if(upgrader == null){
            throw new IllegalArgumentException("Cannot convert " + module.getNodeRegistryName() + " to GUINoiseModule! It does not have a registered converter");
        }

        return upgrader.apply(module);
    }

    /* To serialize a module, all the modules that this module relies on are collected, removing duplicates.
     * Then, each of them write their NBT and to write sources, instead of writing the whole node, they write
     * the index.
     */
    public static CompoundTag serializeNode(SerializableNoiseModule module){
        CompoundTag result = new CompoundTag();
        result.putString("type", "singleModule");
        Set<SerializableNoiseModule> modules = new HashSet<>();
        Stack<SerializableNoiseModule> toProcess = new Stack<>();
        toProcess.push(module);

        while(!toProcess.isEmpty()){
            SerializableNoiseModule top = toProcess.pop();
            if(modules.add(top)) {
                toProcess.addAll(top.getSources());
            }
        }

        List<SerializableNoiseModule> modulesList = new ArrayList<>(modules);

        ListTag<CompoundTag> modulesTag = serializeNodes(modulesList);

        result.put("modules", modulesTag);
        result.putInt("head", modulesList.indexOf(module));

        return result;
    }

    //All nodes that need to be serialized should be
    public static ListTag<CompoundTag> serializeNodes(Collection<SerializableNoiseModule> nodes){
        return serializeNodes(nodes.toArray(SerializableNoiseModule[]::new));
    }
    public static ListTag<CompoundTag> serializeNodes(SerializableNoiseModule[] nodes){
        ListTag<CompoundTag> nodeList = new ListTag<>(TagType.Standard.COMPOUND);
        List<SerializableNoiseModule> moduleList = new ArrayList<>(Arrays.asList(nodes));

        IdentityHashMap<SerializableNoiseModule, Integer> indices = new IdentityHashMap<>();

        for(int i = 0; i < moduleList.size(); i++){
            indices.put(moduleList.get(i), i);
        }

        for(SerializableNoiseModule module : nodes){
            CompoundTag singleModule = new CompoundTag();
            singleModule.putString("type", module.getNodeRegistryName());
            CompoundTag properties = new CompoundTag();
            module.writeNBT(properties, indices);
            singleModule.put("properties", properties);
            nodeList.add(singleModule);
        }

        return nodeList;
    }

    public static SerializableNoiseModule deserializeNode(CompoundTag tag){
        int headIndex = tag.getInt("head");
        ListTag<CompoundTag> modulesTag = (ListTag<CompoundTag>) tag.get("modules", TagType.Standard.LIST);

        List<SerializableNoiseModule> modules = deserializeNodes(modulesTag);

        return modules.get(headIndex);
    }
    public static List<SerializableNoiseModule> deserializeNodes(ListTag<CompoundTag> modulesTag){
        List<SerializableNoiseModule> modules = new ArrayList<>(modulesTag.size());
        for(int i = 0; i < modulesTag.size(); i++){
            SerializableNoiseModule module = nodeRegistry.get(modulesTag.get(i).getString("type")).get();
            modules.add(module);
        }

        for(int i = 0; i < modules.size(); i++){
            modules.get(i).readNBT((CompoundTag) modulesTag.get(i).get("properties"), modules);
        }

        return modules;
    }

    public static Supplier<GUINoiseModule> PERLIN = register("Perlin", SOURCE,() -> new NoiseSourceModule(NoiseType.PERLIN));
    public static Supplier<GUINoiseModule> SIMPLEX = register("Simplex", SOURCE,() -> new NoiseSourceModule(NoiseType.SIMPLEX));
    public static Supplier<GUINoiseModule> OPEN_SIMPLEX = register("OpenSimplex", SOURCE, () -> new NoiseSourceModule(NoiseType.OPEN_SIMPLEX2S));
    public static Supplier<GUINoiseModule> BILLOW = register("Billow", SOURCE, () -> new NoiseSourceModule(NoiseType.BILLOW));
    public static Supplier<GUINoiseModule> RIDGE = register("Ridge", SOURCE, () -> new Ridge());
    public static Supplier<GUINoiseModule> CHECKERBOARD = register("Checkerboard", SOURCE, () -> new CheckerBoard());
    public static Supplier<GUINoiseModule> CONST = register("Const", SOURCE, () -> new Const());
    public static Supplier<GUINoiseModule> INCOHERENT = register("Incoherent", SOURCE, () -> new Incoherent());

    public static Supplier<GUINoiseModule> ADD = register("Add", COMBINER, () -> new BinaryModule(BinaryFunctionType.ADD));
    public static Supplier<GUINoiseModule> MULTIPLY = register("Multiply", COMBINER, () -> new BinaryModule(BinaryFunctionType.MULTIPLY));
    public static Supplier<GUINoiseModule> MAX = register("Max", COMBINER, () -> new BinaryModule(BinaryFunctionType.MAX));
    public static Supplier<GUINoiseModule> MIN = register("Min", COMBINER, () -> new BinaryModule(BinaryFunctionType.MIN));
    public static Supplier<GUINoiseModule> SELECT = register("Select", COMBINER, () -> new Select(null, null, null));

    public static Supplier<GUINoiseModule> TURBULENCE = register("Turbulence", MODIFIER, () -> new Turbulence(null));
    public static Supplier<GUINoiseModule> VORONOI = register("Voronoi", MODIFIER, () -> new Voronoi());
}
