package me.salamander.noisetest.modules.combiner;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.glsl.*;
import me.salamander.noisetest.modules.GUIModule;
import me.salamander.noisetest.modules.SerializableNoiseModule;
import me.salamander.noisetest.modules.source.Const;
import me.salamander.noisetest.util.Util;

import java.io.IOException;
import java.util.*;

public class Select implements GUIModule, GLSLCompilable {
    private SerializableNoiseModule noiseMapOne, noiseMapTwo, selector;
    private double edgeFalloff = 0.0;
    private double threshold = 0;

    private final SelectFunction func;

    public Select(SerializableNoiseModule noiseMapOne, SerializableNoiseModule noiseMapTwo, SerializableNoiseModule selector){
        this.noiseMapOne = noiseMapOne;
        this.noiseMapTwo = noiseMapTwo;
        this.selector = selector;

        requires.add(func = new SelectFunction());
    }

    public Select(SerializableNoiseModule noiseMapOne, SerializableNoiseModule noiseMapTwo, SerializableNoiseModule selector, double edgeFalloff, double threshold){
        this(noiseMapOne, noiseMapTwo, selector);
        this.edgeFalloff = edgeFalloff;
        this.threshold = threshold;
    }

    @Override
    public double sample(double x, double y) {
        final double selectorValue = SerializableNoiseModule.safeSample(selector, x, y);

        if(edgeFalloff > 0.0){
            if(selectorValue < threshold - edgeFalloff){
                return SerializableNoiseModule.safeSample(noiseMapOne, x, y);
            }else if(selectorValue < threshold + edgeFalloff){
                return cubicInterpolation(
                        SerializableNoiseModule.safeSample(noiseMapOne, x, y),
                        SerializableNoiseModule.safeSample(noiseMapTwo, x, y),
                        (selectorValue - threshold + edgeFalloff) / (2 * edgeFalloff)
                );
            }else{
                return SerializableNoiseModule.safeSample(noiseMapTwo, x, y);
            }
        }else{
            if(selectorValue < threshold){
                return SerializableNoiseModule.safeSample(noiseMapOne, x, y);
            }else{
                return SerializableNoiseModule.safeSample(noiseMapTwo, x, y);
            }
        }
    }

    public Select getVisualizer(double lowValue, double highValue){
        return new Select(new Const(lowValue), new Const(highValue), selector, edgeFalloff, threshold);
    }

    public void setNoiseMapOne(SerializableNoiseModule noiseMapOne) {
        this.noiseMapOne = noiseMapOne;
    }

    public void setNoiseMapTwo(SerializableNoiseModule noiseMapTwo) {
        this.noiseMapTwo = noiseMapTwo;
    }

    public void setSelector(SerializableNoiseModule selector) {
        this.selector = selector;
    }

    public void setEdgeFalloff(double edgeFalloff) {
        this.edgeFalloff = edgeFalloff;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    private static double cubicInterpolation(double v0, double v1, double t){
        t = t * t * (3 - 2 * t);
        return v0 * (1 - t) + v1 * t;
    }

    @Override
    public void setSeed(long s) {
        if(noiseMapOne != null) noiseMapOne.setSeed(s);
        if(noiseMapTwo != null) noiseMapTwo.setSeed(s);
        if(selector != null) selector.setSeed(s);
    }


    @Override
    public int numInputs() {
        return 3;
    }

    @Override
    public void setInput(int index, SerializableNoiseModule module) {
        switch (index){
            case 0:
                noiseMapOne = module;
                break;
            case 1:
                noiseMapTwo = module;
                break;
            case 2:
                selector = module;
                break;
            default:
                throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public SerializableNoiseModule getInput(int index) {
        switch (index){
            case 0:
                return noiseMapOne;
            case 1:
                return noiseMapTwo;
            case 2:
                return selector;
            default:
                throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public void setParameter(int index, double value) {
        if(index == 0){
            threshold = value;
        }else if(index == 1){
            edgeFalloff = value;
        }else{
            throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public double getParameter(int index) {
        if(index == 0){
            return threshold;
        }else if(index == 1){
            return edgeFalloff;
        }else{
            throw new IllegalArgumentException("Index out of bounds!");
        }
    }

    @Override
    public Collection<SerializableNoiseModule> getSources() {
        List<SerializableNoiseModule> sources =  new ArrayList<>();
        sources.add(selector);
        sources.add(noiseMapOne);
        sources.add(noiseMapTwo);
        return sources;
    }

    @Override
    public void readNBT(CompoundTag tag, List<SerializableNoiseModule> sourceLookup) {
        edgeFalloff = tag.getDouble("edgeFalloff");
        threshold = tag.getDouble("threshold");

        if(tag.containsKey("inputOne")){
            noiseMapOne = sourceLookup.get(tag.getInt("inputOne"));
        }

        if(tag.containsKey("inputTwo")){
            noiseMapTwo = sourceLookup.get(tag.getInt("inputTwo"));
        }

        if(tag.containsKey("selector")){
            selector = sourceLookup.get(tag.getInt("selector"));
        }
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<SerializableNoiseModule, Integer> indexLookup) {
        tag.putDouble("edgeFalloff", edgeFalloff);
        tag.putDouble("threshold", threshold);

        if(noiseMapOne != null){
            tag.putInt("inputOne", indexLookup.get(noiseMapOne));
        }

        if(noiseMapTwo != null){
            tag.putInt("inputTwo", indexLookup.get(noiseMapTwo));
        }

        if(selector != null){
            tag.putInt("selector", indexLookup.get(selector));
        }
    }

    @Override
    public String getNodeRegistryName() {
        return "Select";
    }


    @Override
    public String glslExpression(String vec2Name, String seedName) {
        return func.name() + "(" + vec2Name + ", " + seedName + ")";
    }

    @Override
    public Set<FunctionInfo> requiredFunctions() {
        return requires;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Select select = (Select) o;
        return Double.compare(select.edgeFalloff, edgeFalloff) == 0 && Double.compare(select.threshold, threshold) == 0 && Objects.equals(noiseMapOne, select.noiseMapOne) && Objects.equals(noiseMapTwo, select.noiseMapTwo) && Objects.equals(selector, select.selector) && Objects.equals(requires, select.requires);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noiseMapOne, noiseMapTwo, selector, edgeFalloff, threshold, requires);
    }

    private final Set<FunctionInfo> requires = new HashSet<>();

    private static final FormattableText caseOne;
    private static final FormattableText caseTwo;

    private final class SelectFunction implements FunctionInfo{
        @Override
        public String name() {
            return "select_" + Math.abs(Select.this.hashCode());
        }

        @Override
        public String generateCode() {
            if(noiseMapOne instanceof GLSLCompilable compileOne && noiseMapTwo instanceof GLSLCompilable compileTwo && selector instanceof GLSLCompilable compileSelector){
                Map<String, Object> lookup = new HashMap<>();

                lookup.put("threshold", threshold);
                lookup.put("falloff", edgeFalloff);

                lookup.put("callOne", compileOne.glslExpression("pos", "seed"));
                lookup.put("callTwo", compileTwo.glslExpression("pos", "seed"));
                lookup.put("selectorCall", compileSelector.glslExpression("pos", "seed"));

                lookup.put("name", name());

                if(edgeFalloff <= 0.0){
                    return caseOne.evaluate(lookup);
                }else{
                    return caseTwo.evaluate(lookup);
                }
            }else{
                throw new NotCompilableException();
            }
        }

        @Override
        public String forwardDeclaration() {
            return "float " + name() + "(vec2 pos, int seed)";
        }

        @Override
        public Set<FunctionInfo> requiredFunctions() {
            if(noiseMapOne instanceof GLSLCompilable compileOne && noiseMapTwo instanceof GLSLCompilable compileTwo && selector instanceof GLSLCompilable compileSelector){
                Set<FunctionInfo> combined = new HashSet<>(compileOne.requiredFunctions());
                combined.addAll(compileTwo.requiredFunctions());
                combined.addAll(compileSelector.requiredFunctions());

                if(edgeFalloff > 0){
                    combined.add(FunctionRegistry.getFunction("cubicInterpolation"));
                }

                return combined;
            }else{
                throw new NotCompilableException();
            }
        }
    }

    @Override
    public long getSeed() {
        return selector == null ? 0 : selector.getSeed();
    }

    static {
        try {
            caseOne = new FormattableText(Util.loadResource("/glsl/extra/select1.func"));
            caseTwo = new FormattableText(Util.loadResource("/glsl/extra/select2.func"));
        }catch (IOException e){
            throw new IllegalStateException("Couldn't load functions", e);
        }
    }
}
